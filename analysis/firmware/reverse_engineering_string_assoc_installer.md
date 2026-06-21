# RTL8761BU — String-Associated Function Installer Analysis

Source: `2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf`
Analysis sessions: 2026-06-08 (initial, partially incorrect), revised 2026-06-08

---

## CORRECTION NOTICE

An earlier version of this document contained significant errors. The summary below
reflects the corrected understanding based on GZF PROCESS MODE analysis.

The main error: the "installer" was incorrectly identified as the address-pair table
at file offset 0xa0. In reality, the true installer is **`FUN_8010e27c`** (a patch
function), which directly installs 6 protocol-level dispatch handlers by writing
function pointers to struct offsets — it does NOT process the address-pair table.

---

## Summary

Kovah's label `patch_that_installs_all_the_string_associated_function_patches__including_LMP`
refers to **`FUN_8010e27c`** (runtime `0x8010e27c`), a 52-byte patch function that:

1. Installs 6 protocol dispatch handlers (function pointers) into the BT stack's
   central message-type dispatch struct (base loaded from `PTR_PTR_8010e310`)
2. Performs hardware register configuration (BB regs 0x40/0x6b/0x6c)
3. Writes two pairs of constant data to RAM locations

The 6 handler functions cover: LMP message processing, an index-0 handler, Link
Controller TX, Link Controller RX, HCI event processing, and LMP channel passthrough.

`FUN_8010e27c` is called indirectly by ROM function
`interesting_string_user_fptr_registration_function @ ROM 0x80009990` via a hook
slot in the BT state struct. The ROM installs the hook by reading the patch entry
`FUN_8010a000`'s literal pool.

---

## Architecture

```
ROM: interesting_string_user_fptr_registration_function @ 0x80009990
          │
          │ calls hook installed at hook slot (ptr from bos_struct area)
          ▼
PATCH: FUN_8010e27c  (= string_assoc_installer)
          │
          │ writes 6 fn-ptrs into dispatch struct (base = PTR_PTR_8010e310)
          │
          ├─ struct[+0x00] = FUN_8010cc94  (unknown_fptr_index0)
          ├─ struct[+0x18] = FUN_8010d9f4  (assoc_w_tLC_RX)
          ├─ struct[+0x30] = FUN_8010daa4  (assoc_w_tHCI_EVT)
          ├─ struct[+0x3c] = FUN_8010da70  (assoc_w_tLC_TX)
          ├─ struct[+0x48] = FUN_8010dfb0  (assoc_w_tLMP)
          └─ struct[+0x60] = FUN_8010d154  (assoc_w_tLMP_CH__passthrough)
```

---

## `FUN_8010e27c` Decompile (52 bytes)

```c
void FUN_8010e27c(void) {
    // Struct base loaded from PTR_PTR_8010e310 (double-indirect)
    code **dispatch_struct = *PTR_PTR_8010e310;

    // Install 6 protocol dispatch handlers:
    dispatch_struct[0x00/4] = FUN_8010cc94;   // unknown index-0 handler
    dispatch_struct[0x18/4] = FUN_8010d9f4;   // assoc_w_tLC_RX
    dispatch_struct[0x30/4] = FUN_8010daa4;   // assoc_w_tHCI_EVT
    dispatch_struct[0x3c/4] = FUN_8010da70;   // assoc_w_tLC_TX
    dispatch_struct[0x48/4] = FUN_8010dfb0;   // assoc_w_tLMP
    dispatch_struct[0x60/4] = FUN_8010d154;   // assoc_w_tLMP_CH__passthrough

    // HW register init:
    (*pcVar2)(0x40, 1, 2);                    // write 2 to BB reg 0x40
    rVal = (*pcVar1)(0x6b);                   // read BB reg 0x6b
    (*pcVar2)(0x6b, 1, (rVal & ~0x1fff) | 0xa000); // set bits 15,13
    rVal = (*pcVar1)(0x6c);                   // read BB reg 0x6c
    (*pcVar2)(0x6c, 1, (rVal & ~7) | 5);      // set bits 2,0
    (*pcVar2)(0x40, 1, 0);                    // write 0 to BB reg 0x40 (reset)

    // Initialize two RAM data pairs:
    *(PTR_DAT_8010e334) = DAT_8010e330;
    *(PTR_DAT_8010e33c) = DAT_8010e338;
    *(PTR_DAT_8010e34c) = DAT_8010e348;
}
```

