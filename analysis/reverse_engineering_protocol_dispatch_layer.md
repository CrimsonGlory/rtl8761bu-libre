# RTL8761BU — Protocol Dispatch Layer Analysis

Source: `2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf`
Analysis session: 2026-06-08
Scripts used: `DecompileStringAssocAndNew.java`, `DecompileRemainingNewFns.java`

---

## Overview

The patch installs a **protocol dispatch layer** that intercepts all major BT protocol
message types before they reach ROM handlers. This layer is installed by
`FUN_8010e27c` (the `string_assoc_installer`) and consists of 6 functions that sit
between the BT stack's message pump and the ROM's default handlers.

Each function follows the pattern:
- Check if the incoming message matches a specific opcode
- If yes: apply patch-specific logic (eSCO, SSP crypto, AFH, VSC)
- Always tail-call the original ROM handler for the message type

---

## Dispatch Struct Layout

The BT stack uses a central message-type dispatch struct. `FUN_8010e27c` writes to
these offsets:

| Offset | Handler installed | Protocol type |
|--------|------------------|---------------|
| `+0x00` | `FUN_8010cc94` | unknown index-0 |
| `+0x18` | `FUN_8010d9f4` | LC RX (Link Controller Receive) |
| `+0x30` | `FUN_8010daa4` | HCI Event |
| `+0x3c` | `FUN_8010da70` | LC TX (Link Controller Transmit) |
| `+0x48` | `FUN_8010dfb0` | LMP (Link Management Protocol) |
| `+0x60` | `FUN_8010d154` | LMP CH (LMP channel passthrough) |

The struct base address is loaded via double-indirection from `0x8012ae8c`
(Kovah label: `PTR_PTR_8010e310`).

---

## Handler: `assoc_w_tLMP` — FUN_8010dfb0 (530 bytes, struct offset +0x48)

### Purpose
Intercepts LMP PDU processing. Adds eSCO connection activation logic and SSP
(Secure Simple Pairing) crypto state management.

### Opcode dispatch

All opcodes are **internal opcode values** (not raw LMP opcodes; internal encoding
is `raw_opcode << 1` for even, `(raw_opcode << 1) | 1` for extended).

| Internal opcode | LMP raw equiv | Handler path |
|-----------------|---------------|--------------|
| `0x480` | `0x240` = LMP_SIMPLE_PAIRING_CONFIRM | SSP crypto state: checks `conn_rec._x58_crypto` for `0x1c0e`/`0xc` patterns; conditionally saves/restores feature page byte |
| `0x489` | `0x244` + ext = LMP_DHKEY_CHECK | Same SSP crypto path |
| `0x270` | `0x138` = LMP internal eSCO | eSCO activation: if conn pending (`+0x8f & 0x10`) and no timers → calls `FUN_8010b5d8` (eSCO activator) |
| `0x259` | `0x12c` = LMP_ACCEPTED | State update: sets `ssp_struct[+0x10]` = 3 |
| `0x26f` | `0x137` = LMP timeout/eSCO timeout | AFH gate: if capable and not already scanning → fires `ROM FUN_80055204` + `FUN_800512a4(3,0)` |
| (others) | — | pass-through to ROM |

After calling ROM LMP handler: if opcode 0x480/0x489 and saved flag → calls
`FUN_8010d37c(conn_handle)` (SSP event notifier).

### Key data references
- `PTR_big_ol_struct_8010e1c4` → connection record base (stride `0x2b8` by conn_handle)
- `PTR_some_feature_page_base_8010e1c8` → feature page / capabilities array
- `PTR_struct_of_at_least_0x300_size_8010e1d0` → SSP state struct
- `PTR_send_LMP_NOT_ACCEPTED_1_8010e1d4` → ROM LMP_NOT_ACCEPTED sender
- `PTR_assoc_w_tLMP_1_8010e1f8` → original ROM LMP handler (tail-call target)

### Libre notes
Most complex handler. Requires:
- SSP crypto state machine (reads/writes to `_x58_crypto_struct`)
- eSCO activation delegation to `FUN_8010b5d8`
- ROM function pointers for LMP_NOT_ACCEPTED and LMP_ACCEPTED responses
- Feature page management (bit 5 of feature page byte 5)

---

## Handler: `unknown_fptr_index0` — FUN_8010cc94 (26 bytes, struct offset +0x00)

