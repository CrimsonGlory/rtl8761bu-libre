# Reverse Engineering: `FUN_8010bba4` — The LMP VSC Hook

**Runtime address**: `0x8010bba4`  
**Memory block in Kovah's .gzf**: `data [80100000 - 8013ffff]` (runtime-addressed copy of patch)  
**File-offset equivalent**: `0x0000bba4` — past the `patch [00000000 - 0000adc3]` block, so this function is only in the runtime-addressed block  
**Size**: 176 bytes  
**Installed by**: `thing_that_calls_thing_that_installs_LMP_Patch` at `0x80103780` → stored at `bos_base+0xd8`  
**Triggered by**: ROM `LMP__268__most_common_for_VSCs2_checks_fptr_patch` at `0x80009a6c` — the ROM function that checks this pointer for every incoming LMP VSC PDU  
**Source**: Kovah `2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf`, decompiled via Ghidra 12.1.2 headless  
**Tool used**: `DecompileAddr.java` with `-noanalysis` flag, address `0x8010bba4`

---

## Ghidra Project Layout Discovery

Kovah's `.gzf` contains **three memory blocks**:

| Block name | Range | Content |
|-----------|-------|---------|
| `patch` | `[00000000 - 0000adc3]` | Patch firmware at file-relative addresses (execute=true) |
| `data` | `[80100000 - 8013ffff]` | Patch firmware at RUNTIME addresses (execute=true) |
| (ROM) | `[80000000 - ~8003ffff]` | ROM dump at runtime addresses |

The `patch` block at `0x00000000` is where Kovah defined all FUNCTION symbols (including `thing_that_calls_thing_that_installs_LMP_Patch` at `0x00003780`). The `data` block at `0x80100000` has the same bytes but instructions only — no function boundaries were created there. This is why `0x8010bba4` required `createFunction()` at runtime: instructions existed, but no function marker.

---

## Summary

`FUN_8010bba4` is the **LMP VSC gateway function**. The ROM calls it whenever an incoming LMP PDU matches the VSC opcode hook installed at `bos_base+0xd8`. It is not the VSC handler itself — it is the outer dispatcher that:

1. Acquires a shared resource (channel/buffer allocation)
2. Checks a "globally busy" flag in the large connection state array
3. Looks up the connection record matching the incoming PDU
4. Temporarily modifies two 16-bit fields in the connection record
5. Dispatches to the **actual VSC handler** via `(*DAT_8010bc64)(pdu, output_buf)`
6. Restores the modified connection fields
7. Builds and sends a 5-byte LMP response PDU

This design separates resource management and connection bookkeeping (done here) from opcode decoding and execution (done by `DAT_8010bc64`).

---

## Annotated Decompilation

