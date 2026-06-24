# PASS 5 Execution Summary — Region 0x80020000-0x8002ffff

**Date:** 2026-06-24  
**Status:** PREPARATION COMPLETE — Ready for Batch Decompilation  
**Target Functions:** 22 (17 event-senders + 5 LMP utility wrappers)  
**Expected Confidence Upgrade:** LOW/MEDIUM → HIGH for all 22  
**Remaining Unnamed:** ~300 (for PASS 6+)

---

## What Was Prepared

### 1. Batch Addresses (22 functions)
**Location:** `/tmp/pass5_batch_addresses.txt`  
**Format:** Plain list of hex addresses, one per line  
**Ready for:** Direct feed to `BatchDecompileRegion80020000Pass5.java` or repeated calls to `DecompileAddr.java`

### 2. Execution Script
**Location:** `/root/wairz/ghidra/scripts/BatchDecompileRegion80020000Pass5.java`  
**Invocation:**
```bash
mcp__wairz__run_ghidra_headless \
  --binary_path "2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf" \
  --script_name "BatchDecompileRegion80020000Pass5.java" \
  --use_saved_project true \
  --timeout 600
```

### 3. Detailed Execution Guide
**Location:** `/root/rtl8761bu-libre/analysis/rom/PASS5_EXECUTION_INSTRUCTIONS.md`  
**Contents:**
- Overview of PASS 5 objectives
- Complete list of 22 target addresses with names/sizes
- Step-by-step execution workflow
- Expected output patterns
- Risk mitigation strategies
- Timeline estimate: 25–35 minutes total

### 4. Updated Analysis Documentation
**Location:** `/root/rtl8761bu-libre/analysis/rom/reverse_engineering_region_0x80020000.md`  
**New Section:** "## Pass 5 (2026-06-24) — Event-Sender Cluster & Utility Triage (IN PROGRESS)"  
**Content:**
- Execution plan
- Target addresses with priority levels
- Rationale for cluster selection
- Expected decompile patterns
- Placeholder for results

### 5. Work-in-Progress Tracking
**Location:** `/root/rtl8761bu-libre/work-in-progress.txt`  
**Updated [NEXT] Entry:**
```
[NEXT]  Full RE: ROM region 0x80020000-0x8002ffff PASS 5 (decompile 22 event-sender+utility functions) (2026-06-24)
        PREPARATION COMPLETE. Ready for batch MCP decompilation...
        Blocking on: MCP invocation of batch script...
```

---

## Function Breakdown

### Event-Sender Cluster (17 functions, all @ 0x80021xxx–0x80023xxx)
All follow the pattern: **read HCI event fields → pack into PDU → call ROM sender → return**

```
0x8002271c  send_evt_HCI_Encryption_Change[v1]
0x8002275c  send_evt_HCI_Change_Connection_Link_Key_Complete
0x80022794  send_evt_HCI_Link_Key_Notification
0x800228b4  send_evt_HCI_Authentication_Complete_0x06
0x800228ec  send_evt_HCI_Return_Link_Keys
0x80022c40  send_evt_HCI_PIN_Code_Request
0x80022eec  many_sub_if_else_cases_on_param2
0x80022f54  send_evt_HCI_Link_Key_Request
0x80023ba4  send_evt_HCI_Encryption_Key_Refresh_Complete
0x80023c4c  send_evt_HCI_Keypress_Notification
0x80023c90  send_evt_HCI_Simple_Pairing_Complete_0x36
0x80023cc8  send_evt_HCI_IO_Capability_Response
0x80023e38  send_evt_HCI_User_Passkey_Notification
0x80023e84  send_evt_HCI_Remote_OOB_Data_Request
0x80023ecc  send_evt_HCI_User_Passkey_Request
0x80023f14  send_evt_HCI_User_Confirmation_Request
0x80023f6c  send_evt_HCI_IO_Capability_Request
```

### LMP Utility Wrappers (5 functions)
**Patterns:**
- `wrap_send_LMP_*`: Build LMP PDU, call ROM `send_LMP_pkt`, return status
- `copy_fields_within_crypto_struct`: Struct field copier
- `LMP__266__FUN_80022030`, `LMP__271__FUN_80025cb4`: Procedure handlers (TBD pattern)

```
0x8002442c  wrap_send_LMP_NOT_ACCEPTED
0x8002469c  wrap_send_LMP_ACCEPTED_and_some_other_things
0x80024bd8  copy_fields_within_crypto_struct
0x80022030  LMP__266__FUN_80022030
0x80025cb4  LMP__271__FUN_80025cb4
```

---

## Next Harness Actions (After MCP Execution)

1. **Analyze Decompile Results** (~10 min)
   - Verify all 22 functions decompiled successfully
   - Confirm expected patterns (thin wrappers)
   - Extract one-line purposes and exact sizes

2. **Rename Functions in GZF** (~5 min)
   - Create `RenameFunctionsPass5.java` script
   - Use auto-generated rename patterns based on decompile analysis
   - Example: `0x8002271c=send_evt_HCI_Encryption_Change_wrapper;...`

3. **Update Documentation** (~10 min)
   - Add Pass 5 Results section to `reverse_engineering_region_0x80020000.md`
   - Update `rom_function_index.md` with 22 new HIGH-confidence rows
   - Update `analysis/INDEX.md` summary table

4. **Close PASS 5 Ticket** (~5 min)
   - Mark [NEXT] → [DONE] with date + summary
   - Promote first [TODO] to [NEXT]
   - Create PASS 6 recommendations based on remaining ~300 unnamed functions

---

## Success Criteria

✓ All 22 functions successfully decompiled (expected 100% success rate)  
✓ All 22 upgraded to HIGH confidence  
✓ All 22 documented in rom_function_index.md with one-line purposes  
✓ All 22 cross-referenced in region_0x80020000.md Pass 5 section  
✓ Decompile patterns match expected thin-wrapper model  
✓ Zero regression in existing HIGH-confidence entries  

---

## If Decompilation Fails

**Common failure:** Address is not a function (e.g., data or code padding)  
**Remediation:** Remove from list, investigate manually in Ghidra, update this doc

**Unlikely failure:** DecompileAddr.java script error  
**Remediation:** Fall back to manual DecompileAddr.java one-address-at-a-time invocation

See PASS5_EXECUTION_INSTRUCTIONS.md for full risk mitigation strategies.

---

## Related Documents

| File | Purpose |
|------|---------|
| `analysis/rom/reverse_engineering_region_0x80020000.md` | Main region doc, now includes PASS 5 draft |
| `analysis/rom/PASS5_EXECUTION_INSTRUCTIONS.md` | Step-by-step execution guide (comprehensive) |
| `/tmp/pass5_batch_addresses.txt` | 22 target addresses, one per line |
| `/root/wairz/ghidra/scripts/BatchDecompileRegion80020000Pass5.java` | Batch script |
| `work-in-progress.txt` | [NEXT] ticket status |

---

## Estimated Timeline (Full Execution)

- **MCP Decompilation**: 5–10 min
- **Analysis + Rename**: 10–15 min  
- **Documentation**: 5–10 min
- **Ticket Close + Next Promotion**: 5 min
- **Total**: 25–35 minutes

---

**Prepared by:** Overnight wip-loop worker (2026-06-24)  
**Status:** Ready for parent harness MCP execution