### Purpose
**Sequencer**, not interceptor. Calls ROM original first, then the patch add-on.

Disassembly confirms `param_1` is forwarded to **both** callees via MIPS `a0`
(Ghidra decompiler omits the implicit first argument on the ROM call):

```
8010cc94: addiu sp,-0x18
8010cc96: sw ra,0x14(sp)
8010cc98: sw s0,0x10(sp)
8010cc9a: lw v0,0x18(pc)          ; → 0x800138cd (ROM unknown_fptr_index0)
8010cc9c: jalr v0                 ; ROM(param_1)  — a0 still = param_1
8010cc9e: _move s0,a0              ; delay slot: save param_1
8010cca0: lw v0,0x14(pc)          ; → 0x8010ca21 (patch handler)
8010cca2: jalr v0                 ; patch(param_1)
8010cca4: _move a0,s0              ; delay slot: restore param_1
```

```c
void FUN_8010cc94(void *param_1) {
    unknown_fptr_index0(param_1);           // ROM @ 0x800138cc
    new_func_for_unknown_fptr_index0(param_1); // PATCH @ 0x8010ca20
}
```

### Literal pool (at 0x8010ccb0–0x8010ccb7)
| Slot | Value | Target |
|------|-------|--------|
| `0x8010ccb0` | `0x800138cd` | ROM `unknown_fptr_index0` @ `0x800138cc` (680 B) |
| `0x8010ccb4` | `0x8010ca21` | PATCH `new_func_for_unknown_fptr_index0` @ `0x8010ca20` (534 B) |

### ROM `unknown_fptr_index0` dispatch

Switch on internal message type at `param_1[8]` (16-bit), offset by −100:

| Case (`type−100`) | Raw type | Handler |
|-------------------|----------|---------|
| 0 | 100 (`0x64`) | `unknown_referencing_default_name_8` |
| 2 | 102 | `FUN_8000f4a0` |
| 3 | 103 | `FUN_80013840` |
| 4 | 104 | `FUN_80072020` |
| 5 | 105 | `FUN_80035640` / `FUN_800356bc` |
| 6 | 106 | indirect call via `param_1[0]` |
| 7 | 107 | `FUN_8007718c` |
| 8 | 108 | slot-scheduling fall-through (complex) |
| 10 | 110 | `FUN_800386d0` |
| 11 | 111 | callback dispatch via `PTR_PTR_80013bac` |
| 12 | 112 | indirect via `PTR_DAT_80013bb4` |
| 13 | 113 | `FUN_800559a0` |
| 16 | 116 | indirect via `PTR_DAT_80013bb8` |
| 17 | 117 | `FUN_80056ca8` + conn record update |

**Type `0x67` (103 decimal) is NOT in the ROM switch** — it hits `default: return`.
The patch handler `FUN_8010ca20` is the sole handler for type `0x67`.

### Libre notes
Trivial 26-byte sequencer: call ROM `0x800138cc`, then patch `0x8010ca20`.
Both calls receive the same `param_1` pointer.

---

## Handler: `assoc_w_tLC_TX` — FUN_8010da70 (44 bytes, struct offset +0x3c)

### Purpose
Intercepts Link Controller transmit path to inject eSCO codec encoding.

```c
void FUN_8010da70(int *param_1) {
    if (param_1[2] == 0x32e) {
        // eSCO TX frame: encode via codec
        int rc = (*DAT_8010da9c)(buf[0x19], buf[0x1a], &stack_buf);
        if (rc != 0) return;  // codec handled it; skip ROM
    }
    (*DAT_8010daa0)(param_1);  // original LC_TX handler
}
```

### Opcode
- `0x32e` = LC internal eSCO TX frame type. Bytes `buf[0x19]` and `buf[0x1a]` are
  codec parameters (likely codec type and sub-type).

### Literal pool (at 0x8010da98–0x8010daa3)
- `0x8010da9c` → codec encode fn (patch or ROM)
- `0x8010daa0` → original LC_TX ROM handler (tail-call)

### Libre notes
eSCO codec transmit: requires implementing or delegating the codec at `DAT_8010da9c`.
For basic BT without eSCO audio, this handler can be a direct pass-through.

---

## Handler: `assoc_w_tLC_RX` — FUN_8010d9f4 (98 bytes, struct offset +0x18)

### Purpose
Intercepts Link Controller receive path to capture eSCO connection state.