```c
void FUN_8010bba4(undefined2 *param_1)   // param_1 = pointer to incoming LMP PDU
{
  int   iVar1;           // multi-purpose temp / return value
  uint  uVar2;           // result/status code (sent back in LMP response)
  // Stack frame layout (sp+0x40 at entry):
  //   sp+0x10:  auStack_30[0]   — output buffer byte 0 (filled by VSC dispatch)
  //   sp+0x11-13: auStack_30[1..3] — more output bytes
  //   sp+0x14-16: auStack_2c[0..2] — response PDU header (3 bytes, filled by bc6c)
  //   sp+0x17:  uStack_29       — result code byte in response PDU
  //   sp+0x18:  uStack_28       — output byte 0 in response PDU
  //   sp+0x1c:  iStack_1c       — connection handle (or flag: 0=none, 1=found)
  //   sp+0x20:  uStack_20       — saved conn_record[+0x2e] (u16)
  //   sp+0x24:  (temp used by asm)
  //   sp+0x34:  saved s0
  //   sp+0x38:  saved s1
  //   sp+0x3c:  saved ra

  auStack_30[0] = 0;                          // zero the output buffer

  // === PHASE 1: Allocate shared resource (type=2) ===
  iVar1 = (*DAT_8010bc54)(2);                 // alloc; arg 2 = resource type or channel
  if (iVar1 == 0 ||                           // allocation failed
      (PTR_base_of_0x1ac_struct_array_0xA_large2_8010bc58[0x28] & 1) != 0) {
    // "busy" flag (bit 0 of conn_array[0x28]) is set: device is processing another VSC
    uVar2 = 0x0c;  // BT reason code 12 = "Command disallowed"
    goto send_response;
  }

  // === PHASE 2: Look up connection record from PDU ===
  iStack_1c = (*DAT_8010bc5c)(param_1);       // resolve connection from PDU; returns handle or 0
  uVar2 = 0x12;  // BT reason code 18 = "Connection rejected, limited resources"
  if (iStack_1c == 0) {
    goto send_response;                        // no connection found — reject
  }

  // === PHASE 3: Look up connection record entry by LMP handle (PDU byte 3) ===
  iVar1 = (*DAT_8010bc60)(*(byte *)((int)param_1 + 3));
  //   PDU byte 3 = LMP transaction handle / link identifier
  //   Returns pointer to the per-link connection state record, or 0

  if (iVar1 == 0) {
    // No per-link record — dispatch anyway with empty saved state
    iStack_1c = 0;
    uStack_20 = 0;
    uStack_18 = 0;
  } else {
    // === PHASE 4: Save and temporarily modify connection record fields ===
    uStack_18 = (uint)*(uint16_t *)(iVar1 + 0x2c);  // save conn_rec[+0x2c] (e.g. seq/ack counter)
    if (uStack_18 != 0) {
      *(int16_t *)(iVar1 + 0x2c) = (short)iStack_1c; // overwrite with connection handle
    }
    uStack_20 = (uint)*(uint16_t *)(iVar1 + 0x2e);  // save conn_rec[+0x2e] (e.g. LMP opcode)
    iStack_1c = 1;                                   // mark: record found
    if (uStack_20 != 0) {
      *(uint16_t *)(iVar1 + 0x2e) = 0;              // zero conn_rec[+0x2e] temporarily
    }
  }

  // === PHASE 5: Dispatch to actual VSC handler ===
  uVar2 = (*DAT_8010bc64)(param_1, auStack_30);
  //   param_1  = full incoming LMP PDU
  //   auStack_30 = output buffer (up to 4 bytes of response data)
  //   returns: status/result code (goes into response PDU)

  // === PHASE 6: Restore connection record ===
  if (iStack_1c != 0) {                             // if per-link record was found
    *(int16_t  *)(iVar1 + 0x2c) = (short)uStack_18; // restore conn_rec[+0x2c]
    *(uint16_t *)(iVar1 + 0x2e) = (uint16_t)uStack_20; // restore conn_rec[+0x2e]
  }

send_response:
  // === PHASE 7: Send LMP response PDU ===
  iStack_1c = uVar2;                           // final result code
  (*DAT_8010bc68)();                           // unknown: possibly release lock or yield

  // Build 3-byte response PDU header from incoming PDU opcode word:
  (*DAT_8010bc6c)(*param_1, auStack_2c);       // mirror TID bit, fill opcode in response header

  // Pack result code and output byte into the response buffer:
  uStack_29 = (byte)iStack_1c;                 // byte 4 of response = status code
  uStack_28 = auStack_30[0];                   // byte 5 of response = VSC output byte 0

  // Send 5-byte LMP PDU:
  (*DAT_8010bc70)(0xe, auStack_2c, 5);
  //   0xe = 14: connection handle / link type indicator (not an LMP opcode)
  //   auStack_2c: pointer to 5-byte response buffer (sp+0x14)
  //   5: payload length

  return;
  // NOTE: Ghidra warns "Could not recover jumptable at 0x8010bc50"
  // This is a false alarm — the jr a3 epilogue is MIPS16e's return-via-saved-ra convention.
  // ra was stored to s-register area, returned via a3 after restore.
}
```

---

## Phase-by-phase Analysis

### Phase 1 — Resource allocation via `DAT_8010bc54` (address `0x8010bc54`)

```asm
li   a0, 2
jalr v0          ; v0 = *DAT_8010bc54
```

The allocator is called with `2`. Based on context, this is likely one of:
- A buffer pool allocator: "alloc 2 bytes" (minimum for an LMP PDU opcode)
- A channel/context slot allocator: "acquire slot type 2" (VSC processing context)

If it returns 0 (failure), the function bails with reason code `0x0c` (Command disallowed) before doing any work. This prevents re-entrant VSC processing.

