# PASS 5 Execution Instructions — Region 0x80020000

**Date Prepared:** 2026-06-24  
**Status:** Ready for MCP Execution  
**Target:** Batch decompile 22 event-sender + utility functions in region 0x80020000  
**Expected Outcome:** Upgrade 22 functions from LOW/MEDIUM to HIGH confidence

## Overview

PASS 5 continues region 0x80020000 after PASS 3–4 completed the LMP encryption/pairing procedures (16 functions) and HCI command router documentation.

The next strategic cluster is the **event-sender family** (`send_evt_HCI_*` functions) and **LMP utility wrappers** — all Kovah-named, all currently LOW or MEDIUM confidence.

## Batch Addresses (22 functions)

**File location:** `/tmp/pass5_batch_addresses.txt` (created by overnight harness)

**Addresses (in execution order, by priority):**

```
0x8002271c  send_evt_HCI_Encryption_Change[v1] (60B)
0x8002275c  send_evt_HCI_Change_Connection_Link_Key_Complete (52B)
0x80022794  send_evt_HCI_Link_Key_Notification (278B)
0x800228b4  send_evt_HCI_Authentication_Complete_0x06 (52B)
0x800228ec  send_evt_HCI_Return_Link_Keys (100B)
0x80022c40  send_evt_HCI_PIN_Code_Request (66B)
0x80022eec  many_sub_if_else_cases_on_param2 (104B)
0x80022f54  send_evt_HCI_Link_Key_Request (66B)
0x80023ba4  send_evt_HCI_Encryption_Key_Refresh_Complete (52B)
0x80023c4c  send_evt_HCI_Keypress_Notification (64B)
0x80023c90  send_evt_HCI_Simple_Pairing_Complete_0x36 (52B)
0x80023cc8  send_evt_HCI_IO_Capability_Response (72B)
0x80023e38  send_evt_HCI_User_Passkey_Notification (70B)
0x80023e84  send_evt_HCI_Remote_OOB_Data_Request (66B)
0x80023ecc  send_evt_HCI_User_Passkey_Request (66B)
0x80023f14  send_evt_HCI_User_Confirmation_Request (84B)
0x80023f6c  send_evt_HCI_IO_Capability_Request (66B)
0x8002442c  wrap_send_LMP_NOT_ACCEPTED (62B)
0x8002469c  wrap_send_LMP_ACCEPTED_and_some_other_things (92B)
0x80024bd8  copy_fields_within_crypto_struct (48B)
0x80022030  LMP__266__FUN_80022030 (86B)
0x80025cb4  LMP__271__FUN_80025cb4 (118B)
```

## Execution Steps

### Step 1: Decompile all 22 functions via MCP

Command template (invoke 22 times, one address per call):

```bash
mcp__wairz__run_ghidra_headless \
  --binary_path "2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf" \
  --script_name "DecompileAddr.java" \
  --script_args "0x8002271c" \
  --use_saved_project true
```

**Or batch via custom script:**

Use `BatchDecompileRegion80020000Pass5.java` (created in `/root/wairz/ghidra/scripts/`):

```bash
mcp__wairz__run_ghidra_headless \
  --binary_path "2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf" \
  --script_name "BatchDecompileRegion80020000Pass5.java" \
  --use_saved_project true \
  --timeout 600
```

Expected output: Full decompiled C code for all 22 functions, with size/purpose confirmations.

### Step 2: Analyze decompile results

For each function, extract:
- Exact size in bytes (should match rom_function_index.md)
- Purpose/behavior (one-line summary)
- Calling conventions + argument types
- Key ROM calls or patterns observed

Expected pattern for send_evt_HCI_* functions:
- Parameter 1: opcode (u16) or param struct pointer
- Parameter 2–N: PDU fields to pack
- Call: ROM `send_HCI_event` or wrapper (via literal pool)
- Return: void or status code

Expected pattern for wrap_send_LMP_* functions:
- Build LMP PDU from parameters
- Call ROM `send_LMP_pkt`
- Return with status

### Step 3: Rename functions in GZF project

After decompile, create a rename script:

```bash
# Format: 0xADDR=NewName;0xADDR=NewName;...
# Example:
0x8002271c=send_evt_HCI_Encryption_Change_wrapper;
0x8002275c=send_evt_HCI_Change_Connection_Link_Key_Complete_wrapper;
...
```

Use `RenameFunctionsPass5.java` (to be created by harness or manually):

```bash
mcp__wairz__run_ghidra_headless \
  --binary_path "2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf" \
  --script_name "RenameFunctionsPass5.java" \
  --use_saved_project true
```

### Step 4: Update documentation

1. Add decompile details to `reverse_engineering_region_0x80020000.md` under "## Pass 5 Results"
2. Update `rom_function_index.md` with 22 new rows:
   - Confidence: `high (decompiled+documented, Pass 5 2026-06-24)`
   - Purpose: one-line from decompile
   - Size: exact from decompile
   - Cross-ref: `region_0x80020000` Pass 5

3. Update `analysis/INDEX.md` summary table for region 0x80020000

### Step 5: Mark ticket [DONE] + promote next

In `work-in-progress.txt`:

```
[DONE]  Full RE: ROM region 0x80020000-0x8002ffff PASS 5 (decompiled 22 event-sender + utility functions) (2026-06-24)
        PASS 1–4: enumeration, LMP encryption/pairing staging, batch decompile (16), HCI router doc
        PASS 5: decompiled send_evt_HCI_* cluster (17 functions) + LMP utility wrappers (5 functions)
        All 22 upgraded to HIGH confidence. Remaining: ~300 unnamed functions (LMP non-encryption,
        subsystem utilities, data processors). Cold-triage strategy documented for PASS 6.
        → analysis/rom/reverse_engineering_region_0x80020000.md (Pass 5 section added)
        → rom_function_index.md (22 new HIGH rows)

[NEXT]  Full RE: ROM region 0x80020000-0x8002ffff PASS 6+ (continue until 0 unnamed remain, ~300 to resolve)
```

## Expected Timeline

- Decompile (Step 1): ~5–10 minutes (all 22 via batch script)
- Analysis + rename (Steps 2–3): ~10–15 minutes (pattern recognition on thin-wrappers)
- Documentation (Step 4): ~5–10 minutes (straightforward table updates)
- Total: ~25–35 minutes for full PASS 5 execution

## Risk Mitigation

**Risk:** One or more functions fail to decompile  
**Mitigation:** DecompileAddr.java is stable; expect 100% success rate on validated addresses from rom_function_index.md

**Risk:** Decompile yields unexpected signatures (not thin wrappers as expected)  
**Mitigation:** Document findings as-is; update purpose/naming accordingly; adjust PASS 6 strategy if needed

## Remaining Work (PASS 6+)

After PASS 5:
- ~300 completely unnamed functions remain
- Stratified by size/xrefs into 4–5 tiers
- Recommended starting point: LMP non-encryption procedures (link management, power control, AFH)
- Then: subsystem utilities, data processors, VSC handlers

See `reverse_engineering_region_0x80020000.md` "Remaining Scope" section for full breakdown.