### Logic
```c
void FUN_8010d9f4(uint param_1) {
    if (param_1[2] == 0x2cd) {
        // eSCO RX: look up conn by handle
        uint conn_handle = *(PTR_DAT_8010da58 + 0xc);
        int state = conn_rec[conn_handle * 0x2b8 + 0x50];
        if (state != -1) {
            *PTR_DAT_8010da60 = 1;          // set flag
            *PTR_DAT_8010da64 = state;       // save state
            conn_rec[conn_handle * 0x2b8 + 0x50] = -1;  // clear
        }
        (*DAT_8010da68)(param_1);            // original handler
        if (*PTR_DAT_8010da60) {
            (*DAT_8010da6c)(conn_handle);    // post-hook with saved state
        }
        return;
    }
    (*DAT_8010da68)(param_1);  // non-eSCO: pass through
}
```

### Opcode
- `0x2cd` = LC internal eSCO RX frame type

### Literal pool (at 0x8010da54–0x8010da6f)
- `0x8010da58` → PTR_DAT (connection handle source struct)
- `0x8010da5c` → conn record array base (`big_ol_struct`, stride `0x2b8`)
- `0x8010da60` → flag byte (cleared/set around handler call)
- `0x8010da64` → saved state dword
- `0x8010da68` → original LC_RX ROM handler (primary call + tail-call)
- `0x8010da6c` → post-hook for eSCO state restore

### Libre notes
Manages eSCO timing/state field at `conn_rec+0x50` around the receive path. Required
for eSCO audio. For basic BT: pass-through to ROM LC_RX handler suffices.

---

## Handler: `assoc_w_tHCI_EVT` — FUN_8010daa4 (518 bytes, struct offset +0x30)

### Purpose
Most complex handler. Intercepts HCI events to manage: AFH scan, eSCO mode changes,
SSP state, BLE connection, and interrupt-masked register operations.

### Event dispatch table

| HCI event byte | Event name | Patch action |
|----------------|-----------|--------------|
| `0xe` | Command Complete | Sub-dispatch on opcode (bytes 3:4): |
| | opcode `0x402` | If payload[5]==0xc and scan inactive → cancel scan (`DAT_8010dcd0`); clear payload[5]; call `DAT_8010dcd4(0, saved_handle)` |
| | opcode `0x2002` | Patch payload bytes[6:7] = `0x1b, 0x00` |
| `0x1` | Inquiry Complete | If AFH scan active: call `VSC_0xfc95_called2_1`(scan_cancel), `FUN_8010dd10(handle, count*0x500)`, fire `FUN_8010dd14` |
| `0x3` | Connection Complete | Interrupt-masked: `(*DAT_8010dcf0)(opcode_combo, 1)`; then enable interrupts |
| `0x5` | Disconnection Complete | Calls `(*DAT_8010dcf8)(pbVar8)` |
| `0x4` | Auth Complete | Calls `(*DAT_8010dcf8)(pbVar8)` |
| `0x7` | Remote Name Req Complete | Check scan flag; if clear → call `(*DAT_8010dce8)()` |
| `0x2c` | Synchronous Conn Changed | Interrupt-masked register write |
| `0x14` | Role Change | Calls `(*DAT_8010dcfc)(pbVar8)` |
| `0x3e` | LE Meta-event | Sub-event 1/10 → `uVar7=1`; sub-event 2/13 → counter check; sub-event 3 → `uVar7=0`; call `(*pcVar5)(pbVar8, uVar7)` |
| `0x53` | Unknown (xor 0x53=0) | If scan flag clear → call `(*DAT_8010dce8)()` |
| (others) | — | Fall through |

All paths end with: `(*DAT_8010dd18)(param_1)` — original HCI_EVT ROM handler.

### Key data references (literal pool at 0x8010dcc8–0x8010dd1c)
- `0x8010dcc8` → scan state flag byte
- `0x8010dccc` → scan connection handle
- `0x8010dcd0` → scan cancel fn ptr
- `0x8010dcd4` → scan result fn ptr
- `0x8010dcd8` → AFH state struct ptr
- `0x8010dcdc` → `(*DAT_8010dcdc)` counter function
- `0x8010dce0` → LE meta handler fn
- `0x8010dce4` → init-done flag ptr
- `0x8010dce8` → init fn (if not done)
- `0x8010dcec` → `PTR_disable_interrupts`
- `0x8010dcf0` → interrupt-masked register write fn
- `0x8010dcf4` → `PTR_enable_interrupts`
- `0x8010dcf8` → handler for events 4/5
- `0x8010dcfc` → handler for event 0x14
- `0x8010dd00` → alt init fn
- `0x8010dd04` → check fn (returns code)
- `0x8010dd08` → saved handle ptr
- `0x8010dd0c` → `PTR_VSC_0xfc95_called2` (AFH scan complete)
- `0x8010dd10` → `FUN_8010dd10` (scan/timer fn)
- `0x8010dd14` → cmd complete forwarder
- `0x8010dd18` → original HCI_EVT ROM handler (tail-call)