### Phase 2 — "Globally busy" guard

```c
PTR_base_of_0x1ac_struct_array_0xA_large2_8010bc58[0x28] & 1
```

Kovah's name for this global: `PTR_base_of_0x1ac_struct_array_0xA_large2`. This is the connection state array at `0x8012dc50` (the "bos" struct). Byte at offset `0x28` has bit 0 as a "VSC processing in progress" or "command pending" flag. If set, the device is already processing a vendor command and must reject the new one. This is the RTL8761BU's single-VSC-at-a-time serialization mechanism.

Reason code `0x0c` (12 decimal) = BT spec error code **"Command Disallowed"** — appropriate for a re-entrancy rejection.

### Phase 3 — Connection lookup via `DAT_8010bc5c`

```c
iStack_1c = (*DAT_8010bc5c)(param_1);  // returns connection handle, or 0
```

This function examines the incoming LMP PDU (or its associated ACL handle) to find the logical connection. Returning 0 means the PDU arrived on a connection that no longer exists or was never established. Reason code `0x12` (18 decimal) = BT spec error code **"Connection Rejected due to Limited Resources"** — used here as "connection not found."

### Phase 4 — Per-link state lookup and temporary field overwrite via `DAT_8010bc60`

```c
iVar1 = (*DAT_8010bc60)(*(byte *)((int)param_1 + 3));
```

PDU byte 3 carries the LMP transaction handle (or link index). This function looks up the per-link connection record and returns a pointer to it, or 0 if not found.

The temporary modification of `conn_rec[+0x2c]` and `conn_rec[+0x2e]`:
- `conn_rec[+0x2c]` is overwritten with the connection handle, then restored afterward
- `conn_rec[+0x2e]` is zeroed temporarily, then restored

This is a **state injection** pattern: for the duration of the VSC dispatch, the connection record is patched to contain information relevant to the current command (e.g., handle, opcode). The actual VSC handler (`DAT_8010bc64`) can then read these fields without needing them as explicit arguments. After dispatch, the original values are restored so the connection record appears unchanged to other code.

### Phase 5 — VSC dispatch via `DAT_8010bc64` ← CRITICAL

```c
uVar2 = (*DAT_8010bc64)(param_1, auStack_30);
//   IN:  param_1    = pointer to incoming LMP PDU
//   IN:  auStack_30 = 4-byte output buffer (on stack)
//   OUT: uVar2      = status code (sent in response)
//        auStack_30 = filled with up to 4 bytes of VSC response data
```

This is the **actual VSC command router**. `DAT_8010bc64` points to the function that reads the VSC opcode from the PDU and dispatches to the right handler. Its address (`0x8010bc64` in the data section) is the pointer slot that `thing_that_calls_thing_that_installs_LMP_Patch` populated.

This is the next function to reverse engineer: following `DAT_8010bc64` will reveal how individual VSC opcodes (fc11, fc39, fc6c, fc95, fcc0, fcc2, fcc4, ...) are decoded and dispatched.

### Phase 6 — State restore

Simple unconditional restore if a per-link record was found. No logic variation here.

### Phase 7 — Response PDU construction and send

```c
(*DAT_8010bc68)();                        // unknown: release resource / yield scheduler
(*DAT_8010bc6c)(*param_1, auStack_2c);   // build LMP response header from incoming opcode
// Result PDU layout (5 bytes at sp+0x14):
//   [0..2]: response opcode header (built by bc6c from *param_1)
//   [3]:    status code (12, 18, or dispatch result)
//   [4]:    first byte of VSC output
(*DAT_8010bc70)(0xe, auStack_2c, 5);     // transmit 5-byte LMP PDU
```

`DAT_8010bc6c` builds the LMP response header from the incoming PDU's first word. For LMP PDUs, the response must mirror the TID (Transaction ID) bit from the request. So `*param_1` (first 2 bytes = raw LMP opcode word) is used to derive the response opcode bytes.

`DAT_8010bc70` is the LMP PDU send function. The first argument `0xe = 14` is most likely a **link/connection selector** (not an LMP opcode), since the opcode is already embedded in `auStack_2c`. A value of 14 may represent the default ACL link type for LMP VSC responses.

