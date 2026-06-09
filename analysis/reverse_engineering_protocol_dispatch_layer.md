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
Minimal pre-hook: runs a side-effect callback before delegating to original handler.

```c
void FUN_8010cc94(void *param_1) {
    (*DAT_8010ccb0)();          // pre-hook (no args)
    (*DAT_8010ccb4)(param_1);   // original handler (with args)
}
```

### Literal pool (at 0x8010ccb0–0x8010ccb7)
- `0x8010ccb0` → pre-hook fn ptr (identity TBD — likely init/setup)
- `0x8010ccb4` → original index-0 handler (ROM)

### Libre notes
Trivial to implement. The pre-hook at `DAT_8010ccb0` needs identification. If the
pre-hook is benign (or a no-op in the libre case), this handler can delegate directly.

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
5. `FUN_8010cc94` — index-0 pre-hook (needs pre-hook fn identified)

### Can stub (basic BT only, no eSCO audio)
6. `FUN_8010da70` — LC TX: pass through to ROM
7. `FUN_8010d9f4` — LC RX: pass through to ROM

### Key identities needed
- ROM `assoc_w_tLMP` handler address (stored at `PTR_assoc_w_tLMP_1_8010e1f8`)
- ROM `assoc_w_tLC_TX` handler address
- ROM `assoc_w_tLC_RX` handler address
- ROM `assoc_w_tHCI_EVT` handler address
- ROM `assoc_w_tLMP_CH` handler address
- Dispatch struct base pointer address (`0x8012ae8c`)