### Libre notes
This handler manages the AFH scan lifecycle, LE connection establishment, and
interrupt-critical register operations. For a minimal libre:
- AFH scanning (event 0x1): must implement or stub
- LE meta (event 0x3e): required for BLE operation
- Interrupt masking (events 0x3/0x2c): critical for atomic register writes
- SSP events (0xe): patch payload modifications for compliance

---

## Handler: `assoc_w_tLMP_CH__passthrough` — FUN_8010d154 (16 bytes, struct offset +0x60)

### Purpose
Pure passthrough — delegates to original ROM LMP channel handler.

```c
void FUN_8010d154(void) {
    (*DAT_8010d164)();   // original LMP_CH ROM handler
}
```

### Literal pool
- `0x8010d164` → original ROM LMP_CH handler (Kovah: `assoc_w_tLMP_CH_passthrough`)

### Libre notes
Trivial: can be a direct jump to the ROM function at the fixed address (no patch
logic needed).

---

## HW Register Init (in `FUN_8010e27c`)

During `string_assoc_installer`, three BB registers are configured:

| Register | Operation | Decoded meaning |
|----------|-----------|-----------------|
| `0x40` | Write 2, then write 0 | Enable mode bit, then reset |
| `0x6b` | RMW: clear `[12:0]`, set `[15,13]` = `0xa000` | Enable features at bits 15,13 |
| `0x6c` | RMW: clear `[2:0]`, set `[2,0]` = `0x5` | Set mode bits |

These are baseband-controller registers accessed via the ROM register r/w interface
(`FUN_8001136c`/`FUN_8001139c`). The write-2-then-write-0 to reg 0x40 suggests a
"strobe" pattern: set enable bit, perform the config, then clear the enable.

---

## Calling Convention

All 6 handlers receive a pointer to a **message struct** as their first argument.
The struct layout:
- `+0x00`: word (4 bytes) — often a connection handle or pointer
- `+0x08`: 16-bit field — internal opcode / message type
- Further fields: message-type-specific payload

The internal opcode at `+0x08` corresponds to ROM message type constants (prefixed
`t` = type: `tLMP`, `tLC_TX`, etc.). These are NOT raw Bluetooth opcodes; they are
BT stack internal message identifiers.

---

## Summary for Libre

### Must implement
1. `FUN_8010e27c` — writes 6 fn-ptrs to dispatch struct offsets, programs 3 BB regs
2. `FUN_8010dfb0` — LMP handler (SSP + eSCO activation path)
3. `FUN_8010daa4` — HCI event handler (AFH, LE, interrupts)
4. `FUN_8010d154` — LMP_CH passthrough (trivial)
5. `FUN_8010cc94` — index-0 sequencer (ROM then patch; both get `param_1`)

### Can stub (basic BT only)
6. `FUN_8010ca20` — type-0x67 monitor: empty return (AFH/coexistence degraded; full impl needs 7 BSS counters + ROM sub-calls)
7. `FUN_8010da70` — LC TX: pass through to ROM
8. `FUN_8010d9f4` — LC RX: pass through to ROM

### Key identities needed
~~All resolved (2026-06-09). See Section: ROM Original Handler Addresses below.~~

---

## ROM Original Handler Addresses (resolved 2026-06-09)

Script: `FindStoreOffset.java` (repurposed) — GZF process mode

Each patch handler stores the ROM original in its literal pool and tail-calls it.
All pool slots were read directly from the DATA block.