Literal pool of `FUN_8010e27c` (0x8010e30c–0x8010e34c):
| Pool addr | Value | Label |
|-----------|-------|-------|
| `0x8010e310` | `0x8012ae8c` | dispatch struct double-ptr |
| `0x8010e314` | `0x8010dfb1` | `patch_replaces->assoc_w_tLMP` |
| `0x8010e318` | `0x8010cc95` | `patch_replaces->unknown_fptr_index0` |
| `0x8010e31c` | `0x8010da71` | `patch_replaces->assoc_w_tLC_TX` |
| `0x8010e320` | `0x8010d9f5` | `patch_replaces->assoc_w_tLC_RX` |
| `0x8010e324` | `0x8010daa5` | `patch_replaces->assoc_w_tHCI_EVT` |
| `0x8010e328` | `0x8010d155` | `patch_replaces->assoc_w_tLMP_CH__passthrough` |
| `0x8010e32c` | `0x8010af71` | `FUN_8010af70` (sub-installer #1) |
| `0x8010e330` | `0x8010d2e9` | `FUN_8010d2e8` |
| `0x8010e334` | `0x80122518` | RAM data slot (write target) |
| `0x8010e338` | `0x8010c7b5` | `FUN_8010c7b4` |
| `0x8010e33c` | `0x801216e8` | RAM data slot (write target) |
| `0x8010e340` | `0x8003bd95` | ROM `FUN_8003bd94` |
| `0x8010e344` | `0x8003bbf1` | ROM `VSC_0xfd49_FUN_8003bbf0` |
| `0x8010e348` | `0x8010a64d` | `FUN_8010a64c` |
| `0x8010e34c` | `0x80120dc0` | RAM data slot (write target) |

---

## The 6 Protocol Dispatch Handlers

### `assoc_w_tLMP` — FUN_8010dfb0 (530 bytes)

LMP protocol message interceptor. Dispatches on internal opcode `param_1[2]`:

| Opcode | Meaning | Action |
|--------|---------|--------|
| `0x480` | SSP confirm/verify (LMP_SIMPLE_PAIRING_CONFIRM class) | Handles crypto state; checks `conn_rec._x58` for values `0x1c0e`/`0xc`; conditionally clears/sets feature page bit 5 |
| `0x489` | SSP related (LMP_DHKey variant) | Same crypto state path |
| `0x270` | eSCO link state (`0x138`) | Checks conn flags; if pending eSCO (`+0x8f & 0x10`) with no timers → calls `FUN_8010b5d8` (eSCO activator) |
| `0x259` | LMP_ACCEPTED (opcode `0x12c`) | If special state: sets dword in struct at `+0x10` = 3 |
| `0x26f` | eSCO timeout (`0x137`) | If AFH cap conditions met: calls ROM `FUN_80055204` (disable scan) + `FUN_800512a4`(3,0) |
| (fall-through) | All other opcodes | Tail-calls ROM `PTR_assoc_w_tLMP_1_8010e1f8` (original LMP handler) |

Post-call cleanup: if saved flag + opcode 0x480/0x489 → calls `FUN_8010d37c`(conn_handle).
Returns after: sets feature page bit 5 back if flag.

### `unknown_fptr_index0` — FUN_8010cc94 (26 bytes)

Simple sequenced caller:
```c
void FUN_8010cc94(void *param_1) {
    (*DAT_8010ccb0)();         // call hook fn (no args)
    (*DAT_8010ccb4)(param_1);  // call original handler
}
```
Pre-hook pattern: runs a side-effect before delegating to original.

### `assoc_w_tLC_TX` — FUN_8010da70 (44 bytes)

Link Controller TX interceptor. Dispatches on `param_1[2]`:

| Opcode | Meaning | Action |
|--------|---------|--------|
| `0x32e` | eSCO TX frame | Calls `(*DAT_8010da9c)(buf[0x19], buf[0x1a], &stack_buf)` — codec encode; if returns 0, skip |
| (fall-through) | All other LC TX | Calls `(*DAT_8010daa0)(param_1)` — original LC_TX handler |

Intercepts the eSCO codec transmit path for codec type `0x32e`.

### `assoc_w_tLC_RX` — FUN_8010d9f4 (98 bytes)

Link Controller RX interceptor. Dispatches on `param_1[2]`:

| Opcode | Meaning | Action |
|--------|---------|--------|
| `0x2cd` | eSCO RX frame | Looks up conn by handle from `PTR_DAT_8010da58`; reads `conn_rec+0x50`; if not -1: saves it, zeroes to -1, calls original then post-hook |
| (fall-through) | All other LC RX | Calls `(*DAT_8010da68)(param_1)` — original LC_RX handler |

Saves and clears a timer/state field at `conn_rec+0x50` around the original handler call.

### `assoc_w_tHCI_EVT` — FUN_8010daa4 (518 bytes)

HCI event interceptor — the most complex. Dispatches on first byte `*param_1[0]`:

| HCI event | Code | Action |
|-----------|------|--------|
| Command Complete | `0xe` | Sub-dispatch on HCI opcode (bytes 3-4): `0x402` → if payload[5]==0xc and scan flag clear → cancel scan; else clear scan flag; `0x2002` → patch payload bytes[6:7] = `0x1b, 0x00` |
| Inquiry Complete | `0x1` | If scan state active: calls `VSC_0xfc95_called2` (AFH scan complete), then calls `FUN_8010dd10(handle, count*0x500)`; fires `FUN_8010dd14(HCI_cmd, *param_1)` |
| Connection Complete | `0x3` | Interrupt-masked: calls `(*DAT_8010dcf0)(opcode, 1)` (register conn event); calls enable_interrupts |
| Disconnect Complete | `0x5` | Calls `(*DAT_8010dcf8)(pbVar8)` |
| Auth Complete | `0x7`/`0x4` | `0x4`→calls `(*DAT_8010dcf8)`, `0x7` → handled specially |
| LE Meta | `0x3e` | Sub-event 1/10→`uVar7=1`; sub-event 2/13→check counter; sub-event 3→`uVar7=0`; calls `(*pcVar5)(pbVar8, uVar7)` |
| `0x2c` | Mode Change | Interrupt-masked register write |
| `0x14` | Role Change | calls `(*DAT_8010dcfc)(pbVar8)` |
| `0x53` | Unknown | If `*PTR_scan_flag == '\0'` → calls `(*DAT_8010dce8)()` |
| (fall-through) | All others | Falls to `(*DAT_8010dd18)(param_1)` — original HCI_EVT handler |

All paths ultimately call `(*DAT_8010dd18)(param_1)` as the original HCI event handler.

### `assoc_w_tLMP_CH__passthrough` — FUN_8010d154 (16 bytes)

Pure passthrough:
```c
void FUN_8010d154(void) {
    (*DAT_8010d164)();  // call original LMP_CH handler
}
```
No interception — simply delegates to original ROM handler.

---

## Relationship to Address-Pair Table (file 0xa0)

The address-pair table at file offset 0xa0 (runtime `0x801000A0`) is a **separate
mechanism** from the 6 protocol dispatch handlers above.

The old analysis incorrectly claimed that `FUN_8010e27c` processes this table.
In reality:
- `FUN_8010e27c` writes directly to struct offsets — it does NOT iterate the table
- The DATA BLOCK (Kovah's runtime snapshot) shows address `0x801000A0` as all zeros
- The fn_ptrs listed in the old analysis (0x8010A174, etc.) point INTO the code body
  of `FUN_8010a000` — they are NOT callable function entry points (they lack prologues)

**Current status (2026-06-10)**: **INTENTIONAL OMIT** for libre firmware. GZF
`FindTableProcessor.java` found zero consumers; DATA block zeros at runtime;
hardware connect OK without the table. See
`reverse_engineering_address_pair_table_omit.md` for full verdict.

---

## Old "Handler Stubs" Region — Corrected

The old analysis identified `0x8010A160–0x8010A34C` as a "MIPS16e handler stubs"
region (36 × 8-byte `bteqz/lw/bteqz/addiu` entries). This was a **misidentification**.

The GZF DATA BLOCK shows that `FUN_8010a000` (the patch entry function, 578 bytes)
spans `0x8010a000–0x8010a241`. The bytes at `0x8010A160–0x8010A241` are the **code
body of FUN_8010a000**, not a separate dispatch table.

The repeating 8-byte patterns in that region are standard MIPS16e compare-and-branch
instruction sequences used in the large installer. Ghidra failed to decode these in
old import mode due to `EXTEND pcode errors`, but in GZF PROCESS MODE (with full
memory context) the function decompiles correctly.

The literal pool of `FUN_8010a000` starts at `0x8010a244` (after the 578-byte body).

---

## Libre Replacement Implications

### What must be provided

| Item | Status | Notes |
|------|--------|-------|
| `FUN_8010e27c` (string_assoc_installer) | **Required** | Installs 6 protocol handlers into dispatch struct |
| `FUN_8010dfb0` (assoc_w_tLMP) | **Required** | 530-byte LMP interceptor (SSP + eSCO) |
| `FUN_8010cc94` (unknown_fptr_index0) | **Required** | 26-byte pre-hook caller |
| `FUN_8010da70` (assoc_w_tLC_TX) | **Required for eSCO audio** | 44-byte codec TX intercept |
| `FUN_8010d9f4` (assoc_w_tLC_RX) | **Required for eSCO audio** | 98-byte codec RX intercept |
| `FUN_8010daa4` (assoc_w_tHCI_EVT) | **Required** | 518-byte HCI event interceptor |
| `FUN_8010d154` (assoc_w_tLMP_CH__passthrough) | **Required (passthrough only)** | 16-byte delegator |
| BB register init (0x40, 0x6b, 0x6c) in `FUN_8010e27c` | **Required** | HW configuration |
| Address-pair table at `0x801000A0` | **UNRESOLVED** | Purpose unclear |

### BB register init (HW config in FUN_8010e27c)

- Reg `0x40`: write 2 (enable mode), then write 0 (reset) — likely clock or mode enable
- Reg `0x6b`: set bits 15,13 (mask: `0x1fff`, OR: `0xa000`)
- Reg `0x6c`: set bits 2,0 (mask: `~7`, OR: `5`)

Purpose: likely enables specific LMP/eSCO features in the baseband controller.
The registers 0x6b/0x6c are distinct from the AFH and codec registers seen elsewhere.

### Handler tail-calls

All 6 handlers end by calling the **original ROM handler** (stored in their respective
literal pool). The libre implementation must:
1. Obtain the ROM function pointer (from ROM at the known address)
2. Store it in the correct literal pool slot
3. Call it as the tail-call in each handler

The original ROM handlers are identified by Kovah's naming:
- `PTR_assoc_w_tLMP_1_8010e1f8` → ROM LMP handler
- `DAT_8010daa0` → original LC_TX handler
- `DAT_8010da68` → original LC_RX handler
- `DAT_8010dd18` → original HCI_EVT handler
- `DAT_8010d164` → original LMP_CH handler