The final PDU always has exactly 5 bytes, regardless of the VSC's own output size. Only 1 byte of VSC output (`auStack_30[0]`) makes it into the response. This implies the LMP-based VSC path is designed for compact status responses, not for transferring bulk data.

---

## Key Data Pointers (all in `data` block `[80100000-8013ffff]`)

| Pointer address | Inferred function | Notes |
|----------------|-------------------|-------|
| `DAT_8010bc54` | Resource/buffer allocator | arg=2; returns non-zero on success |
| `PTR_base_of_0x1ac_struct_array_0xA_large2_8010bc58` | Connection state array base | `[0x28] & 1` = globally-busy flag |
| `DAT_8010bc5c` | Connection lookup from PDU | arg=PDU ptr; returns handle or 0 |
| `DAT_8010bc60` | Per-link record lookup | arg=PDU byte 3 (link index); returns conn_rec ptr or 0 |
| `DAT_8010bc64` | **VSC dispatcher** ← next RE target | arg=(PDU, outbuf); returns status; dispatches by opcode |
| `DAT_8010bc68` | Post-dispatch cleanup | no args; possibly releases resource from Phase 1 |
| `DAT_8010bc6c` | LMP response header builder | args=(*param_1, response_buf); fills 3 bytes |
| `DAT_8010bc70` | LMP PDU send | args=(0xe, buf, len=5); transmits response |

---

## Error Codes

| Value | BT Spec meaning | When returned |
|-------|----------------|---------------|
| `0x0c` (12) | Command Disallowed | Resource alloc failed, OR device already processing a VSC |
| `0x12` (18) | Connection Rejected (limited resources) | No connection found for incoming PDU |
| (from dispatch) | Varies | VSC handler's own result code |

---

## MIPS16e Notes

1. **`jr a3` return**: The epilogue restores `ra` to `a3` (`lw a3, 0x3c(sp)`), then uses `jr a3` to return. Ghidra sees `a3` (normally arg register 3) being used as the return address and emits "Could not recover jumptable" — this is a false alarm, not an actual computed jump. This is standard RTL8761BU MIPS16e calling convention.

2. **PC-relative loads** (`lw v0, 0xa0(pc)`): All function pointers are loaded from a local literal pool in the code section. Each `DAT_8010bcXX` pointer is one such literal pool entry — a 4-byte word in the `data` block that holds a runtime address. This is the MIPS16e equivalent of position-independent code.

3. **Delay slots**: All `jalr`/`jr` instructions have a delay-slot instruction prefixed with `_`. The delay slot executes before the branch takes effect. In several places the argument is set in the delay slot (e.g., `jalr v0 / _li a0, 0x2` means a0=2 is set as the call happens).

---

## Implications for the Libre Replacement

The LMP VSC path has two layers:

```
ROM: LMP__268__most_common_for_VSCs2_checks_fptr_patch (0x80009a6c)
  → checks bos_base+0xd8 fptr
    → FUN_8010bba4 (this function): gateway/dispatcher wrapper
         → *DAT_8010bc64: actual VSC opcode router  ← NEXT target
              → individual VSC handlers (fc11, fc39, fc6c, fc95, fcc0, etc.)
```

A libre replacement must implement this entire call chain. The gateway function (`FUN_8010bba4`) itself is relatively straightforward — mostly bookkeeping. The hard part is `DAT_8010bc64` and the individual VSC handlers. Understanding `DAT_8010bc64` is the next step.

The globally-busy flag at `bos_array[0x28]` must be respected: the libre implementation needs to maintain this flag correctly to prevent re-entrant VSC processing.

The temporary modification of `conn_rec[+0x2c]` and `conn_rec[+0x2e]` during dispatch suggests these fields are consulted by the VSC handlers. A libre implementation must replicate this state injection or explicitly pass these values as arguments.

---

## Next Steps

1. **Decompile `DAT_8010bc64`** (the VSC opcode router) — this reveals the full table of supported VSC opcodes
2. **Decompile `DAT_8010bc5c`** (connection lookup) — determines how the LMP PDU maps to a connection
3. **Cross-reference with `VSC_0xfc11_*`, `VSC_0xfc39_*` etc.** already documented in `kovah_function_list.md` — these are ROM functions, but the patch may override them via `DAT_8010bc64`'s dispatch table