| Pool slot | Owner patch fn | Raw ptr | ROM fn addr | Kovah name | Size |
|-----------|---------------|---------|-------------|------------|------|
| `0x8010ccb0` | `FUN_8010cc94` (pre-hook call, no explicit args) | `0x800138cd` | `0x800138cc` | `unknown_fptr_index0` | 680 B |
| `0x8010ccb4` | `FUN_8010cc94` (main call, with `param_1`) | `0x8010ca21` | `0x8010ca20` | `new_func_for_unknown_fptr_index0` | 534 B |
| `0x8010d164` | `FUN_8010d154` (LMP_CH pass-through) | `0x80066e69` | `0x80066e68` | `assoc_w_tLMP_CH` | 200 B |
| `0x8010da68` | `FUN_8010d9f4` (LC_RX interceptor) | `0x80042189` | `0x80042188` | `assoc_w_tLC_RX` | 634 B |
| `0x8010daa0` | `FUN_8010da70` (LC_TX interceptor) | `0x80042421` | `0x80042420` | `assoc_w_tLC_TX` | 418 B |
| `0x8010dd18` | `FUN_8010daa4` (HCI_EVT interceptor) | `0x80020bed` | `0x80020bec` | `assoc_w_tHCI_EVT` | 718 B |
| `0x8010e1f8` | `FUN_8010dfb0` (LMP interceptor) | `0x80071635` | `0x80071634` | `assoc_w_tLMP` | 462 B |

All raw pointers have bit 0 set (MIPS16e odd address convention).

### Naming convention clarified

Kovah uses the **same label** for both the ROM original and the struct slot:
- `assoc_w_tLMP` in ROM (`0x80071634`) is the function the patch **overrides** at struct[`+0x48`]
- The patch function `FUN_8010dfb0` intercepts calls and tail-calls back to ROM `assoc_w_tLMP`
- Likewise for LC_RX, LC_TX, HCI_EVT, LMP_CH

The only exception is the index-0 slot where `FUN_8010cc94` is a **sequencer** (not interceptor):
it calls ROM `unknown_fptr_index0` first (without forwarding `param_1` explicitly) then calls
`new_func_for_unknown_fptr_index0` (a **patch function**, not ROM) with `param_1`.

### Critical discovery: `new_func_for_unknown_fptr_index0` is a PATCH function

`0x8010ca20` is in the DATA block (runtime `0x80100000+`) — this is a 534-byte **patch**
function, not a ROM function. It is called by `FUN_8010cc94` as the "actual handler" after
the ROM original runs. It handles internal opcode `0x67` only:
- Maintains a count (2000 ms / 200-tick thresholds) for AFH/coexistence monitoring
- Reads `PTR_struct_of_at_least_0x300_size` (`bos`-class struct) at `+0x16a`
- Checks HW register `0x2d` for channel quality
- Manages a 9-step counter (`PTR_DAT_8010cc74`) and fires a cleanup fn at step 9
- Monitors BLE connection quality via a 200-tick counter
- Fires a diagnostic log if stuck condition detected (after 200 ticks in same state)

This function requires real libre implementation.

### ROM handler dispatch coverage

| ROM fn | Addr | Key opcodes handled |
|--------|------|---------------------|
| `unknown_fptr_index0` | `0x800138cc` | Switch on `param_1[4]-100`: cases 0,2,3,4,5,6,7,8,0xa,0xb,0xc,0xd,0x10,0x11 |
| `assoc_w_tLMP_CH` | `0x80066e68` | `0x3ea`, `0x3ed`, `0x3ee` (LMP channel sub-opcodes) |
| `assoc_w_tLC_RX` | `0x80042188` | `0x2be`, `0x2c1`, `0x2c4`, `0x2c5`, `0x2c8`, `0x2c9`, `0x2ca`, `0x2cd`, `0x2d0`, `0x2d1`, `0x2d3`, `0x4b7` |
| `assoc_w_tLC_TX` | `0x80042420` | `1`, `0x320`, `0x321`, `0x323`, `0x326`, `0x328`, `0x329`, `0x32a`, `0x32c`, `0x32d`, `0x32e`, `0x32f`, `0x330`, `0x4b6` |
| `assoc_w_tHCI_EVT` | `0x80020bec` | `0x1f6`, `0x1f8`, `0x1f9`, `0x1fa`, `0x1fb`, `0x1fc`, `0x1fd`, `0x452`, `0x453`, `0x454`, `0x455` |
| `assoc_w_tLMP` | `0x80071634` | Large LMP opcode dispatcher (opcodes `0x259`–`0x26d`+) |

### Updated libre requirements

For the libre implementation:

1. All **ROM** original handlers are called via their fixed addresses — no reimplementation needed.
2. `new_func_for_unknown_fptr_index0` (`0x8010ca20`, 534 B) **must be reimplemented** — see
   full decompile in Section below. Required for proper AFH/coexistence; can be stubbed for minimal BT.
3. The sequencer `FUN_8010cc94` must call ROM `0x800138cc` first, then `new_func_for_unknown_fptr_index0`,
   passing `param_1` to both.
4. All five other patch interceptors (`FUN_8010d154`, `FUN_8010d9f4`, `FUN_8010da70`, `FUN_8010daa4`,
   `FUN_8010dfb0`) call ROM at the addresses above as their tail-call / pass-through target.

---

## Handler: `new_func_for_unknown_fptr_index0` — FUN_8010ca20 (534 bytes)

Analysis session: 2026-06-09. Script: `DecompileCA20CC94.java` (GZF process mode).

### Purpose

Periodic **AFH / BLE coexistence quality monitor**. Invoked on every index-0 dispatch
message after the ROM handler returns. Only acts when internal message type at
`param_1[8]` equals `0x67` (timer/tick message not handled by ROM).

### Message struct (inferred from both handlers)

| Offset | Field | Used by |
|--------|-------|---------|
| `+0x00` | indirect fn ptr or flags | ROM case 6 |
| `+0x04` | conn handle / sub-fields | ROM cases 5, 13, 16 |
| `+0x08` | **internal message type** (16-bit) | ROM switch (`type−100`); patch checks `== 0x67` |
| `+0x0d` | byte arg | ROM case 16 |

### Logic flow (opcode `0x67` only)

```
param_1[8] == 0x67?
  no → return immediately
  yes ↓

1. TICK COUNTER (ushort @ 0x8012b944 via pool 0x8010cc38)
   if counter > 2000 → reset, fire diagnostic log (ROM 0x80074fa8)
   counter++

2. BLE COEXISTENCE STUCK DETECTOR
   if both BLE structs inactive (PTR_80124e84[4]==0, PTR_80124e54[4]==0, bit0 of [6]==0):
     mirror bit0 of [6] into byte @ 0x8012b828
   else:
     track conn id from list head PTR_80124e18 → +4 → +0xc
     compare with last id @ 0x8012b824; increment stuck byte @ 0x8012b828 on match
     if stuck byte > 200:
       diagnostic log (level 2, tag 0xfa)
       if BLE struct +0x1c != -1:
         struct[6] |= 0x0c
         ROM LMP__25C @ 0x80009a30(conn_handle, 0)
         ROM LMP__268 @ 0x80009a6c(conn_handle, 3)

3. AFH HW REGISTER POLL (if bos_struct[0x16a] & 1)
   read BB reg 0x2d via PATCH FUN_8010a5d8 (codec reg reader)
   if (value >> 8 & 0xf) == 5:
     increment byte counter @ 0x8012b81c
     at counter == 9:
       ROM FUN_8003ce50(0)
       ROM FUN_8003ce50(bos_struct[0x16a])
   else:
     reset counter @ 0x8012b81c to 0

4. BLE CONNECTION TIMING STUCK DETECTOR
   active if any of:
     bos_struct[0x16e] != 0
     conn_array[0x1d4] != 0          (base 0x8012382c)
     conn_array[0x44] & 1
     PTR_80124e84[4] != 0
   sample MMIO 0xb000a33c (HW timing counter)
   compare with last sample @ 0x8012b820
   if delta is 0x3fe or −0x402 → increment stuck byte @ 0x8012b81d
   if stuck byte > 200:
     diagnostic log (magic 0xdead)
     ROM FUN_80043038()              (recovery/cleanup)

5. increment ushort @ 0x8012b80c (global tick)
```

### Literal pool (0x8010cc38–0x8010cc90)

| Pool addr | Points to | Role |
|-----------|-----------|------|
| `0x8010cc38` | `0x8012b944` | Main 2000-tick ushort counter |
| `0x8010cc3c` | `0x80111068` | Log format string table |
| `0x8010cc40` | `0x801234b4` | Log argument data block |
| `0x8010cc44` | `0x80074fa8` | ROM `possible_logging_function?_var_args` |
| `0x8010cc48` | `0x80124e84` | BLE connection struct A |
| `0x8010cc4c` | `0x80124e54` | BLE connection struct B |
| `0x8010cc50` | `0x80124e18` | BLE connection list head |
| `0x8010cc54` | `0x8012b824` | Last-seen BLE conn id (int) |
| `0x8010cc58` | `0x8012b828` | BLE stuck-state byte counter |
| `0x8010cc5c` | `0x80009a04` | ROM timestamp/getter fn |
| `0x8010cc60` | `0x00dead55` | Debug magic constant for logs |
| `0x8010cc64` | `0x80009a30` | ROM `LMP__25C_called1` |
| `0x8010cc68` | `0x80009a6c` | ROM `LMP__268__most_common_for_VSCs2_checks_fptr_patch` |
| `0x8010cc6c` | `0x801259ec` | `struct_of_at_least_0x300_size` (bos-class) |
| `0x8010cc70` | `0x8010a5d8` | PATCH `FUN_8010a5d8` (codec/MMIO reg reader) |
| `0x8010cc74` | `0x8012b81c` | AFH 9-step byte counter |
| `0x8010cc78` | `0x8003ce50` | ROM cleanup fn (fired at step 9) |
| `0x8010cc7c` | `0x8012382c` | `base_of_0x1ac_struct_array_0xA_large2` |
| `0x8010cc80` | `0xb000a33c` | MMIO HW timing register |
| `0x8010cc84` | `0x8012b820` | Last MMIO timing sample |
| `0x8010cc88` | `0x8012b81d` | BLE timing stuck byte counter |
| `0x8010cc8c` | `0x80043038` | ROM recovery/cleanup fn |
| `0x8010cc90` | `0x8012b80c` | Global ushort tick counter |

### BSS globals required (all zero-init)

| RAM addr | Size | Purpose |
|----------|------|---------|
| `0x8012b80c` | 2 | Global tick counter |
| `0x8012b81c` | 1 | AFH 9-step counter |
| `0x8012b81d` | 1 | BLE timing stuck counter |
| `0x8012b820` | 4 | Last MMIO timing sample |
| `0x8012b824` | 4 | Last BLE conn id |
| `0x8012b828` | 1 | BLE coexistence stuck counter |
| `0x8012b944` | 2 | Main 2000-tick counter |

ROM-populated structs (`0x801259ec`, `0x8012382c`, `0x80124e84`, etc.) are
runtime-allocated by ROM — libre firmware reads them, does not initialize them.

### Sub-call summary

| Callee | Addr | Patch/ROM | When |
|--------|------|-----------|------|
| `possible_logging_function?_var_args` | `0x80074fa8` | ROM | Diagnostic logs (levels 2/3, tag `0xfa`) |
| `FUN_80009a04` | `0x80009a04` | ROM | Timestamp for log args |
| `LMP__25C_called1` | `0x80009a30` | ROM | BLE stuck recovery (conn handle) |
| `LMP__268__...` | `0x80009a6c` | ROM | BLE stuck recovery (mode 3) |
| `FUN_8010a5d8` | `0x8010a5d8` | **PATCH** | Read BB reg 0x2d |
| `FUN_8003ce50` | `0x8003ce50` | ROM | AFH cleanup at 9th consecutive poll |
| `FUN_80043038` | `0x80043038` | ROM | BLE timing stuck recovery |

### Libre implementation options

**Minimal stub** (basic BT, no AFH tuning):
```c
void new_func_for_unknown_fptr_index0(int *msg) {
    (void)msg;  /* or: if (*(short *)(msg + 2) != 0x67) return; */
}
```

**Full implementation** requires:
- 7 BSS counters (table above), zeroed at init
- ROM calls at fixed addresses for logging, LMP recovery, cleanup
- PATCH `FUN_8010a5d8` for register 0x2d read (already analyzed in sco_esco layer)
- No tail-call to ROM — function returns after patch-only work

### Libre implementation (2026-06-10)

`src/protocol_dispatch.S`: vendor-fixed @ PRAM+`0x2A20` (runtime `0x8010CA20`);
534 B byte-identical transcription + 130 B `fn_ca20_gap` @ PRAM+`0x2C36` (vendor
pool bridge to `fn_ccb8`). Regenerate: `scripts/gen_fn_ca20_asm.py`. Linker scatter
in `rtl8761bu.ld`. Prefix `[0x2A20,0x2CB8)` 0/664 diffs vs NF_REF. Replaces
STUB-T1 empty-return stub; completes T3 protocol-dispatch surface for P3.
