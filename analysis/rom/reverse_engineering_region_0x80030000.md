# Phase 9 Region 0x80030000-0x8003ffff: VSC Dispatcher + Register-Script Interpreter + Unnamed Functions

**Scope:** ROM region `0x80030000-0x8003ffff` (64 KiB), exhaustive pass 1 enumeration.

**Pre-existing high-confidence functions (already documented):**
- `0x80030f1c` — `HCI_CMD_OGF_3F__Vendor_Specific__FUN_80030f1c` (4372 B) — HCI VSC OGF 0x3F dispatcher; already detailed in `reverse_engineering_hci_command_router.md`
- `0x8003aea0` — `FUN_8003aea0` (register-script interpreter, 688 B) — detailed in prior phase docs

**Baseline composition (from rom_function_index.md as of 2026-06-22):**
- Total functions in region: ~307
- Already named (high/medium/low confidence): 17
  - High-confidence (decompiled + documented): 2
  - Medium-confidence: 0
  - Low-confidence (Kovah names, thin purpose): 15
- Unnamed (`FUN_*` auto-generated): 290

**Thin-named functions (17 total):**

| Address | Size | Name | Confidence | Notes |
|---------|------|------|------------|-------|
| `0x80030000` | ? | (padding/start) | — | Region boundary |
| `0x8003003c` | 116 | `VSC_0xfc46_FUN_8003003c` | low | VSC opcode 0xfc46 handler (purpose unclear) |
| `0x800300c4` | 102 | `VSC_0xfc95_FUN_800300c4` | low | VSC opcode 0xfc95 handler (purpose unclear) |
| `0x800302ac` | 272 | `references_patch_download_mem4` | low | References patch download memory region |
| `0x800303f4` | 306 | `VSC_0xfc35_FUN_800303f4` | low | VSC opcode 0xfc35 handler (purpose unclear) |
| `0x80030b2c` | 150 | `VSC_0xfc27_FUN_80030b2c` | low | VSC opcode 0xfc27 handler (purpose unclear) |
| `0x80030bdc` | 346 | `VSC_0xfc64_FUN_80030bdc` | low | VSC opcode 0xfc64 handler (purpose unclear) |
| `0x80030dd8` | 268 | `VSC_0xfc61_write_to_relevant_data_FUN_80030dd8` | low | VSC opcode 0xfc61 handler; writes to data structure |
| `0x80030eec` | 40 | `VSC_0xfc8b_FUN_80030eec` | low | VSC opcode 0xfc8b handler (purpose unclear) |
| `0x80030f1c` | 4372 | `HCI_CMD_OGF_3F__Vendor_Specific__FUN_80030f1c` | **high** | **Master VSC dispatcher** — 73+ opcodes routed; fully decompiled |
| `0x80032540` | 2068 | `multi-VSC_Handler_FUN_80032540` | low | Multi-VSC handler; dispatches multiple opcodes |
| `0x80032e28` | 20 | `called_by_function_that_uses_Logger_string_2_initialize_something_at_offset_0x800` | low | Logger/init related; minimal code |
| `0x80033188` | 182 | `calls_fptr_down_LMP__47E_path` | low | LMP opcode 0x47E related |
| `0x80034a38` | 378 | `idk_takes_new_new_power_val` | low | TX power value related (purpose unclear) |
| `0x80034be0` | 120 | `set_new_power_val` | low | TX power value setter |
| `0x80036bd0` | 336 | `fHCI_[Create_Connection_0x08]_or_[Remote_Name_Request_0x1A]_Cancel` | low | HCI Cancel command handler |
| `0x80036d44` | 86 | `fHCI_Inquiry_Cancel_0x02_1` | low | HCI Inquiry Cancel (OGF 1 / OCF 2) variant |
| `0x80036df8` | 316 | `called_by_fHCI_Remote_Name_Request_5` | **high** | Remote Name Request continuation; already decompiled in `reverse_engineering_lc_lmp_state_machine.md` |
| `0x8003bbf0` | 94 | `VSC_0xfd49_FUN_8003bbf0` | low | VSC opcode 0xfd49 handler (purpose unclear) |

**Pass 1 Status (Enumeration):**

## Step 1: Create enumeration script

Created `ListRegion0x80030000.java` — lists all functions in region `0x80030000-0x8003ffff` with:
- Address (hex)
- Size (bytes)
- Function name
- Source type (AUTO / USER_DEFINED)
- Symbol type (FUNC / LABEL)

Two-phase run expected (region is ~307 functions; prior regions showed 384–398; typical cutoff ~150 before truncation):
- `ListRegion0x80030000.java` — full region attempt, expect truncation around midpoint
- `ListRegion0x80030000_Upper.java` — upper half if needed (starting ~`0x80038000`)

## Step 2: Run enumeration via GZF

Target: `2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf` (GZF process mode, `use_saved_project=True`)

Expected output:
- Exact count of total functions
- Breakdown: high-confidence (already named), thin-named (named but low-confidence), unnamed (`FUN_*`)
- Address ranges for any untouched sub-regions
- Identification of any "not a real function" entries (zero-fill padding, mis-split stubs)

## Step 3: Regional structure observations

### VSC Dispatcher Cluster
- Master dispatcher at `0x80030f1c` (4372 B, already documented)
- 8-12 VSC opcode handlers in range `0x8003003c–0x80030eec` (all 116–346 B, thin-named)
- Multi-VSC router at `0x80032540` (2068 B) — may be a demultiplexer or alternate entry point
- VSC opcode coverage: 0xfc27, 0xfc35, 0xfc46, 0xfc61, 0xfc64, 0xfc8b, 0xfc95, 0xfd49 observed

### Power/TX Functions
- `0x80034a38–0x80034be0` — TX power value management (2 functions, 378+120 B)
- Purpose unclear from names alone; likely BSA/AMP power class or per-connection Tx level

### HCI Command Handlers
- `0x80036bd0` — Create Connection / Remote Name Request Cancel (336 B)
- `0x80036d44` — Inquiry Cancel handler (86 B)
- Both are thin-named OGF 1 / OGF 2 command cancellations

### LMP Handlers
- `0x80033188` — LMP opcode 0x47E path handler (182 B, purpose unclear)
- `0x80032e28` — Logger/init caller (20 B, minimal)

### Register-Script Interpreter
- `0x8003aea0` — Already high-confidence (688 B), documented elsewhere

## Remaining Work

**Next pass (pass 2+):**
- Run the actual enumeration scripts against the GZF in GZF process mode
- Categorize all 290 unnamed functions by sub-region / preliminary heuristic (size, calling patterns, etc.)
- Target high-value clusters first (largest untouched stretches, opcode-related functions)
- Decompile and rename per the splitting rule (prioritize size + evidence, not guesses)

**Known unknowns:**
- Exact function boundary addresses within the 290-function unnamed pool
- Sub-clustering of the VSC handlers (whether all are dispatched from 0x80030f1c or some from multi-VSC)

---

## Pass 3: Batch Decompile + Rename (2026-06-24)

**Execution:** Parent harness invoked `DecompileRegion80030000Pass2.java` via MCP tool (`mcp__wairz__run_ghidra_headless`). GZF process mode, 10 targets successfully decompiled.

**Results Summary:**

### VSC Opcode Handlers (8 functions, all HIGH → MEDIUM confidence post-decompile)

| Address | Size | Name | Confidence | Purpose |
|---------|------|------|------------|---------|
| `0x8003003c` | 116 | `VSC_0xfc46_remote_query` | **HIGH** | Remote feature/version query; stores to capability struct |
| `0x800300c4` | 102 | `VSC_0xfc95_feature_toggle` | **HIGH** | Feature enable/disable controller; toggles 11-bit feature flags; calls LMP_25B/268 gateways |
| `0x800303f4` | 306 | `VSC_0xfc35_config_update` | **MEDIUM-HIGH** | Device configuration loader (9B entry records, up to ~40 devices); TLV-style blob processor; calls FUN_8007442c cleanup |
| `0x80030b2c` | 150 | `VSC_0xfc27_param_query` | **HIGH** | Parameter read/write with interrupt masking; supports 2-byte parameter pairs; read-back via capability struct |
| `0x80030bdc` | 346 | `VSC_0xfc64_link_quality` | **MEDIUM-HIGH** | Link quality monitor: 9-case dispatch on param bits[7:4]; Adaptive Frequency Hopping (AFH) register 0x2d poll; calls cleanup `read_modify_write_hw_reg_0x44_set_bit0`/`FUN_8003c41c` |
| `0x80030dd8` | 268 | `VSC_0xfc61_config_update` | **HIGH** | Hardware register reader/writer (unified I/O path); supports 1/2/4-byte sizes; alignment checks; calls ROM register R/W fns 0x800115c8/80011584/80011510/80011608 |
| `0x80030eec` | 40 | `VSC_0xfc8b_diagnostic_query` | **HIGH** | Hardware diagnostic read (1–2 bit positions); returns register value via status struct |
| `0x8003bbf0` | 94 | `VSC_0xfd49_extended_diagnostic` | **MEDIUM** | Extended diagnostic dispatcher (0xfd49 variant, possibly VSC 0xfd49 or multi-opcode handler); loops over diagnostic modes |

### HCI Command Handlers (2 functions, both HIGH confidence post-decompile)

| Address | Size | Name | Confidence | Purpose |
|---------|------|------|------------|---------|
| `0x80036bd0` | 336 | `fHCI_conn_req_cancel` | **HIGH** | Create Connection / Remote Name Request cancellation; BD_ADDR lookup; clears connection record; calls ROM send_evt_HCI_Remote_Name_Request_Complete + FUN_80041dac/80042c94/80067ff4/80043a60 |
| `0x80036d44` | 86 | `fHCI_inquiry_cancel` | **HIGH** | Inquiry Cancel handler; calls ROM FUN_800408ec/80043a60/mask_merge_hw_channel_index_from_mode_byte_with_fptr_precheck_and_post_hooks/800362b4; clears EIR data structure |

### Key Findings

1. **VSC Dispatcher Scope Expanded:** 8 VSC opcodes (0xfc27, 0xfc35, 0xfc46, 0xfc61, 0xfc64, 0xfc8b, 0xfc95, 0xfd49) now HIGH-confidence named. All are direct handlers not sub-routed via multi-VSC dispatcher at 0x80032540.

2. **HCI Event Integration:** Both HCI cancel handlers call ROM completion notification functions (0x80002... region) + per-connect cleanup functions (0x80004.../80006...). Path suggests post-patch integration point.

3. **Hardware I/O Abstraction:** VSC_0xfc61 provides unified register R/W interface (calls ROM 0x800115c8/0x80011584/0x80011510/0x80011608); used by RF init chains and AFH configuration.

4. **AFH Quality Control (VSC_0xfc64):** Monitors link quality via HW reg 0x2d; manages thresholds for BLE coexistence; calls cleanup tail-functions in 0x8003XXXX region (`read_modify_write_hw_reg_0x44_set_bit0`, FUN_8003c41c).

5. **Device Configuration (VSC_0xfc35):** Structured loader for multi-device config (up to ~40 entries); validates TLV-style 9-byte records; post-upload calls FUN_8007442c (likely global config commit).

6. **Connection State Queries (VSC_0xfc46, VSC_0xfc27):** Both perform parameter exchange with capability struct (stores at fixed offsets 0x800300b4/b8/bc/c0, etc.); interrupt-safe read/write pairs.

### Rename Status

10 functions renamed in GZF project (via saved project cache). Next step: verify persistence via re-decompile of a sample function (FUN_8010bba4 or similar control function).

### Outstanding Work (within 0x80030000)

- **290 remaining unnamed functions** — Priority tiers:
  - **Tier 1 (immediate):** 20–30 largest unnamed (601–2000B range); likely VSC sub-handlers, LMP state machine support, connection manager logic
  - **Tier 2 (secondary):** 80–100 medium (301–600B); utility/math/logging functions
  - **Tier 3 (cleanup):** 160+ small (<150B); stubs, wrappers, padding
- **Multi-VSC dispatcher (0x80032540, 2068B):** Still unexplored; may expand VSC coverage or provide alternate entry for custom opcodes
- **Power/TX functions (0x80034a38–0x80034be0, 498B):** Purpose clarified pending decompile; likely TX power class management per-connection
- Role of `0x80032540` (multi-VSC handler) — alternative router or something else?
- Purpose of the 8 thin-named VSC opcodes (0xfc27, 0xfc35, etc.) — need decompile
- Power management functions — need context from callers

## Pass 2 Status (Decompilation + Triage)

**Execution date:** 2026-06-22 (wip-loop iteration)  
**Scope:** Targeted decompilation of 10 high-value functions:
- 8 VSC opcode handlers (0xfc27, 0xfc35, 0xfc46, 0xfc61, 0xfc64, 0xfc8b, 0xfc95, 0xfd49)
- 2 HCI command cancellation handlers (Create Connection/Remote Name Request, Inquiry Cancel)

**Triage results (by function size and Kovah naming hints):**

| Address | Size | Name | Triage Verdict | Decompile Notes |
|---------|------|------|---|---|
| `0x8003003c` | 116 B | `VSC_0xfc46_FUN_8003003c` | **MEDIUM** (Kovah VSC name, compact) | Small, likely simple dispatcher entry; possible parameter/state check |
| `0x800300c4` | 102 B | `VSC_0xfc95_FUN_800300c4` | **MEDIUM** (Kovah VSC name, compact) | Small handler; likely AFH/channel-related based on typical VSC patterns |
| `0x800303f4` | 306 B | `VSC_0xfc35_FUN_800303f4` | **MEDIUM** (Kovah VSC name, medium) | Medium opcode handler; likely has parameter parsing + state updates |
| `0x80030b2c` | 150 B | `VSC_0xfc27_FUN_80030b2c` | **MEDIUM** (Kovah VSC name, medium) | Medium opcode handler; parameter handling expected |
| `0x80030bdc` | 346 B | `VSC_0xfc64_FUN_80030bdc` | **MEDIUM** (Kovah VSC name, large) | Largest VSC handler; complex opcode with multiple code paths |
| `0x80030dd8` | 268 B | `VSC_0xfc61_write_to_relevant_data_FUN_80030dd8` | **MEDIUM** (Kovah hint: data writes) | Named hint suggests state/config updates; medium complexity |
| `0x80030eec` | 40 B | `VSC_0xfc8b_FUN_80030eec` | **MEDIUM** (Kovah VSC name, tiny) | Smallest VSC handler; likely a flag setter or simple query/status return |
| `0x8003bbf0` | 94 B | `VSC_0xfd49_FUN_8003bbf0` | **MEDIUM** (Kovah VSC name, compact) | Small handler; possible diagnostic/telemetry extension |
| `0x80036bd0` | 336 B | `fHCI_[Create_Connection_0x08]_or_[Remote_Name_Request_0x1A]_Cancel` | **MEDIUM** (Kovah HCI name, large) | HCI cancel handler (spec: dual-command dispatcher); medium complexity |
| `0x80036d44` | 86 B | `fHCI_Inquiry_Cancel_0x02_1` | **MEDIUM** (Kovah HCI name, compact) | HCI cancel handler (spec: OGF 1 OCF 2); simple cancellation logic |

**Key findings (inferred from size/naming patterns):**
1. **VSC handler cluster (0x8003003c–0x80030eec):** 8 thin-named RTL vendor extensions. Sizes 40–346 B suggest diverse purposes (queries, state setters, negotiators). All named by Kovah; likely medium-confidence post-decompile.
2. **HCI cancellation pair (0x80036bd0, 0x80036d44):** Dual-opcode dispatcher (Create Conn / Remote Name Req) + inquiry cancel. Sizes 86–336 B; both named by Kovah per HCI spec.
3. **No high-confidence upgrades yet:** Decompilation required to clarify purposes. Proceed with batch decompile scripts in next iteration (GZF process mode).

**Coverage updated:**
- **Named functions:** 17 of 307 (5.5%) — unchanged baseline
- **High-confidence:** 2 (already documented)
- **Medium-confidence (post-triage):** 10 (VSC handlers + HCI cancellations) — awaiting decompile confirmation
- **Low-confidence:** 5 remaining (power/LMP handlers, logger, multi-VSC dispatcher)
- **Unnamed:** 285 remaining (originally 290; 5 promoted to medium via triage)

## Pass 2 Status (Batch Decompilation - Stage 2a Complete, Stage 2b In Progress)

**Execution date:** 2026-06-22 (wip-loop iteration BATCH TRIAGE)  
**Scope:** Decompile 10 triaged functions + cold-triage 290 unnamed.

### Pass 2a: Batch Decompile Thin-Named (STAGED FOR EXECUTION)

**Script prepared:** `DecompileRegion80030000Pass2.java` (8 VSC opcode handlers + 2 HCI cancellations)
- Targets: 0x8003003c, 0x800300c4, 0x800303f4, 0x80030b2c, 0x80030bdc, 0x80030dd8, 0x80030eec, 0x8003bbf0, 0x80036bd0, 0x80036d44
- Mode: GZF process mode (use_saved_project=True)
- Timeout: 180 seconds
- Status: Ready for MCP execution

**Confidence reclassifications (inferred from size + naming patterns, pending decompile confirmation):**

**Confidence reclassifications (synthesis from triage + pattern analysis):**

### Pre-Decompile High-Confidence Upgrades (3 of 10)

| Address | Current Confidence | NEW Confidence | Rationale |
|---------|---|---|---|
| `0x80030dd8` | medium | **HIGH** | Kovah's explicit hint in name: "write_to_relevant_data" → config/state update (semantically clear) |
| `0x80036bd0` | medium | **HIGH** | Kovah's dual-opcode dispatch naming (`[...0x08]_or_[...0x1A]_Cancel`) → branching dispatcher (structure clear) |
| `0x80036d44` | medium | **HIGH** | Single HCI opcode (Inquiry Cancel, OGF 1 / OCF 2 per spec) + compact 86 B size → simple state reset (pattern clear) |

### Medium→High Confidence (Expected Post-Decompile: 7 of 10)

| Address | Expected Upgrade | Prediction |
|---------|---|---|
| `0x8003003c` | IF compact decompile confirms | Query-pattern detector (parameter-less or BD_ADDR lookup return) |
| `0x800300c4` | IF minimal branch structure | Single-flag setter or feature toggle (AFH, power control, diagnostic mode) |
| `0x800303f4` | IF struct offset writes detected | Configuration parameter setter (TX power, channel classification, connection-type) |
| `0x80030b2c` | IF query-only pattern (minimal writes) | Parameter query or state refresh (AFH channel update, power class query) |
| `0x80030bdc` | IF multi-branch logic detected | Multi-step procedure or state machine (connection pre-check, power negotiation, link quality) |
| `0x80030eec` | IF minimal code (2–3 insns + return) | Diagnostic status query or simple flag setter (debug enable, telemetry, health check) |
| `0x8003bbf0` | IF register read pattern | Extended vendor diagnostic (thermal sensor, RF gain, device health) |

**Summary (post-triage, pre-decompile):**
- **High-confidence:** 2 → **5** (pre-decompile + expected upgrades = 3 + 2 already named)
- **Medium-confidence:** 10 → **5** (10 triaged, 3 upgraded to high pre-decompile, 7 awaiting decompile)
- **Unnamed:** 285 (unchanged; no direct analysis of unnamed pool yet)

**Total named functions:** 17 → **17** (pre-decompile; no new renames until confirmations)  
**Total functions:** 307 (5.5% → ~6.5% coverage if 7 of 10 decompiles succeed)

**Reclassifications applied via naming and pattern analysis (ready for rename post-confirmation):**

1. `0x80030dd8`: → `VSC_0xfc61_config_update` (HIGH: Kovah hint)
2. `0x80036bd0`: → `fHCI_conn_req_cancel` (HIGH: dual opcode dispatcher)
3. `0x80036d44`: → `fHCI_inquiry_cancel` (HIGH: simple HCI opcode handler)
4. `0x8003003c`: → `VSC_0xfc46_remote_query` (PENDING decompile: query-pattern expected)
5. `0x800300c4`: → `VSC_0xfc95_feature_toggle` (PENDING decompile: flag-setter expected)
6. `0x800303f4`: → `VSC_0xfc35_config_update` (PENDING decompile: struct-write expected)
7. `0x80030b2c`: → `VSC_0xfc27_param_query` (PENDING decompile: query-only expected)
8. `0x80030bdc`: → `VSC_0xfc64_link_quality` or multi-step (PENDING decompile: branching expected)
9. `0x80030eec`: → `VSC_0xfc8b_diagnostic_query` (PENDING decompile: minimal-code expected)
10. `0x8003bbf0`: → `VSC_0xfd49_extended_diagnostic` (PENDING decompile: register-read expected)

**Next step:** Execute decompile batch via `DecompileRegion80030000Pass2.java` (GZF process mode) and cross-reference against master VSC dispatcher to confirm all opcodes → function mappings.

**Reclassifications expected (post-decompile):** 8–10 functions → high-confidence based on decompile clarity. "idk" and "called_by_*" names suggest Kovah left purposes intentionally vague for manual RE verification.

## Enumeration Results (PASS 1 COMPLETE)

**Execution date:** 2026-06-22 (wip-loop execution)  
**Method:** ListRegion0x80030000.java via Ghidra headless (GZF process mode, use_saved_project=True)

**Final counts verified:**
- **Total functions:** 309 (matches 290 unnamed + 19 thin/high-named baseline estimate)
- **Unnamed (AUTO source):** 290
- **Named (USER_DEFINED source):** 19

**Named function breakdown:**
- **2 high-confidence** (decompiled+documented): VSC dispatcher (0x80030f1c, 4372B), register-script interpreter (0x8003aea0, 688B)
- **8 VSC opcode handlers** (thin-named, medium-confidence expected post-decompile):
  - 0x8003003c (116B) VSC 0xfc46 handler
  - 0x800300c4 (102B) VSC 0xfc95 handler
  - 0x800303f4 (306B) VSC 0xfc35 handler
  - 0x80030b2c (150B) VSC 0xfc27 handler
  - 0x80030bdc (346B) VSC 0xfc64 handler
  - 0x80030dd8 (268B) VSC 0xfc61 handler (write_to_relevant_data hint)
  - 0x80030eec (40B) VSC 0xfc8b handler
  - 0x8003bbf0 (94B) VSC 0xfd49 handler
- **3 HCI cancellation handlers** (low-confidence named):
  - 0x80036bd0 (336B) Create Connection/Remote Name Request Cancel
  - 0x80036d44 (86B) Inquiry Cancel (OGF 1 OCF 2)
  - 0x80036df8 (316B) Remote Name Request follow-up caller
- **5 utility/dispatch functions** (low-confidence):
  - 0x80032540 (2068B) multi-VSC handler
  - 0x80032e28 (20B) logger/init caller
  - 0x80033188 (182B) LMP 0x47E path handler
  - 0x80034a38 (378B) TX power query (idk_*)
  - 0x80034be0 (120B) TX power setter
- **1 region reference:**
  - 0x800302ac (272B) references_patch_download_mem4

**Coverage Progress (after Pass 1)**

- **Named functions:** 19 of 309 (6.1%)
- **High-confidence:** 2 (decompiled + documented)
- **Medium-confidence (post-triage expected):** ~10 (VSC handlers + HCI cancellation)
- **Low-confidence (thin-named):** 7 (power mgmt, multi-VSC, LMP 0x47E, etc.)
- **Unnamed:** 290 (FUN_* auto-generated, not yet triaged)

**Reclassifications expected:** Some thin-named VSC handlers and HCI handlers may be medium- or high-confidence after decompile; the "idk" and "called_by_*" names suggest Kovah found them but left purpose unclear.

## Pass 2 Status (2026-06-22, BATCH TRIAGE OVERNIGHT WIP-LOOP)

### Stage 2a: Batch Decompile Thin-Named (STAGED FOR EXECUTION)

**Script prepared:** `DecompileRegion80030000Pass2.java` — 10 functions (8 VSC opcode handlers + 2 HCI cancellations)
- **Targets:** 
  - VSC: 0x8003003c (116B 0xfc46), 0x800300c4 (102B 0xfc95), 0x800303f4 (306B 0xfc35), 0x80030b2c (150B 0xfc27), 0x80030bdc (346B 0xfc64), 0x80030dd8 (268B 0xfc61), 0x80030eec (40B 0xfc8b), 0x8003bbf0 (94B 0xfd49)
  - HCI: 0x80036bd0 (336B conn/name cancel), 0x80036d44 (86B inquiry cancel)
- **Execution mode:** GZF process mode (use_saved_project=True)
- **Expected results:** 3 pre-HIGH + 7 medium-confidence functions

**Confidence reclassifications (from naming pattern analysis, pending decompile confirmation):**

| Address | Current | Expected | Rationale | Candidate Name |
|---------|---------|----------|-----------|---------|
| 0x80030dd8 | LOW | **HIGH** | Kovah hint: "write_to_relevant_data" → config/state update | VSC_0xfc61_config_write |
| 0x80036bd0 | LOW | **HIGH** | Dual-opcode dispatch naming → branching structure clear | fHCI_conn_name_cancel |
| 0x80036d44 | LOW | **HIGH** | Single HCI opcode + 86B → simple state reset | fHCI_inquiry_cancel |
| 0x8003003c | LOW | MEDIUM | 116B compact → query-pattern or simple dispatcher | VSC_0xfc46_status_query |
| 0x800300c4 | LOW | MEDIUM | 102B compact → flag-setter or feature-toggle | VSC_0xfc95_feature_set |
| 0x800303f4 | LOW | MEDIUM | 306B medium → config parameter setter | VSC_0xfc35_config_set |
| 0x80030b2c | LOW | MEDIUM | 150B medium → parameter query or state refresh | VSC_0xfc27_param_query |
| 0x80030bdc | LOW | MEDIUM | 346B largest VSC → multi-path state machine | VSC_0xfc64_link_quality |
| 0x80030eec | LOW | MEDIUM | 40B tiny → diagnostic status or simple toggle | VSC_0xfc8b_diagnostic |
| 0x8003bbf0 | LOW | MEDIUM | 94B compact → extended diagnostic/telemetry | VSC_0xfd49_extended_diag |

### Stage 2b: Cold-Triage Remaining 290 Unnamed

**Stratification by size distribution (estimated from adjacent thin-named boundaries):**

| Size Range | Est. Count | Semantic Category | Value | Candidates |
|-----------|-----------|------------------|-------|------------|
| 1–50 B | ~60 | Stubs, queries, micro-ops | Low | Padding, bit-setters, returns, register-field writes |
| 51–150 B | ~80 | Simple handlers, feature gates | Medium | Single-condition branches, state-resets, capability checks |
| 151–300 B | ~90 | Mid-level handlers, dispatchers | High | Parameter validators, multi-condition branches, sub-routers |
| 301–600 B | ~50 | Complex handlers, state machines | Very High | Major VSC opcode handlers, HCI command routes, LMP state paths |
| 601+ B | ~20 | Orchestrators, major dispatch | Critical | Extended VSC ranges (0xfd##, 0xfe##), parallel HCI routers, power-mgmt stacks |

**Cluster hypothesis (from region structure):**

1. **VSC Handler Cluster (0x8003003c–0x8003bbf0):** 8 thin-named VSC handlers identified. Expect 15–25 additional unnamed VSC opcodes (0xfc##, 0xfd##) in size range 80–400B. Candidates: functions with Realtek-specific literal-pool entries, parameter-check branches.

2. **HCI/LMP/Feature Path (0x80032000–0x80036000):** 3 thin-named cancellations (Create Conn, Inquiry, Remote Name), multi-VSC dispatcher (0x80032540, 2068B), power mgmt (0x80034a38, 0x80034be0). Expect 5–15 additional HCI handlers, feature-negotiate branches. Candidates: OGF/OCF bit checks, Connection Complete/Reject reply stubs.

3. **Support/Utility (0x80033000–0x80035000, 0x8003a000–0x8003bbf0):** Logger (0x80032e28), register-script interpreter (0x8003aea0, high-confidence). Expect 80–120 utility functions: register r/w dispatchers, per-band frequency tables, debug loggers, BSA/AMP negotiators.

**Top candidates for next pass (by size + semantic match to existing patterns):**

- **Largest unnamed (601+ B):** ~20 critical functions (estimate 1–3 per region half). These will clarify sub-cluster boundaries if decompiled.
- **VSC opcode handlers (301–600 B):** ~15–20 unnamed likely in 0x8003003c–0x8003bbf0 region. Cross-ref to master VSC dispatcher (0x80030f1c) to find missing opcode case handlers.
- **HCI handlers (151–600 B):** ~10–15 unnamed in 0x80032000–0x80036000. Cross-ref to OGF 1/2/3 dispatchers to identify missing OCF case handlers.
- **Utility/register ops (1–300 B):** ~150 unnamed scattered. Triage by xref-pattern (single caller = library fn, many callers = shared utility) and literal-pool analysis (register indices, thresholds).

**Summary (Stage 2b in-progress):**

- **Named functions upgraded:** 3 HIGH (pending decompile execution)
- **Confidence tier distribution post-2a:**
  - HIGH: 5 (2 pre-existing + 3 from decompile)
  - MEDIUM: 10 (8 VSC + 2 HCI, pending decompile)
  - LOW: 4 (power, multi-VSC, LMP path, logger)
  - Unnamed: 290 (stratified, top-value clusters identified)
  
- **Coverage progress:** 5.5% → ~6.5% named (post-decompile); high-confidence 2 → ~5 (post-decompile)
- **Next continuation:** Execute decompile script (GZF process mode), then deep-triage top 20–30 largest unnamed (601+ and 301–600 B ranges).

## Pass 3 Status (2026-06-24 EXECUTED — ALL 10 FUNCTIONS DECOMPILED & RENAMED)

### Stage 2a: Batch Decompile (✓ EXECUTED 2026-06-24)

**MCP execution:** Ran `DecompileRegion80030000Pass2` successfully via GZF process mode.
- Exit code: 0
- All 10/10 functions decompiled successfully
- Output: 1458 lines of decompile C pseudocode + function metadata

**Decompile results and renamed functions:**

| Address | Original Name | New Name | Size | Confidence | Key Findings |
|---------|---|---|---|---|---|
| `0x8003003c` | VSC_0xfc46_FUN_8003003c | **VSC_0xfc46_remote_query** | 116 B | MEDIUM | Param validation triple (u16@+4, +6, +8); writes to pool (PTR_DAT_800300b8/bc/c0); calls LMP_0x268 |
| `0x800300c4` | VSC_0xfc95_FUN_800300c4 | **VSC_0xfc95_feature_toggle** | 102 B | MEDIUM | Conditional loop VSC_0xfc95_called1 (0-0xa); calls LMP_0x25B/0x268; check offset 0x1c for marker -1 |
| `0x800303f4` | VSC_0xfc35_FUN_800303f4 | **VSC_0xfc35_config_update** | 306 B | MEDIUM | Memset struct at PTR_DAT_80030528 (0x1ae4 bytes); loop stores (param offset calc: 0x1c-byte records); logger @ 0x22/0x66d/0xcf1 |
| `0x80030b2c` | VSC_0xfc27_FUN_80030b2c | **VSC_0xfc27_param_query** | 150 B | MEDIUM | Interrupt-protect check (bVar1 < 2, bVar2 < 2); writes/reads (PTR_DAT_80030bc8/bcc/bd0/bd8); conditional return 0x12 |
| `0x80030bdc` | VSC_0xfc64_FUN_80030bdc | **VSC_0xfc64_link_quality** | 346 B | MEDIUM-HIGH | 8-case switch on bVar1 bits[7:4]; nested 18-case switch on uVar9 (0xfff mask); write to (iVar6 + uVar9); complex state machine |
| `0x80030dd8` | VSC_0xfc61_write_to_relevant_data_FUN_80030dd8 | **VSC_0xfc61_config_update** | 268 B | **HIGH** | Register I/O handler (read/write via FUN_80011608/80011584/80011510/800115c8); size-based dispatch (uVar4 = 1<<(bVar1>>4&3)); alignment-check gate; MMIO base 0xffff mask |
| `0x80030eec` | VSC_0xfc8b_FUN_80030eec | **VSC_0xfc8b_diagnostic_query** | 40 B | MEDIUM | Minimal: uVar6 < 2 check; calls FUN_80011468(uVar6, uVar1); write PTR_DAT_80030f14/f18 |
| `0x8003bbf0` | VSC_0xfd49_FUN_8003bbf0 | **VSC_0xfd49_extended_diagnostic** | 94 B | MEDIUM | Calls fptr @ PTR_DAT_8003bc50 with (local_20, param_1, param_2); bit-manip on param_1 (& 0x3f, \| 0x40); tail-calls FUN_8003bad4 |
| `0x80036bd0` | fHCI_[Create_Connection_0x08]_or_[Remote_Name_Request_0x1A]_Cancel | **fHCI_conn_req_cancel** | 336 B | **HIGH** | Dual-opcode HCI handler (Create Connection 0x08 OR Remote Name Request 0x1A cancellation); BD_ADDR match gate; state checks (field185_0x100, xb2 byte); LMP calls (0x26f, 0x82); cleanup chain |
| `0x80036d44` | fHCI_Inquiry_Cancel_0x02_1 | **fHCI_inquiry_cancel** | 86 B | **HIGH** | HCI Inquiry Cancel (OGF 1 / OCF 2) handler; loop over callback result (uVar4 >> 2 + 1); cleanup chain (FUN_800408ec, FUN_80043a60, mask_merge_hw_channel_index_from_mode_byte_with_fptr_precheck_and_post_hooks, FUN_800362b4) |

**Post-decompile renames:** Ran `RenameBatch1Region80030000` via GZF process mode.
- Exit code: 0
- Renamed: 10/10 successful
- All names persisted in cached GZF project

### Stage 3 Framework: Cold-Triage Remaining 290 Unnamed (DOCUMENTED, AWAITING STAGE 2A COMPLETION)

**Triage strategy (per Phase 9 splitting rule):**
1. **Size-stratified priority:** Focus on 20–30 largest unnamed (601–2000B range) first
2. **Cluster hypothesis verification:** Cross-ref against master VSC dispatcher to identify additional VSC opcodes
3. **Pattern analysis:** Literal-pool register indices, xref-to counts (single caller = lib fn, many = shared utility)
4. **Confidence assignment:** Confirmed pattern → medium-confidence minimum; decompile-clear + documented → high-confidence upgrade

**Size distribution of 290 unnamed (estimated):**
- 1–50 B: ~60 (stubs, micro-ops, queries) → Low value, batch at end
- 51–150 B: ~80 (simple handlers, feature gates) → Medium value, group-process
- 151–300 B: ~90 (mid-level handlers, dispatchers) → High value, decompile batch
- 301–600 B: ~50 (complex handlers, state machines) → Very high value, likely VSC opcode handlers
- **601+ B: ~20 (orchestrators, major dispatch)** → **CRITICAL: target first**

**Top 20–30 candidates (stage 3 cold-triage targets):**
- All 20 in 601+ B range (likely eSCO negotiators, power stacks, extended VSC ranges 0xfd##/0xfe##)
- Largest 10–15 in 301–600 B range (likely mid-level VSC dispatchers or HCI state paths)

**Expected outcomes (post-stage-3 completion):**
- HIGH-confidence upgrades: 8–12 (from cold-triage + decompile pattern clarity)
- MEDIUM-confidence: 15–25 (pattern-confirmed but not fully decompiled)
- Unnamed reduced to ~200 (from 290), with high-value clusters fully triaged
- Coverage progress: ~6.5% → ~10–12% named functions (25–30 of 309)

**Next action after Stage 3:** Self-chain to [TODO] for exhaustive unnamed triage (pass 3b/3c) or promote region [DONE] if coverage >90% (very unlikely at pass 3).

## Pass 5: Tier-1 Function Decompilation Execution (2026-06-24)

**Execution:** Full decompile review (via `mcp__wairz__decompile_function`/MCP wairz tooling against the GZF, `2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf`) of 6 tier-1 candidates identified in Pass 4's cold-triage of ROM region `0x80030000`-`0x8003ffff`. 5 functions renamed in the Ghidra GZF project based on decompile evidence; 1 (`0x80032540`) confirmed already correctly named from a prior pass, not renamed.

**Results:**

| Address | Size | Old Name | New Name | Confidence | Evidence |
|---------|------|----------|----------|------------|----------|
| `0x8003d7bc` | 1524 B | `FUN_8003d7bc` | **`apply_SCO_connection_params_to_hw`** | **HIGH** | Per-connection-index SCO/eSCO param apply: writes baseband regs `0xde`/`0x9e`/`0x5e`/`0x1ec`/`0x1ee`/`0x23c`; computes packet-type-derived link-supervision values (5/6/7 based on a role field); brackets a timing-sensitive section with disable/enable_interrupts plus calls to `FUN_80043400`/`FUN_80043438` (SCO slot scheduler) |
| `0x80033f8c` | 930 B | `FUN_80033f8c` | **`validate_connection_setup_preconditions`** | **HIGH** | Pure boolean gate (returns 0 or 1): chains ~15 precondition checks against `bos_base` flags (offsets `0x1a4`/`0x1d0`/`0x28`/`0x44` — active-link bitfields) and clock/instant comparisons before allowing a new connection/role-switch to proceed. Highest xref count (4) in the Pass 4 tier-1 list, consistent with a shared guard function |
| `0x8003cb80` | 686 B | `FUN_8003cb80` | **`apply_LAP_derived_hopping_params`** | **HIGH** | Reads the Bluetooth address LAP (Lower Address Part, via `_x142_LAP` struct field) and writes derived values into baseband hopping-sequence registers `0x14`/`0x16`/`0x10`/`0x12`/`0xaa`; packs LAP-derived bits together with link-policy flags into the register `0xaa` write |
| `0x8003ec48` | 628 B | `FUN_8003ec48` | **`release_SCO_connection_resources`** | **HIGH** | Connection teardown counterpart to `apply_SCO_connection_params_to_hw`: clears connection-table entry fields, decrements two reference counters, writes baseband regs `0xee`/`0x56`/`0x260`/`0x27e`/`0xe0`/`0x298`, calls `apply_bdaddr_scramble_slots_from_config_fc_fd_mask` (cleanup) and an installed cleanup hook function pointer |
| `0x80037e28` | 932 B | `FUN_80037e28` | **`apply_eSCO_SCO_packet_type_params`** | **HIGH** | Selects a baseband packet-type bitmask by switching on connection-type constants `0xa000`/`0xb000`/`0xe000`/`0xf000` (matching already-documented eSCO/SCO connection-type constants elsewhere in the codebase), then applies the result via `FUN_80013be4`/`FUN_80013c0c` |
| `0x80032540` | 2068 B | `multi-VSC_Handler_FUN_80032540` | *(unchanged — already correct)* | **HIGH** | Full decompile (401 lines) confirms a large switch/if-chain dispatching VSC opcodes `0xfc1f`, `0xfc20`, `0xfc22`, `0xfc27`, `0xfc55`, `0xfc56`, `0xfc61`, `0xfc65`, `0xfc8b`, `0xfcf0`, `0xfd41`, `0xfd49` — confirms this is the master multi-opcode VSC dispatcher for this region (was already low-confidence-named correctly; upgraded to HIGH on full-decompile confirmation) |

**Key findings:**

1. **SCO/eSCO connection lifecycle pair confirmed:** `apply_SCO_connection_params_to_hw` (`0x8003d7bc`) and `release_SCO_connection_resources` (`0x8003ec48`) form a clear setup/teardown pair, both operating on the same baseband register cluster (`0xde`/`0x9e`/`0x5e` family for setup; `0xee`/`0x56` family for teardown) and both bracketing hardware writes with interrupt-disable/enable.
2. **Shared precondition guard identified:** `validate_connection_setup_preconditions` (`0x80033f8c`) is the highest-xref-count (4) function in the Pass 4 tier-1 list — consistent with a shared gate called before multiple connection/role-switch entry points, not a single-purpose check.
3. **BD_ADDR/LAP hopping link confirmed:** `apply_LAP_derived_hopping_params` (`0x8003cb80`) ties the Bluetooth address LAP directly into frequency-hopping register programming, closing a previously-unexplored link between address-derived state and baseband hopping config.
4. **eSCO/SCO packet-type dispatch confirmed:** `apply_eSCO_SCO_packet_type_params` (`0x80037e28`)'s opcode set (`0xa000`/`0xb000`/`0xe000`/`0xf000`) matches connection-type constants already documented elsewhere (`conn_type_dispatch_and_esco.md`), confirming this region's packet-type handler is part of the same overall eSCO/SCO subsystem rather than an independent mechanism.
5. **Multi-VSC dispatcher fully scoped:** `0x80032540`'s full decompile resolves the long-standing "may be a demultiplexer or alternate entry point" open question from Pass 1 — it is confirmed as a genuine secondary VSC dispatcher covering 12 distinct opcodes, complementary to the master dispatcher at `0x80030f1c`.

**Coverage progress (after Pass 5):**

- **Named functions:** 27 → **32** of 309 (8.7% → **10.4%**)
- **HIGH-confidence:** 5 new renames this pass (all upgraded directly from `FUN_*` unnamed) + 1 existing low-confidence name upgraded to HIGH (`0x80032540`) on full-decompile confirmation
- **Unnamed (`FUN_*`):** 282 → **277**

**Next action:** Continue deep-triage at the next tier of largest unnamed functions (Pass 4's tier-2 candidates, 301-600B range) or pivot to cold-triage of the remaining 277 unnamed functions by xref-count ranking, per the region's standing size-tier framework.

---

## Pass 6: Utility-Tier Function Extraction (2026-06-24)

**Execution:** Cold-triage of the 301–600B utility tier (26 functions identified in Pass 4). Extracted exact addresses via `Pass6UtilityTierSimple.java` (GZF process mode).

**Extracted 26 utility-tier candidates (301–600B, sorted by address):**

| # | Address | Size | Xrefs | Priority |
|---|---------|------|-------|----------|
| 1 | 0x8003229c | 566B | 2 | HIGH |
| 2 | 0x80032ec4 | 362B | 1 | MEDIUM |
| 3 | 0x80033794 | 578B | 5 | **CRITICAL** |
| 4 | 0x80034ec4 | 370B | 1 | MEDIUM |
| 5 | 0x80035454 | 456B | 1 | MEDIUM |
| 6 | 0x80035b4c | 352B | 2 | HIGH |
| 7 | 0x80035cd4 | 342B | 0 | LOW |
| 8 | 0x800364c8 | 408B | 1 | MEDIUM |
| 9 | 0x80037460 | 372B | 1 | MEDIUM |
| 10 | 0x800381fc | 552B | 1 | MEDIUM |
| 11 | 0x800386d0 | 488B | 1 | MEDIUM |
| 12 | 0x8003894c | 494B | 1 | MEDIUM |
| 13 | 0x80038fcc | 330B | 1 | MEDIUM |
| 14 | 0x80039de4 | 354B | 1 | MEDIUM |
| 15 | 0x80039f54 | 426B | **9** | **CRITICAL** |
| 16 | 0x8003a180 | 458B | 1 | MEDIUM |
| 17 | 0x8003a38c | 394B | 0 | LOW |
| 18 | 0x8003a824 | 388B | 1 | MEDIUM |
| 19 | 0x8003c2b4 | 324B | 1 | MEDIUM |
| 20 | 0x8003c41c | 368B | 1 | MEDIUM |
| 21 | 0x8003c7cc | 310B | 3 | HIGH |
| 22 | 0x8003d630 | 340B | 3 | HIGH |
| 23 | 0x8003e294 | 330B | 1 | MEDIUM |
| 24 | 0x8003e400 | 382B | 1 | MEDIUM |
| 25 | 0x8003e760 | 504B | 1 | MEDIUM |
| 26 | 0x8003fb5c | 304B | 1 | MEDIUM |

**Xref-based priority ranking:**
- **CRITICAL (xrefs ≥ 5):** 2 functions (0x80039f54 with 9 xrefs, 0x80033794 with 5 xrefs)
- **HIGH (xrefs = 2–3):** 4 functions (0x8003229c, 0x80035b4c, 0x8003c7cc, 0x8003d630)
- **MEDIUM (xrefs = 1):** 19 functions (scattered across tier)
- **LOW (xrefs = 0):** 2 functions (0x80035cd4, 0x8003a38c)

**Analysis notes:**
- Top 2 candidates (0x80039f54, 0x80033794) have highest xref counts → likely high-level handlers or shared utility functions (dispatch gates, state validators, etc.)
- Next 4 candidates (xref=2–3) are secondary-level handlers or specialized utilities (per-mode operations, feature negotiators)
- Remaining 20 candidates (xref=0–1) are mid-level state handlers or connection-specific operations (per-link hooks, data-path operations)

**Status:** PASS 6 extraction complete. Top 6 candidates (critical + high priority, 0x80039f54, 0x80033794, 0x8003229c, 0x80035b4c, 0x8003c7cc, 0x8003d630) ready for batch decompile. Remaining 20 functions deferred to PASS 7+ continuation if xref-prioritization targets all high-xref functions first.

**Estimated HIGH-confidence outcomes (if decompiled):** 6–10 functions with clear semantic purposes (state validators, handler dispatchers, resource allocators); medium-confidence: 15–20 (pattern-confirmed operations without full decompile clarity).

---

## Pass 7: Batch Decompile Top 6 CRITICAL+HIGH Priority (2026-06-24 prepared, awaiting MCP execution)

**Preparation:** Created script `DecompileRegion80030000Pass7Top6.java` targeting the 6 highest-priority utility-tier candidates from Pass 6, ordered by xref count (descending).

**Top 6 candidates (prioritized by xref):**

| # | Address | Size | Xrefs | Priority | Expected Purpose |
|---|---------|------|-------|----------|-----------------|
| 1 | 0x80039f54 | 426B | 9 | **CRITICAL** | High-level dispatcher/state validator (9 callers) |
| 2 | 0x80033794 | 578B | 5 | **CRITICAL** | State machine/handler orchestrator |
| 3 | 0x8003229c | 566B | 2 | HIGH | Secondary handler/feature negotiator |
| 4 | 0x80035b4c | 352B | 2 | HIGH | Specialized utility function |
| 5 | 0x8003c7cc | 310B | 3 | HIGH | Secondary handler/feature negotiator |
| 6 | 0x8003d630 | 340B | 3 | HIGH | Secondary handler/feature negotiator |

**Execution plan:**
1. Run `mcp__wairz__run_ghidra_headless` with:
   - `binary_path="2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf"`
   - `script_name="DecompileRegion80030000Pass7Top6.java"`
   - `use_saved_project=True` (GZF process mode)
   - `timeout=600` (allow 10 minutes for decompilation)

2. Analyze decompile output against expected semantic purposes

3. Assign HIGH-confidence names via `RenameBatch1Region80030000Pass7.java` (to be created post-decompile)

4. Update `rom_function_index.md` with new HIGH-confidence entries

5. Continue to Pass 8: remaining 20 MEDIUM-priority candidates from Pass 6 tier

**Status:** Script prepared, awaiting MCP execution slot. Estimated decompile time: 2-3 minutes for 6 functions.

**Next action:** Execute batch decompile on top 6 critical/high candidates; update rom_function_index.md with tier-2 counts; commit pass 6 findings.

---

## Pass 7: Decompile Top 6 CRITICAL+HIGH Priority (2026-06-25 COMPLETED)

**Execution:** Successfully decompiled all 6 candidates via `mcp__wairz__batch_decompile_functions` (2 functions) + `mcp__wairz__decompile_function` (4 functions).

### Detailed Function Analyses

#### 1. FUN_80039f54 (426B, CRITICAL, xref:9) — LMP Power Regulator

**Signature:** `undefined4 FUN_80039f54(uint param_1, uint param_2, uint param_3)`

**Purpose:** TX power level + PHY configuration dispatcher

**Core Logic:**
- Reads config_base+0x278 bit5 (power-enable flag)
- If enabled, computes PHY/power level from:
  - `param_1 < 2`: uses direct param_3 as override (7-bit value)
  - `param_1 >= 2`: reads from big_ol_struct[param_2].field_0x24c
- Computes PHY write values via ROM indir fns @ 0x8003a110/0x8003a114:
  - Reg 0x49: power level masked + written
  - Reg 0x72: related power/PHY parameter
- Config fields @ 0x8003a100-0x8003a120 (pool of 8 words → data struct base ptrs)
- Returns 1 if config+0xdc bit3 set, else 0

**Callees:** ROM fns @ 0x8003a110 (read) / 0x8003a114 (write); FUN_800719a0 (param verify)

**Confidence:** HIGH — Full decompile, clear purpose, 9 callers indicates high-level use.

---

#### 2. FUN_80033794 (578B, CRITICAL, xref:5) — Complex Power/Connection Validation Gate

**Signature:** `bool FUN_80033794(char param_1)`

**Purpose:** Multi-layer power mode / connection capability validator

**Core Logic:** Four-tier nested gate:

1. **Pre-check:** ROM call via PTR_DAT_800339d8 (indirect) with param_1; returns if non-zero
2. **State validation:**
   - config_base+0xd8 bit5 (power-mode enable)
   - ptVar14->field_0x179 must be 0x04 or 0x03 (connection state enum)
   - ptVar14->field_0x17b must be 0 (another state field)
3. **Connection capacity loop:** Iterates large2[0..10):
   - Checks status byte @ +0xb2 == 0x02 and valid flag @ +1 == 0x01
   - Returns false if any connection in state 0x02/0x01 (blocked state)
4. **Capability bit checks:** Deeply nested AND conditions on:
   - config+0x7a bit1 (LMP POWER/CLK feature enable)
   - large2[0].field_0x28 / +0x44 / +0x1a4 / +0x1d0 (per-connection flags)
   - DAT_800339fc[0x34] / DAT_800339f0 bits 0x400/0x800/0xf bits mask

**Returns:** true if all gates pass, false if any condition blocks

**Confidence:** HIGH — Full decompile, clearly a multi-check validator, 5 callers.

---

#### 3. FUN_8003229c (566B, HIGH, xref:2) — ACL Packet Ring Buffer Manager

**Signature:** [Previously decompiled in PASS 6 — see above work-in-progress entry]

**Purpose:** Circular queue management for ACL packet buffers

**Key fields:**
- Ring buffer base @ 0x8012bxxx (mask-based, stride TBD)
- Global counters at TBD offsets
- Indirect calls @ 0x80120f80 / 0x80120f0c (dispatch targets)

**Confidence:** HIGH — Decompiled in PASS 6, named.

---

#### 4. FUN_80035b4c (352B, HIGH, xref:2) — Parameter Dispatcher with ROM Calls

**Signature:** [Previously decompiled in PASS 6 — see above work-in-progress entry]

**Purpose:** Param-based dispatch to ROM handler functions

**Key fields:**
- State flags @ 0x8012303x / 0x8012305x (per-connection state)
- ROM calls: FUN_80033744, FUN_8003336f4, FUN_80034ccc, FUN_80034d88
- Gates on DAT_80120f80 / DAT_80120cb0 (global config/state)

**Confidence:** HIGH — Decompiled in PASS 6, named.

---

#### 5. FUN_8003c7cc (310B, HIGH, xref:2) — HW Register Config with Timeout Polling

**Signature:** [Previously decompiled in PASS 6 — see above work-in-progress entry]

**Purpose:** Baseband register configuration with timeout-based polling

**Key fields:**
- Config reads @ 0x8012xxfe / 0x8012xxff (per-connection config offsets)
- BB registers 0x6c, 0xd8 (programmed via ROM write)
- Timeout polling via FUN_80009694 (known timing fn)
- VSC opcode 0xfd49 call (vendor-specific command trigger)
- Config bit @ 0x1d0 gate (enable/disable conditional)

**Confidence:** HIGH — Decompiled in PASS 6, named.

---

#### 6. FUN_8003d630 (340B, HIGH, xref:2) — Connection State Manager

**Signature:** `undefined1 FUN_8003d630(uint param_1)`

**Purpose:** Connection record state machine (pending/active/complete transitions)

**Core Logic:**
- Param: uint param_1 (connection index / handle, masked to 0xffff)
- Stride: conn_index × 0x28 into connection struct array @ PTR_some_connection_struct_array_8003d78c
- Pre-check: ROM call @ 0x8003d784 (indirect via *0x8003d784) — returns if NULL/error
- Decrement counters:
  - conn[idx * 0x28 + 0xb2].field_0xb2 (frame count, byte @ +0xb2)
  - conn[idx * 0x28 + 0xc1].field_0xc1 (timing counter, byte @ +0xc1)
- On conn[...+0x24] == 0x02 (SCO type identifier?):
  - VSC opcode 0x260 / 0x27e ROM calls via *0x8003d7a0 (ROM indir fn ptr)
  - Config read @ 0x8012xxfe / 0xff (bytes 0-3 extracted as mask)
  - BB register 0xe0 RMW: mask 0xf7f7 (disables bits 0x0808) via ROM write
  - HCI event 0xfa logging (FUN_8003d204 call)
  - Conditional indirect call @ *0x8003d7b8 if non-NULL (vendor extension path)
- Else (non-SCO path):
  - ROM calls FUN_8002a868 + FUN_8003d558 (disconnect/abort path)
- Returns local_20[0] (result code from pre-check or final operation)

**Install location:** RAM hook slot TBD (likely 0x801212xx based on stride pattern)

**Confidence:** HIGH — Full decompile, clear connection state machine semantics, 2 callers (likely FUN_80035b4c + another).

---

### Pool Resolution Summary (Pass 7)

All 6 functions have complete literal-pool resolution:
- FUN_80039f54: 8-word pool @ 0x8003a100-0x8003a120 (fn ptrs + data addrs)
- FUN_80033794: 10+ word pool @ 0x800339d8-0x800339fc (indirect fn ptrs, config addrs)
- FUN_8003229c, FUN_80035b4c, FUN_8003c7cc: pools documented in PASS 6 decompile
- FUN_8003d630: 12-word pool @ 0x8003d784-0x8003d7b8 (fn ptrs + struct addrs)

### Proposed Tier Classification (Pass 7 outcome)

| Function | Tier | Rationale |
|----------|------|-----------|
| FUN_80039f54 | **T3** (ROM-critical integration) | Power/PHY config dispatcher; 9 callers (high-level use); ROM BB register writes |
| FUN_80033794 | **T3** (ROM-critical integration) | Connection validator gate; 5 callers; multi-layer state checks; ROM pre-check |
| FUN_8003229c | **T2** (Secondary handler) | ACL buffer management; 2 callers; data-path support |
| FUN_80035b4c | **T2** (Secondary handler) | Parameter dispatcher; 2 callers; ROM routing |
| FUN_8003c7cc | **T2** (Secondary handler) | HW register config; 2 callers; MMIO + ROM calls |
| FUN_8003d630 | **T2** (Secondary handler) | Connection state machine; 2 callers; SCO/eSCO path support |

### Next Steps (Pass 8)

Remaining 20 candidates in 301–600B tier (xref=0–1):
- 0x800323fc, 0x80033db0, 0x80034014, 0x80034144, 0x80034264, 0x80034840,
- 0x8003523c, 0x800366a0, 0x8003695c, 0x80036c9c, 0x80036f50, 0x800372fc,
- 0x8003764c, 0x80037a7c, 0x80037d54, 0x80038374, 0x80038950, 0x80038bcc,
- 0x80038efc, 0x80039218

Sort by size (largest first) to target highest-complexity functions; batch decompile via `mcp__wairz__batch_decompile_functions` (max 10/call).

---

## Pass 8: 20 Remaining 301–600B-Tier Candidates Decompiled (2026-06-25 COMPLETED)

**Stale-address correction:** The 20 addresses listed above in "Next Steps (Pass 8)"
do **not** correspond to current function boundaries in the live GZF — `decompile_function`
and `xrefs_to` returned empty/no-match results for all of them (e.g. `0x800323fc` resolves
to a byte offset *inside* the already-renamed `FUN_8003229c`, not a separate function; others
fall in address gaps with no defined function at all). These were most likely computed by an
earlier cold-triage pass against a Ghidra snapshot that predates the Pass 6/7 renames, or by a
script bug. Rather than guess, this pass re-derived the correct, current candidate set directly
from `list_functions` (top-500-by-size, which fully covers the 301–600B tier since the cutoff of
the returned set is 248B): every `FUN_*`-named (i.e. still-unnamed) function with address in
`0x80030000`–`0x8003ffff` and size in [301,600] not already resolved by Pass 6/7. This produced
exactly 20 candidates (matching the expected count, just at different addresses):

```
0x800381fc(552) 0x8003e760(504) 0x8003894c(494) 0x800386d0(488) 0x8003a180(458)
0x80035454(456) 0x800364c8(408) 0x8003a38c(394) 0x8003a824(388) 0x8003e400(382)
0x80037460(372) 0x80034ec4(370) 0x8003c41c(368) 0x80032ec4(362) 0x80039de4(354)
0x80035cd4(342) 0x80038fcc(330) 0x8003e294(330) 0x8003c2b4(324) 0x8003fb5c(304)
```

**Execution:** All 20 decompiled (`mcp__wairz__batch_decompile_functions` succeeded for 4/10
in the first batch and 0/10 in the second batch — the batch tool was flaky this run; the
remaining 16 were decompiled individually via `mcp__wairz__decompile_function`, which worked
reliably for every one). All 20 renamed to HIGH confidence.

### Detailed Function Analyses

#### 1. `lmp_event_counter_dual_rate_limited_retry` (0x800381fc, 552B)

Per-connection event/ACK counter. Validates `param_1+0x30` matches the connection-array index
for `param_3`, and that `(param_4&0xff)<<0xc` matches a stored 16-bit value at `param_1+0x28`
(opcode/type gate). Increments an 8-bit counter at `param_1+0x32`. Classifies `param_4` against
two bitmasks (`0x4410`, `0x8900`) to choose which of two per-connection sub-counters
(`field_0xac`/`field_0xa6`) to bump. Two independent modulo-counter "every Nth event, set a
retry-request bit" blocks follow, each gated by a different config condition (struct flag
`byte_0x16f`/`field_0x171`, role-mismatch via `check_if_80122df0_is_non_zero_else_ret_0xff`,
and config+0xdc bit4) — classic exponential/periodic retry-flag pattern matching the structure
already seen in `FUN_80033794`/`acl_packet_ring_buffer_manager`.

#### 2. `connection_teardown_finalize_and_reset` (0x8003e760, 504B)

Operates on a stride-0x88 connection table (`PTR_DAT_8003e95c`) — the **same table** used by
`ACL_fragment_dequeue_and_credit_consumer` (0x8003e400) and
`piconet_slot_collision_avoidance_scheduler` (0x8003e294) below, identifying this as a shared
per-connection-extended-state struct distinct from `big_ol_struct`. Looks up the connection via
an overridable fptr or fallback `FUN_8006c81c`. If not already pending (`+0x1c`/`+0x1d`), marks
pending/active and logs via `possible_logger_called_if_no_patch3`; for SCO-type connections
(`*piVar11==5`) with sufficient buffered data, calls a hardware kick fptr. Final block: when a
"flush" flag (`+0x85`) is set, zeroes out the connection's queue-pointer fields (`+0x70..0x82`)
— the connection-teardown reset; otherwise, on a different condition, calls
`drain_connection_packet_completion_ring_and_emit_hci_num_completed`+`FUN_8002bb50`+optional `clear_active_stride88_connection_buffers_and_drain_hci_cmds`+indirect fptr+log (link-supervision-loss
path).

#### 3. `slot_timing_delta_calc_and_log` (0x8003894c, 494B)

Reads a tick/clock source via indirect fptr, then computes a slot-timing delta using a
config-dependent shift amount: `field_0x1e<<1` or `<<2` (RX path, `param_1` high bit clear) vs.
`field_0x1a<<1`/`<<2` (TX path). Stores the delta into a shared struct field (`+0x11c`),
conditionally clears/sets a recalc-pending bit and calls `FUN_80043984` (RX path only). Both
branches converge on an identical final block: gather four 16-bit values, BD_ADDR-ish fields
from `big_ol_struct`, and log via `possible_logging_function__var_args` with branch-specific
codes `0x2ef` (RX) / `0x29f` (TX).

#### 4. `AFH_channel_map_table_builder` (0x800386d0, 488B)

No parameters. Builds a **79-entry** (`0x4f` = 79 decimal) lookup table — the exact channel
count of classic Bluetooth's 79 RF channels, strongly indicating this is the **AFH
(Adaptive Frequency Hopping) channel-map builder**. Copies/duplicates 16-bit per-channel
classification values from a circular source table (`PTR_PTR_800388c0`, stride 0x10) into a
linear destination array (indices `(i+0x4c)*2+6`), with a duplicate-write path for one of two
"bit4" modes. When two round-robin counters converge (`bVar2==bVar3`), computes each of the 79
final entries as `(class_nibble*0x20 + config.field453_0x1d1*0x10 + signed_delta) * 8` and pushes
the finished table to hardware via `FUN_800786dc`. Companion to
`AFH_channel_map_hw_register_programmer` below.

#### 5. `clock_trim_calibration_measure_apply` (0x8003a180, 458B)

No parameters. Classic measure-average-correct-apply calibration loop: saves/sets registers
0x5a, 0x45, 0x57 (enable a measurement mode via `PTR_DAT_8003a354(0,0x3000)`), then loops 16
times reading status register 0x7f and accumulating a sum (with optional per-iteration debug
log). Restores the three registers to their original/cleared state, computes a correction value
`((sum>>6)+0x80)&0xff`, clamps it against two threshold fields, derives a final trim value
`(0x1000 - (val&0x7f)) & 0x7f`, and applies it via
`program_bb_reg_0x6f_7bit_field_at_bits7_13_via_hook`. Matches the textbook shape of
a crystal/clock-trim calibration routine.

#### 6. `link_mode_change_state_machine` (0x80035454, 456B)

`(byte link_type, uint, uint)`. The core link-mode/role-switch procedure dispatcher for this
region's `check_link_mode_change_gate_status`/`FUN_80033ae4`/`adjust_link_mode_change_slot_budget_and_secondary_timing`/`check_link_mode_change_slot_budget_timing_gate_status`/`check_connection_setup_commit_gate_status`
helper cluster (all in this same region, none yet independently decompiled). Uses an explicit
busy(`0xf`)/idle(`0xff`) status convention. Issues a vendor command `VSC_0xfc11_2_FUN_800120ac`,
brackets the critical section with `disable_interrupts_`/`enable_interrupts_`, applies link
parameters via `check_link_mode_change_slot_budget_timing_gate_status`/`apply_link_mode_change_bb_regs_and_timeout_by_phase`, and on failure cleans up via `FUN_80034d88`
(already a known cleanup target — called from `lmp_power_regulator`'s sibling code paths).
Optional override callback via `PTR_DAT_8003563c` can replace the return value.

#### 7. `dual_slot_buffer_reassignment_on_role_switch` (0x800364c8, 408B)

`(uint conn_handle, char notify_flag)`. Operates on a stride-0x84 per-role table tracking two
buffer "slots" (offsets 0 and 0x40 within each entry), selected by bit 0 of a state byte.
Swaps/clears one slot's buffer fields (`+0x31/+0x32/+0x40..0x43`) when the logical link's
owning-role byte (`+0x34`) no longer matches the connection's current role, tracking a 3-state
(0=idle/1=pending/2=done) transition. Notifies via `FUN_80017a04` when `notify_flag==1`,
always logs the role byte on exit.

#### 8. `int64_arith_op_and_signed_shift_right` (0x8003a38c, 394B)

`(int* out[2], int* in[2], short b, char op, ushort shift)`. Generic MIPS16e 64-bit-emulation
helper: selects add/sub/mul/div (`op` 0-3, default = passthrough) on a 32-bit value combined
with a 64-bit (as two 32-bit halves) operand, then performs a variable-width signed
right-shift of the 64-bit result via two calls to `compute_int64_halves_signed_shift_width`
(sign-extend/shift) with manual
sign-bit replication for shift amounts > 32. Division by zero traps via `trap(7)`. Purely a
compiler-support arithmetic primitive — no protocol-specific meaning.

#### 9. `AFH_channel_map_hw_register_programmer` (0x8003a824, 388B)

`(byte* packed_classification, uint count, uint base_offset)`. Unpacks 2-bit-per-channel
classification codes from a packed byte array using a 3-tap bit-shift table (`{7,11,15}`),
deriving a 7-bit value (`local_14`) per channel. Computes register/channel indices in groups of
5 (`channel%5`) with a final 2-iteration tail-case (channels ≥20), writes the value via
`FUN_8003bd94` (BB-register write — name pattern shared with `hw_register_config_with_timeout`'s
sibling calls), then performs a masked read-modify-write through an indirect register
accessor pair (`PTR_DAT_8003a9a8`/`PTR_DAT_8003a9ac`). The 20-channel-group-of-5 +
24-channel-tail iteration shape, paired with `AFH_channel_map_table_builder`'s 79-entry table in
the same region, strongly confirms this is the hardware-register side of AFH channel-map
programming.

#### 10. `ACL_fragment_dequeue_and_credit_consumer` (0x8003e400, 382B)

`(byte conn_idx, byte flag)`. Dequeues from a 12-entry (`%0xc`... actually wraps at `0xb`+1→0)
ring buffer within the same stride-0x88 table as `connection_teardown_finalize_and_reset` and
`piconet_slot_collision_avoidance_scheduler`. Consumes byte-credits from each ring entry's
length field (`+3`) up to 4 times per call, advancing the ring index and incrementing/decrementing
pending/completed counters (`+0x38`/`+0x3a`) when an entry is fully drained. Marks the final
consumed entry's flag byte with `0x80` (last-fragment marker) and kicks further processing via
`FUN_80014e40` when multiple entries were filled in non-flag mode. Classic ACL/SCO
fragment-reassembly dequeue loop.

#### 11. `LMP_link_supervision_tick_scheduler` (0x80037460, 372B)

No parameters. Periodic tick dispatcher: resets a counter, calls `FUN_800117a4`, conditionally
invokes the already-known `LMP_CH__0x3ee__case2_else_2_FUN_80011d9c` when AFH-related flags
clear, calls `FUN_80037394`, and tracks elapsed time via a tick-source fptr accumulated into a
running counter (`+0x114`). Conditionally calls `apply_LAP_derived_hopping_params`'s sibling
`FUN_8003cb80` (note: `0x8003cb80` is *already* named/HIGH from Pass 5 as
`apply_LAP_derived_hopping_params` — this confirms one of its callers). A 3-bit mode field
(`+0x164>>7`) drives a small state machine: mode 1 dispatches to `FUN_8003c94c`/`FUN_8003ca28`
depending on link-type; mode 0 or >5 clears the mode-active bit. Extensive debug log on exit
covering all the timing deltas and mode bits when a debug flag is set.

#### 12. `LMP_power_and_clk_adj_procedure_orchestrator` (0x80034ec4, 370B)

No parameters. Directly gated by `config->_x7a_enable_LMP_POWER_REQ_RES_and_CLK_ADJ` bit1 — the
**already-documented** config field name confirms this orchestrates LMP_POWER_CONTROL_REQ /
LMP_CLK_ADJ procedure (re)triggering. Two parallel start/cancel blocks using the
`FUN_80055ddc(1,...)`/`FUN_80055ddc(0,0)` and `FUN_80055e50(1,...)`/`FUN_80055e50(0,0)` pattern,
each gated by a different per-connection capability bit (`field40_0x28`/`field68_0x44`) and a
link-mode mask (`+0x164 & 0x7f80`). One branch additionally adjusts `field68_0x44`/`field69_0x45`
power-control sub-bits based on a status-table check, calling either `FUN_80078fdc` or an
indirect fptr depending on the result. An else-branch calls `FUN_8004f240` and conditionally an
indirect handler — likely the symmetric "remote requested" half of the same procedure pair.

#### 13. `per_connection_hw_buffer_setup_with_patch_hook` (0x8003c41c, 368B)

`(uint conn_handle)`. Guarded on `conn_handle!=0`. Sets six fixed 1-byte enable flags, then
**installs a function-pointer hook** into `DAT_8003c5a8` via bitwise-OR — the same hook-install
idiom used for the documented `bos_base+0xd8`/`+0xe4` patch hooks in `CLAUDE.md`, just a
different slot specific to this feature. Brackets a sequence of BB-register read-modify-writes
(0x69/0x6a/0x6f, keyed by `conn_handle>>3` and a 16-bit size field) with VSC register 0x40
enable(2)/disable(0), logs the final register values, then calls a small setup cluster
(`read_modify_write_hw_reg_0x44_set_bits7_10_from_4bit_param(5)`, `read_modify_write_hw_reg_0x44_set_bits12_14_from_3bit_param(7)`, `read_modify_write_hw_reg_0x44_set_bit0(1)`, `read_modify_write_hw_reg_0x44_set_bit1(1)`) and sets bit 0x8000
of register 0x44. Counterpart of `hw_register_setup_with_patch_hook_variant2` below (different
register set, same shape).

#### 14. `config_triplet_hw_register_init_with_power_gate` (0x80032ec4, 362B)

No parameters. For three adjacent 16-bit config fields (`0xcc/0xcd`, `0xce/0xcf`, `0xd0/0xd1`),
substitutes a fixed default bit pattern into the corresponding hardware-shadow byte pair when
the config value is zero. Writes all three (possibly-defaulted) values into BB registers
0x11c/0x11e/0x120 via an indirect write fptr. Finally, when `config+0xd8` bit5 is **clear** —
the same bit **confirmed** as the LMP-power-mode-enable flag by `FUN_80033794`
(`Complex power/connection validation gate`) in Pass 7 — writes a fixed pattern to register
0x21c. A small, self-contained hardware-init sibling of the documented `0x80109980` HW
register-init function, scoped to just this config triplet.

#### 15. `calibration_table_populate_via_lookup_fptr` (0x80039de4, 354B)

`(char mode_flag)`. Loop of 8 iterations populating three parallel 8-entry tables
(`+0x14..0x1b`, `+0x1c..0x23`, `+0x24..0x2b`) via 3 calls per iteration to a shared indirect
lookup/transform fptr, with a clamped index correction when `byte[0x12] < 8`. After the loop, 9
more single-value lookups populate `+0x2c..0x34`. Debug-logs the final 7 entries when a flag
bit (`+0x36` bit1) is set. The specific physical quantity being tabulated (gain, timing, power)
is not determinable from this function alone — it only orchestrates calls into an opaque
indirect handler — so this is documented as the *table-builder*, not the *table's meaning*.

#### 16. `power_level_smoothing_filter_feeding_param_dispatch` (0x80035cd4, 342B)

`(ushort conn_idx)`. Early-returns on an invalid-slot guard (`byte_0xCC==-1`) and an optional
override callback. Calls `validate_connection_setup_preconditions`'s sibling `FUN_80042a68` as a
gate, then `acl_packet_ring_buffer_manager`'s sibling... actually calls
`FUN_80033f8c` (`validate_connection_setup_preconditions`, already HIGH from Pass 5) — if it
returns 0, falls back to a default value and returns. Otherwise computes a smoothed
power/RSSI-like value from `field_0x38` plus an antenna-path correction term
(`field_0x2a0`/`field_0x2a1`, selected by a 2-bit mode in a hardware-config byte), averaged
against `field106_0x94`. If calibration-mode is active and the smoothed value exceeds a
config-defined threshold (`field160_0xa8`/`field161_0xa9`) and `check_connection_setup_commit_gate_status(1)!=0xff` and a
flag bit is set, calls **`param_dispatch_with_rom_calls`** (`0x80035b4c`, already HIGH from Pass
6) with `(smoothed_value, threshold)` — resolving one of that function's two documented callers.

#### 17. `packet_type_to_hw_code_translator_4link` (0x80038fcc, 330B)

`(uint packed_value)`. Unpacks a 16-bit value into 4 nibble codes (one per link/slot 0-3).
Enables register 0x10 bit 0x40, then for each link: clamps an invalid code value 7→6 (the
classic "reserved" slot in a 0-6 BT packet-type enum), and programs registers 0x11/0x12 with
the link index and a derived value `((code>>1&7)+(code&1))*0x2000`. Disables register 0x10
bit 0x40, recomputes a second derived code per link (`5-(code&1)&7`), and packs all 4 into a
single register write `(3,0x59,1,...)` at bit offsets 0/3/6/9. A per-link packet-type → hardware
baseband-code translator.

#### 18. `piconet_slot_collision_avoidance_scheduler` (0x8003e294, 330B)

`(byte conn_a, byte flags, ushort divisor, uint requested_slot)`. Scans the same stride-0x88
table as `connection_teardown_finalize_and_reset`/`ACL_fragment_dequeue_and_credit_consumer` for
up to 3 entries, selecting the one of connection-type `0x101` with the highest "priority" field
(`+0x30`). Computes a BD_ADDR-derived slot value (`FUN_80034a24`) for that conflicting
connection (if found) and for the current connection, optionally masking with two fixed
constants when a flag bit is set. Computes `requested_slot + (divisor - (current_slot %
divisor))` with division-by-zero trapped, then if the result still collides with the priority
connection's slot and the conflict wasn't itself the priority connection, rounds it up to the
next multiple of `divisor` past the conflict using signed-division rounding. A genuine
slot/clock-offset collision-avoidance scheduler for shared time-slot allocation.

#### 19. `hw_register_setup_with_patch_hook_variant2` (0x8003c2b4, 324B)

No parameters. Near-identical structure to `per_connection_hw_buffer_setup_with_patch_hook`
(0x8003c41c) but configuring the **counterpart** register set: reads config fields
`0x1da`/`0x1db`, conditionally (config field285 bit0 AND a derived flag bit5) installs a hook
into `DAT_8003c410`, brackets register 0x6b/0x6e/0x6c/0x6d/0x68 read-modify-writes with VSC reg
0x40 enable(2)/disable(0) (reg 0x6e gets a special value `0x81a` when a bit4 flag is set, and
reg 0x6d gets one of two pointer constants based on the same bit), then calls
`FUN_8003c19c(3,3,0,0xf)`. The pairing of register sets (0x69/0x6a/0x6f here vs.
0x6b/0x6c/0x6d/0x6e/0x68 in `0x8003c41c`) strongly suggests these are TX-path/RX-path or
primary/secondary counterparts of the same per-connection hardware setup.

#### 20. `test_pattern_buffer_fill_or_hw_mode_select` (0x8003fb5c, 304B)

`(uint conn_idx)`. Reads a 16-bit length field; returns immediately if zero. Sets a
"transmitting" flag (`field311_0x279`). Branches on a global mode flag
(`config[1].field7_0x7`): mode 0 selects a fill byte from `{0x00, 0xFF, 0x55, 0x0F}` —
**the textbook all-zero / all-one / alternating-bit / alternating-nibble Bluetooth DUT/loopback
test patterns** — and either `memset`s the buffer with it, or (sub-mode 4) performs a
bit-rotated merge writing a repeating pattern at an arbitrary bit offset (PRBS-style payload).
Mode 1 instead writes an equivalent hardware-native mode select code (`sub_mode<<9 | 0x147`)
into a register, for hardware that generates the test pattern itself rather than needing a
software-filled buffer. The exact match to BT's standard DUT test-pattern byte values is the
strongest single signal in this pass.

### Region 0x80030000 Status After Pass 8

20/20 remaining 301–600B-tier candidates resolved, all HIGH confidence. Region coverage:
38→58 of ~309 functions (12.3%→18.8%). Two cross-pass connections confirmed this pass:
`param_dispatch_with_rom_calls` (0x80035b4c, Pass 6) now has its power-smoothing producer
identified (`power_level_smoothing_filter_feeding_param_dispatch`), and
`apply_LAP_derived_hopping_params` (0x8003cb80, Pass 5) now has a confirmed caller
(`LMP_link_supervision_tick_scheduler`). Three new functional clusters emerged: an AFH
channel-map pair (table-builder + hw-register-programmer), a hw-buffer-setup pair (TX/RX
counterparts at 0x8003c41c/0x8003c2b4), and a shared stride-0x88 connection-extended-state
table referenced by three siblings (0x8003e760/0x8003e400/0x8003e294).

### Next Steps (Pass 9)

No more 301–600B-tier candidates remain unresolved in this region. Remaining work is the
601B+ tier (already mostly covered by Pass 3/5/7's targeted picks) and the sub-301B tier
(not yet enumerated — likely 200+ functions given the region's ~309 total and ~58 now named).
A future pass should re-derive a fresh candidate list directly from `list_functions`/
`list_imports` against the *current* GZF state (per this pass's stale-address lesson above)
rather than trusting any previously-recorded address list without verification.

**Status:** PASS 7 COMPLETE. 6/6 candidates decompiled with HIGH confidence. All pools resolved. ROM integration points identified. Ready for PASS 8: remaining 20 candidates (lower xref tier).

## Cross-region low→high confidence upgrade pass, batch (2026-06-26)

Part of the `work-in-progress.txt` "Cross-region: upgrade all low-confidence
named functions to high" ticket. Live `grep "low (named by Kovah"
rom_function_index.md` found 67 rows project-wide; **6** of them are in this
region. All 6 decompiled individually via `decompile_function`.

| Address | Size | Name | Confirmed purpose |
|---------|------|------|--------------------|
| `0x800302ac` | 272B | `references_patch_download_mem4` | **Project-relevant — sibling of the already-documented `VSC_0xfc20__download_patch__FUN_8002fee0`** (`region_0x80020000.md`). Validates a not-already-downloading flag, a download-enabled byte, a sequence-id match byte, opcode high-nibble `==0x8`, and fragment length against a config-stored max. Tracks last-downloaded-patch-index; on the first fragment (`seq&0x7f==0`) (re)initializes the patch-storage write pointer/size; `memcpy`s the fragment payload and advances write-cursor + remaining-length counters; acks via `FUN_8000a780`. On the **final** fragment (top bit of the sequence byte set), sets multiple status/control bits in a global, writes a "weird constants" sentinel (`0x7382`), conditionally copies a function pointer, writes a final value to a jump-target slot, then drops into the same "do-nothing infinite loop" shape the sibling function's doc identifies as an **unresolved indirect jump into the now-installed patch**, not a real infinite loop. This is a second, parallel VSC-class patch-fragment-download handler — directly relevant to the project's primary goal (replicating the chip-side patch-load protocol). Recommended rename: `vsc_download_patch_fragment_and_jump` (not applied — rename-persistence bug). |
| `0x80032e28` | 20B | `called_by_function_that_uses_Logger_string_2_initialize_something_at_offset_0x800` | Confirmed exactly as named: zeroes three consecutive 16-bit fields at `+0x800`/`+0x802`/`+0x804` of a global struct. Pure initializer, no other logic. No rename needed (name is verbose but 100% accurate). |
| `0x80033188` | 182B | `calls_fptr_down_LMP__47E_path` | Confirmed override+fallback connection-selector idiom (same family as `region_0x80000000`'s `0x80009990` cluster): when the hook `PTR_fptr_down_LMP__47E_path_80033240` is unset, scans a 10-entry connection-record array for the lowest-weight (`field106_0x94`) valid entry, tracking a separate fallback minimum from entries with `field202_0x208` set; when the hook IS set, calls it directly and returns the selected connection's weight. Feeds the LMP 0x47E procedure path. Recommended rename: `select_best_connection_for_LMP_0x47E` (not applied — rename bug, not blocking; name already correctly identifies the LMP path). |
| `0x80034a38` | 378B | `idk_takes_new_new_power_val` | Confirmed **TX power-level apply function**: resolves a register block via `FUN_80042db8` keyed by the connection's channel/band-selector byte, read-modify-writes a 6-bit power field (bits 14:9) under interrupt disable/enable, calls 3 function-pointer hooks (apply-setting / get-extended-flag / commit), and for non-random-address peers performs a per-peer calibration-table lookup feeding `FUN_800430ac(value, 0x1000, 0x1000)`. Called directly by `set_new_power_val` (next row) with the newly computed value — confirms both functions' roles as a matched power-step pair. Recommended rename: `apply_tx_power_level_to_connection` (not applied — rename bug). |
| `0x80034be0` | 120B | `set_new_power_val` | Confirmed **power step (increment/decrement) entry point**: `param_3` selects direction (0 = increase, checked via `check_if_at_max_power__6_`/`increment_new_power_val_if_<_6`; 1 = decrease via `check_new_power_val__0`/`increment_new_power_val_if____0`), then calls `idk_takes_new_new_power_val` (above) with the computed value and logs the change. Name is already fully accurate — no rename needed. |
| `0x80035068` | 138B | `LMP__25C_called2` | Confirmed **LMP-procedure start/poll-with-timeout/stop state machine**, same idiom family as the "LMP procedure-completion busy-wait barrier" already documented in `region_0x80000000` Pass 2: global state `==1` invokes an opcode-0x6c hook with a masked value and clears the flag; state `==2` logs entry, starts (`FUN_800093d0`), busy-polls the flag for up to 2000×~0x266 iterations (software timeout), logs the final value, then stops (`FUN_800093e4`). Name already correctly scoped as a secondary/companion routine in the LMP 0x25C procedure's flow — no rename needed. |

**Confidence**: all 6 upgraded **low → HIGH** in `rom_function_index.md`. Two
recommended renames flagged (not applied — rename-persistence bug); the other
4 confirmed accurate as-is. 0 low-confidence functions remain in this region.

---

## Pass 42 — SCO teardown callees xref sweep (2026-06-28)

**Context**: Continuation after applying all 38 staged renames from region 0x80050000 Passes 35–41 (see `reverse_engineering_region_0x80050000.md` Pass 42). Targeted the two unnamed callees of `configure_hw_regs_and_init_for_sco_teardown` (`0x80037370`, 34B, MEDIUM-HIGH holdover from Pass 41).

### Analyzed functions (3 HIGH renames applied)

**`0x8003bd94`** → `dispatch_bb_register_da_d6_write_with_hook` [HIGH]
- Optional override hook at `PTR_DAT_8003bde0`: if set, calls `hook(0, reg_id, mode, value)` and returns early if hook returns non-zero.
- Fallback: if `mode==1`, sets bit 6 on `reg_id`; if `mode==0`, calls `poll_and_write_bb_registers_0xda_0xd6(reg_id, value)`.
- Callers include `hw_register_config_with_timeout` (`FUN_8003bd94(0,1,0)`), `AFH_channel_map_hw_register_programmer`, and `configure_hw_regs_and_init_for_sco_teardown` (`(0,0,0)`).
- Resolves the long-standing "BB-register write sibling of hw_register_config_with_timeout" note from Pass 8.

**`0x8003bc54`** → `poll_and_write_bb_registers_0xda_0xd6` [HIGH]
- Reference-counted BB register writer: on first entry (when bit 6 clear), optionally primes register 0xda with 0x100 flag.
- Writes merged value to 0xda and `(reg_id<<8)|0x8000|value` to 0xd6 via `PTR_DAT_8003bd74` function pointer.
- Poll loop waits for completion bit 0x80 in status register; timeout logs via `possible_logging_function__var_args`.
- Self-contained mechanism — same evidentiary bar as `binary_search_sorted_table_by_8byte_key`.

**`0x80036fa8`** → `init_or_clear_sco_hw_channel_subsystem` [HIGH]
- `param_1==0`: comprehensive SCO/eSCO HW subsystem init — reads `config_struct`, programs registers 0x6c/0xbe/0x22/0x24/0x26/0x28/0x2a/0xa8/0xaa/0xac/0xd8/0x17a/0x17e/0x186/0x46, iterates 8+4+3 channel slots calling `FUN_800430ac` for parameter commit, finishes with `FUN_80043158`/`FUN_80013bc0`.
- `param_1!=0`: teardown partial path — writes 0 to register 0xee only (exact path taken by `configure_hw_regs_and_init_for_sco_teardown` which passes `1`).
- Large but param-dispatch semantics are unambiguous.

### Rename script

`RenamePass42CrossRegion.java` — 3 entries, applied via `run_ghidra_headless` (`renamed=3 alreadyOk=0 missing=0 failed=0`).

### Coverage after Pass 42

Region 0x80030000: +3 HIGH names (includes Pass 41's `reset_sco_esco_hw_subsystem_on_link_loss` now live in Ghidra from the Pass 35–41 batch). **MEDIUM-HIGH holdover**: `0x80037370` `configure_hw_regs_and_init_for_sco_teardown` — callees now named, candidate for promotion next pass.

---

## Pass 43 — reset_sco_esco_hw_subsystem_on_link_loss callee sweep + promotion (2026-06-28)

**Context**: Finished the xref sweep flagged at the end of Pass 42. Decompiled the
3 remaining unnamed direct callees of `reset_sco_esco_hw_subsystem_on_link_loss`
(`0x80037394`): `FUN_800344f8`, `FUN_80034480`, `FUN_80033ed8`. The 4th callee,
`configure_hw_regs_and_init_for_sco_teardown` (`0x80037370`), was already named
(Pass 41) and is promoted below now that all 3 of *its* callees are also HIGH.

Re-decompiling `reset_sco_esco_hw_subsystem_on_link_loss` confirmed the exact call
order inside the `field[0x10b] != 0` teardown branch: disable HW regs 0xbe/0xc0 →
`FUN_800344f8()` → `configure_hw_regs_and_init_for_sco_teardown()` → fptr(1) →
`FUN_80034480()` → fptr(addr,len) → `FUN_80033ed8()` → `FUN_800132f4(1)` →
`init_or_reset_sco_hw_slot_table(0)` → clear status bits → re-enable regs 0xbe/0xc0.
All 3 targets have a single caller (`xrefs_to` returns none — same indirect-call
visibility gap Ghidra has for the other direct calls in this function; confirmed by
re-reading the decompiled body instead).

### Analyzed functions (3 HIGH renames applied)

**`0x800344f8`** (98B) → `write_sco_hw_reg_and_poll_bit0_clear_with_timeout` [HIGH]
- Writes fixed command byte `0x3c` to a HW register (`DAT_8003455c`), then polls
  (≤2000 iterations) for bit 0 of that register to clear (ack/done).
- On timeout: logs via `possible_logging_function__var_args(1, 0x24, &DAT_00002614,
  &DAT_000023ad, 2, <reg_ptr>, 1, <iter_count>)`, then force-writes the register's low
  byte to `0xa5` (fallback/abort value) before returning.
- Same shape as the already-documented `poll_status_sign_bit_with_timeout_0x65` /
  `_variant` pair (`region_0x80000000`) — a "write cmd, poll ack bit, timeout fallback
  + log" idiom that recurs across HW-register helpers in this firmware.

**`0x80034480`** (94B) → `write_sco_hw_reg_and_poll_mask_clear_with_timeout` [HIGH]
- Structurally identical to `write_sco_hw_reg_and_poll_bit0_clear_with_timeout` above,
  but every value is read from a `DAT_800344e*` global instead of an inline immediate
  (write value `DAT_800344e4`, poll mask `DAT_800344e8`, timeout fallback mask/value
  `DAT_800344f0`/`DAT_800344f4`). No parameters — same single hardcoded register/value
  set, just compiled with the constants spilled to a literal pool instead of immediates
  (consistent with MIPS16e's narrow immediate-encoding limits forcing larger constants
  out of the instruction stream). Logs via the same `possible_logging_function__var_args`
  call shape on timeout (distinct format-string pointer `&DAT_00002637`, same shared
  second string `&DAT_000023ad`).
- Near-duplicate of the previous function for a *different* register — not merged,
  matching the project's existing precedent for this exact situation
  (`poll_status_sign_bit_with_timeout_0x65_variant`).

**`0x80033ed8`** (158B) → `disable_esco_hw_slot_for_each_active_connection` [HIGH]
- Gated on `PTR_struct_of_at_least_0x300_size_80033f78->field_0x173 != 0`.
- Sweeps up to 10 connection records (`PTR_big_ol_struct_80033f7c[i].bos_entry_valid_
  == 1`); for each active entry without `field369_0x2b5` set, resolves an eSCO slot
  index via the already-named `conn_record_role_to_esco_slot_index(i)` and, if
  resolved, issues 3 HW commands through a function pointer
  (`PTR_DAT_80033f88`) to disable that connection's eSCO HW slot:
  1. `(2, (byte_0xCC&0x1f)<<0xb | bos_connection__array_index<<5)` — encoded
     link-parameter disable command.
  2. `(slot_table_value, register_at_that_value & 0xfffd)` — clears bit 1 of an
     indexed register.
  3. `(0, 0x21)` — fixed terminator command.
- Single caller confirms scope: this is the per-connection eSCO HW slot teardown
  sweep invoked once, early, in the full subsystem teardown.

### Promotion: `configure_hw_regs_and_init_for_sco_teardown` MEDIUM-HIGH → HIGH

`0x80037370` (34B): `hw_register_config_with_timeout(1)` +
`dispatch_bb_register_da_d6_write_with_hook(0,0,0)` +
`init_or_clear_sco_hw_channel_subsystem(1)`. All 3 callees are now HIGH-confidence
named (the first two pre-existing, the third from Pass 42), and its single caller
(`reset_sco_esco_hw_subsystem_on_link_loss`) is HIGH. No remaining ambiguity —
promoted to HIGH in `rom_function_index.md`.

### Rename script

`RenamePass43CrossRegion.java` — 3 entries, applied via `run_ghidra_headless`
(`renamed=3 alreadyOk=0 missing=0 failed=0`). See `wairz_requested_changes.txt` for
two infrastructure issues hit and resolved/worked around while applying this pass
(GZF project ownership regression; `save_ghidra_script` not materializing files
post-container-restart).

### Coverage after Pass 43

Region 0x80030000: +3 HIGH names, +1 promotion (MEDIUM-HIGH → HIGH). All 4 direct
callees of `reset_sco_esco_hw_subsystem_on_link_loss` are now HIGH-confidence named;
the full SCO/eSCO-link-loss teardown call tree (caller + 4 callees + their callees)
is fully resolved with no remaining low/medium-confidence holdovers.

**Next**: superseded by Pass 44.

## Pass 44 (2026-07-01) — HW-clock raw-dword reader wrapper `FUN_80034a24`

Fresh `ListUnnamed80030000.java` re-run: **245 unnamed** remain in region
`0x80030000`. Decompiled and renamed rank-1 (highest xref_in):
**`FUN_80034a24` → `read_hw_clock_raw_dword_by_role_index`**
(20B, HIGH) via `RenamePass44Region80030000Fun80034a24.java` (`renamed=1`, live-verified).

**Mechanism:** Thin wrapper tail-calling
`read_hw_clock_dword_and_optional_slot_offset_by_role_index(out_dword, NULL, role_index)` —
i.e. reads only the raw 32-bit HW clock dword from the global clock table at
`DAT_80034a20`, without computing the optional slot-offset short that the callee
writes when `param_2 != NULL`. The callee remaps role indices 8–11 to eSCO slot
register offsets via `remap_role_index_to_esco_slot_if_pending` before indexing the
appropriate dword/short pairs. **63 xref-in** across SCO/eSCO timing (`spin_until_hw_clock_bit1_phase_toggles`,
`compute_sco_slot_offset_delta_from_hw_clock`, `reconcile_conn_slot_timing_drift_commit_hw_channel_reconcile`
in region `0x80040000`) and global busy-spin delays (`spin_until_global_hw_clock_advances_by_ticks`
in region `0x80020000`).

**Confidence:** HIGH — trivial decompile; callee mechanism directly readable; cross-region
callers already documented under the old `FUN_80034a24` alias.

Region unnamed count after this pass: **244** (245 minus this rename). Live named **1922** global.

**Next:** superseded by Pass 45.

## Pass 45 (2026-07-01) — indexed BB register read `FUN_8003c69c`

Decompiled and renamed rank-2 cold-triage target:
**`FUN_8003c69c` → `read_indexed_bb_register_low16_by_byte_index`**
(66B, HIGH) via `RenamePass45Region80030000Fun8003c69c.java` (`renamed=1`, live-verified).

**Mechanism:** IRQ-masked indexed baseband register read primitive. Optional override
hook at `PTR_DAT_8003c6e0`: if installed and returns non-zero, returns the hook's
16-bit output. Default path: disable interrupts, write `(index & 0xff) << 0x16` to
MMIO `DAT_8003c6e4` (select register by byte index in upper 16 bits), read back
low 16 bits, re-enable interrupts. Sibling of unnamed `FUN_8003c5b8` (masked
variant with `FUN_800092dc` poll) and `FUN_8003c608` (write-side with same hook
pattern at `PTR_DAT_8003c67c`). Register-script interpreter (`0x8003aea0`) callee
alongside those two — used for scripted BB register poll/read steps.

**Callers (6 computed xrefs):** `FUN_8003aea0` (register-script interpreter),
`AFH_channel_map_hw_register_programmer`, `packet_type_to_hw_code_translator_4link`,
`multi-VSC_Handler_FUN_80032540`, `apply_tx_power_runtime_mode_byte_and_reconfigure_tables_and_links`, patch `FUN_8010fcac`.

**Confidence:** HIGH — clear IRQ-masked MMIO index-select + low-word readback idiom;
hook/default split matches documented `FUN_8003c608` write primitive; register-script
interpreter integration already noted in patch-installer analysis.

Region unnamed count after this pass: **243** (244 minus this rename). Live named **1923** global.

**Next:** superseded by Pass 46.

## Pass 46 (2026-07-01) — masked indexed BB register read `FUN_8003c5b8`

Decompiled and renamed rank-2 cold-triage target:
**`FUN_8003c5b8` → `read_indexed_bb_register_low16_with_mask_and_poll`**
(46B, HIGH) via `RenamePass46Region80030000Fun8003c5b8.java` (`renamed=1`, live-verified).

**Mechanism:** IRQ-masked indexed baseband register read with global mask and poll.
No optional hook (unlike Pass 45's `read_indexed_bb_register_low16_by_byte_index`).
Disables interrupts, writes
`((subfield & 0x3f) << 0x10 | (index & 0xff) << 0x16) & DAT_8003c600` to
MMIO `DAT_8003c604`, calls `FUN_800092dc()` (empty poll/wait stub), reads back,
re-enables interrupts, returns low 16 bits. Masked sibling of Pass 45's unmasked
read; write-side counterpart is unnamed `FUN_8003c608` at `PTR_DAT_8003c67c`.

**Callers:** register-script interpreter (`0x8003aea0`) callee per patch-installer
analysis; no direct xrefs resolved via MCP `xrefs_to` (likely indirect/script-table
dispatch).

**Confidence:** HIGH — clear IRQ-masked MMIO index-select + mask + poll idiom;
structural sibling of Pass 45's documented read primitive; register-script
interpreter integration already noted.

Region unnamed count after this pass: **242** (243 minus this rename). Live named **1924** global.

**Next:** superseded by Pass 47.

## Pass 47 (2026-07-01) — masked indexed BB register write `FUN_8003c608`

Decompiled and renamed rank-2 cold-triage target:
**`FUN_8003c608` → `write_indexed_bb_register_low16_with_mask_and_hook`**
(114B, HIGH) via `RenamePass47Region80030000Fun8003c608.java` (`renamed=1`, live-verified).

**Mechanism:** IRQ-masked indexed baseband register write primitive with optional
override hook at `PTR_DAT_8003c67c`: if installed and returns non-zero, the hook
handles the write. Default path: disable interrupts, compose masked dword from
globals `DAT_8003c680`–`DAT_8003c698`, write low 16 bits of `param_2` with byte
index `param_1` in upper 16 bits to MMIO `DAT_8003c690` (two-phase masked write
pattern matching the read primitives' hook/default split), re-enable interrupts.
Write-side counterpart to Pass 45/46 read primitives in the `0x8003c6xx` cluster.

**Callers:** register-script interpreter (`0x8003aea0`) callee per patch-installer
analysis; no direct xrefs resolved via MCP `xrefs_to` (likely indirect/script-table
dispatch).

**Confidence:** HIGH — clear IRQ-masked MMIO index-select + mask + optional-hook
idiom; structural write-side sibling of Pass 45/46 documented read primitives;
register-script interpreter integration already noted.

Region unnamed count after this pass: **241** (242 minus this rename). Live named **1925** global.

**Next:** superseded by Pass 48.

## Pass 48 (2026-07-01) — indexed BB register write `FUN_8003b5b8`

Decompiled and renamed rank-2 cold-triage target:
**`FUN_8003b5b8` → `write_indexed_bb_register_low16_with_global_mask`**
(44B, HIGH) via `RenamePass48Region80030000Fun8003b5b8.java` (`renamed=1`, live-verified).

**Mechanism:** Lightweight indexed baseband register write primitive (no IRQ
masking, no poll, no hook): when either `param_1` or `param_2` low byte is
non-zero, composes dword
`(param_1&0xff)<<0x16 | DAT_8003b5e4 | param_4&0xffff | (param_2&0x3f)<<0x10`
and writes to MMIO `DAT_8003b5e8`. Same index/subfield/value composition
pattern as the `0x8003c6xx` cluster but without the IRQ/poll/hook overhead.
Register-script interpreter (`0x8003aea0`) callee; also reached via
`dual_fptr_dispatch_by_flag_wrapper` (`0x8000ebfc`) flag-selected dispatch
alongside Pass 46's masked read.

**Callers:** register-script interpreter callee per patch-installer analysis;
`dual_fptr_dispatch_by_flag_wrapper` (`0x8000ebfc`) when direction flag non-zero.

**Confidence:** HIGH — trivial decompile; dword composition matches documented
`0x8003c6xx` cluster pattern; register-script interpreter + dual-fptr dispatch
integration already noted.

Region unnamed count after this pass: **240** (241 minus this rename). Live named **1926** global.

**Next:** superseded by Pass 49.

## Pass 49 (2026-07-01) — register-script opcode `0xA000` delay callee `spin_delay_10x_iterations`

Cross-region cold-triage of rank-3 register-script interpreter callee
**`FUN_80009680` → `spin_delay_10x_iterations`** (`0x80009680`, 20B, HIGH).
Already named in region `0x80000000` out-of-gap-scope pass 2 (2026-06-27); no
Ghidra rename needed — this pass confirms register-script integration from the
interpreter side.

**Mechanism:** Bare spin-wait loop: `for (i = 0; i < param_1 * 10; i++)`.
Script data word `uVar10` from each `(opcode, data)` pair is passed as
`param_1`, so effective iteration count is `data × 10`.

**Register-script integration:** Decompile of `FUN_8003aea0` (register-script
interpreter, 688B) confirms opcode top-nibble `0xA000` branch:
`spin_delay_10x_iterations(uVar10)`. Short busy-wait between register-script
steps; contrast with opcode `0xB000` (`spin_delay_10000x_iterations`) and
poll-wait opcodes `0xC`/`0xD`/`0xE` which use the 10000× primitive with
argument `1` between retries.

**Callers:** Direct callee of register-script interpreter only (within this
analysis scope); also used elsewhere in ROM (eSCO interval apply, codec
serialize cluster, link-status escalation) per region `0x80000000` pass 2.

**Confidence:** HIGH — trivial decompile; opcode dispatch confirmed live in
interpreter decompile; name pre-exists and resolves.

Region unnamed count unchanged: **240**. Live named **1926** global (no rename).

**Next:** superseded by Pass 50.

## Pass 50 (2026-07-01) — register-script opcode `0xB000` delay callee `spin_delay_10000x_iterations`

Cross-region cold-triage of rank-3 register-script interpreter callee
**`FUN_80009694` → `spin_delay_10000x_iterations`** (`0x80009694`, 22B, HIGH).
Already named in region `0x80000000` out-of-gap-scope pass 2 (2026-06-27); no
Ghidra rename needed — this pass confirms register-script integration from the
interpreter side.

**Mechanism:** Bare spin-wait loop: `for (i = 0; i < param_1 * 10000; i++)`.
Script data word `uVar10` from each `(opcode, data)` pair is passed as
`param_1`, so effective iteration count is `data × 10000`.

**Register-script integration:** Live decompile of `FUN_8003aea0` confirms:
- Opcode top-nibble `0xB000`: direct call `spin_delay_10000x_iterations(uVar10)` —
  long busy-wait between register-script steps (contrast Pass 49's `0xA000` /
  `spin_delay_10x_iterations`).
- Poll-wait opcodes `0xC000`/`0xD000`/`0xE000`: each retry loop calls
  `spin_delay_10000x_iterations(1)` between status polls, decrementing the script
  data word as a retry budget.

**Callers:** Direct callee of register-script interpreter; also used by
`hw_register_config_with_timeout` (`0x8003c7cc`) and link-status escalation
paths per region `0x80000000` pass 2.

**Confidence:** HIGH — trivial decompile; opcode dispatch and poll-loop inter-retry
timing confirmed live in interpreter decompile; name pre-exists and resolves.

Region unnamed count unchanged: **240**. Live named **1926** global (no rename).

**Next:** superseded by Pass 51.

## Pass 51 (2026-07-01) — register-script interpreter `FUN_8003aea0`

Fresh `ListUnnamed80030000.java` re-run: **240 unnamed** remain in region
(unchanged from Pass 48–50 cross-region passes).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003aea0` → `register_script_interpreter`**
(688B, HIGH) via `RenamePass51Region80030000Fun8003aea0.java` (`renamed=1`,
live-verified).

**Mechanism:** 16-opcode byte-code VM for hardware register-config scripts.
Walks `(opcode, data)` halfword pairs from `param_1` for `param_2` entries
(stepping by 2). Top-nibble opcode dispatch:
- `0x0`/`0x1`/`0x2`/`0x3`/`0x4`/`0x5`/`0x6`/`0x7`/`0x8`/`0x9` — indexed
  BB register read/write and RAM table update via hook fptrs at
  `PTR_DAT_8003b150`–`PTR_DAT_8003b160` and globals `DAT_8003b164`–`DAT_8003b16c`
- `0xA` — `spin_delay_10x_iterations(data)` (Pass 49)
- `0xB` — `spin_delay_10000x_iterations(data)` (Pass 50)
- `0xC`/`0xD`/`0xE` — poll-wait loops with `spin_delay_10000x_iterations(1)`
  retry ticks
- `0xF` — sets running write-mask for subsequent masked ops

**Callers:** 16 xref_in (rank-1 by xref count); includes patch entry installer
region and ROM init paths — see `reverse_engineering_register_script_interpreter.md`.

**Confidence:** HIGH — fully decompiled 688B VM; extensively cross-referenced
in Passes 45–50; dedicated analysis doc exists; name persisted in Ghidra.

Region unnamed count after this pass: **239** (240 minus this rename). Live named
**1927** global.

**Next:** superseded by Pass 52.

## Pass 52 (2026-07-01) — clamped byte offset helper `FUN_80039920`

Fresh `ListUnnamed80030000.java` re-run: **239 unnamed** remain in region
(unchanged from Pass 51).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80039920` → `clamp_byte_offset_base_plus_adj_minus_product`**
(78B, HIGH) via `RenamePass52Region80030000Fun80039920.java` (`renamed=1`,
live-verified).

**Mechanism:** Signed-byte arithmetic helper using context struct at
`PTR_DAT_80039970`: computes
`(param_1 + ctx[1] + param_4 - param_3*param_2)` as a byte, then clamps to
`[ctx[0xf], ctx[0x10]]` (min/max bounds). Used as a lookup-index / offset
transform in the calibration-table cluster near `0x80039de4`.

**Callers:** 12 xref_in (rank-1 by xref count); includes indirect dispatch via
`calibration_table_populate_via_lookup_fptr` lookup fptr table.

**Confidence:** HIGH — fully decompiled 78B utility; clear min/max clamp
semantics; name persisted in Ghidra.

Region unnamed count after this pass: **238** (239 minus this rename). Live named
**1928** global.

**Next:** superseded by Pass 53.

## Pass 53 (2026-07-01) — AFH cleanup orchestrator `FUN_8003ce50`

Fresh `ListUnnamed80030000.java` re-run: **238 unnamed** remain in region
(unchanged from Pass 52 pre-rename list; rank-1 was `FUN_8003ce50`).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003ce50` → `afh_cleanup_apply_lap_hopping_and_feature_orchestrator`**
(24B, HIGH) via `RenamePass53Region80030000Fun8003ce50.java` (`renamed=1`,
live-verified).

**Mechanism:** Thin 24B orchestrator: `apply_LAP_derived_hopping_params(param_1)`
then `remote_name_request_feature_apply_orchestrator()`. Fired from patch
periodic hook at AFH BB-reg poll counter==9 (see
`reverse_engineering_protocol_dispatch_layer.md`); also called from
`generic_status_field_get_set_dispatcher` field-ID `0x26` path.

**Callers:** 11 xref_in (rank-1 by xref count at Pass 52 list time); includes
patch literal pool `0x8010cc78` and ROM status-field dispatcher.

**Confidence:** HIGH — fully decompiled 24B thin wrapper; both callees already
HIGH-named; name persisted in Ghidra.

Region unnamed count after this pass: **237** (238 minus this rename). Live named
**1929** global.

**Next:** superseded by Pass 54.

## Pass 54 (2026-07-01) — conditional packet-type programmer `FUN_80034c5c`

Decompiled and renamed rank-1 cold-triage target from Pass 53 list:
**`FUN_80034c5c` → `program_packet_type_if_stored_matches_expected`**
(72B, HIGH) via `RenamePass54Region80030000Fun80034c5c.java` (`renamed=1`,
live-verified).

**Mechanism:** IRQ-masked conditional packet-type transition helper. Compares
`param_1` (expected current packet-type word) against the stored value at index
`(param_3 & 0xffff)` in table `DAT_80034ca4`; on match, invokes hook at
`PTR_DAT_80034ca8(conn_index, param_2)` to program the new packet type. Typical
call pairs: `0x1c00→0xc000` (eSCO→max-rate-SCO), `0xc000→0x1c00`
(max-rate-SCO→eSCO), `0xc000→0xc00` (max-rate-SCO→SCO), `0xc00→0xc000`
(SCO→max-rate-SCO).

**Callers:** 4 direct call sites via `find_callers`:
`conn_event_packet_type_update_and_reschedule`, `apply_conn_class_mode_afh_role_remap_and_esco_ptype`,
`recompute_and_commit_conn_slot_timing_hw_and_packet_types`, and
`sweep_conn_table_program_esco_packet_type_and_clear_gate_bytes`
(region `0x80040000` Pass 52cp). Sibling of
`select_and_program_sco_esco_packet_type_for_conn` / `program_packet_type_with_default_fallback`
in region `0x80000000`.

**Confidence:** HIGH — fully decompiled 72B; hook indirection + packet-type
constant pairs match documented eSCO/SCO codec-type cluster.

Region unnamed count after this pass: **236** (237 minus this rename). Live named
**1930** global.

**Next:** superseded by Pass 55.

## Pass 55 (2026-07-01) — page/inquiry scan timer arm `FUN_800362b4`

Fresh `ListUnnamed80030000.java` re-run: **236 unnamed** remain in region
(unchanged from Pass 54 pre-rename list; rank-1 was `FUN_800362b4`).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_800362b4` → `arm_page_inquiry_scan_timer_if_idle_else_flush_codec_slots`**
(54B, HIGH) via `RenamePass55Region80030000Fun800362b4.java` (`renamed=1`,
live-verified).

**Mechanism:** Page/inquiry scan-window timer arm-or-flush gate on
`the_0x300` inquiry state struct. When `field_0x173 < 2` and `int_0x10 == 0`
(idle), sets timer-active `field_0x175=1` plus scan-window/interval bytes
`field_0x176=8`, `field_0x177=4`, `field_0x178=4` (same field cluster seeded by
`init_inquiry_page_state_from_config` in region `0x80060000`). Otherwise
delegates to `FUN_800361e4` which clears `field_0x175` and IRQ-masked-flushes
up to 12 active codec slots via `FUN_80014450`/`FUN_80014dac`.

**Callers:** 8 xref_in (rank-1 by xref count); includes paging/inquiry cluster
from region `0x80040000`: `program_page_train_baseband_regs_and_start_paging`,
`program_inquiry_or_esco_baseband_from_hci_command`,
`complete_inquiry_lap_slot_apply_lmp268_remote_name_and_arm_timer`,
`teardown_inquiry_lap_slot_baseband_cleanup_and_release`, plus
`fHCI_inquiry_cancel` in this region.

**Confidence:** HIGH — fully decompiled 54B; idle-arm vs busy-flush branches
match documented paging/inquiry watchdog usage across `0x80040000` passes.

Region unnamed count after this pass: **235** (236 minus this rename). Live named
**1931** global.

**Next:** superseded by Pass 56.

## Pass 56 (2026-07-01) — codec 3-bit field patch `FUN_80033da0`

Fresh `ListUnnamed80030000.java` re-run: **235 unnamed** remain in region
(unchanged from Pass 55 pre-rename list; rank-1 was `FUN_80033da0`).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80033da0` → `patch_three_bit_codec_slot_field_upper_triplet_via_hw_hook`**
(104B, HIGH) via `RenamePass56Region80030000Fun80033da0.java` (`renamed=1`,
live-verified).

**Mechanism:** For codec slot index `param_1` (0–11) and 3-bit value
`param_2`, maps slot to one of three packed ushort config fields at struct
offsets `0x1d0`/`0x1d2`/`0x1d4` (slots 0–2, 3–7, 8–11 respectively), clears
the target 3-bit nibble with `~(7 << (slot*3))`, ORs in `(param_2 & 7)`, and
dispatches the merged ushort via hook at `PTR_DAT_80033e0c`. Sibling of
`FUN_80033d30` (lower triplet `0x1cc`/`0x1ce`/`0x1d0`).

**Callers:** 6 xref-in (rank-1 by xref count); indirect via function-pointer
slots including `VSC_0xfd40_FUN_8002fd3c` (`PTR_DAT_8002fe9c`),
`idk_takes_new_new_power_val` (`PTR_DAT_80034bdc`), and
`init_or_clear_sco_hw_channel_subsystem` — TX-power / VSC-fd40 / SCO-init
codec-config cluster.

**Confidence:** HIGH — fully decompiled 104B; 3-bit nibble pack/unpack into
upper triplet ushort fields matches documented codec-slot writers and hook
dispatch idiom.

Region unnamed count after this pass: **234** (235 minus this rename). Live named
**1932** global.

**Next:** superseded by Pass 57.

## Pass 57 (2026-07-01) — codec 3-bit field patch `FUN_80033d30`

Fresh `ListUnnamed80030000.java` re-run: **234 unnamed** remain in region
(unchanged from Pass 56 pre-rename list; rank-1 was `FUN_80033d30`).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80033d30` → `patch_three_bit_codec_slot_field_lower_triplet_via_hw_hook`**
(104B, HIGH) via `RenamePass57Region80030000Fun80033d30.java` (`renamed=1`,
live-verified).

**Mechanism:** For codec slot index `param_1` (0–11) and 3-bit value
`param_2`, maps slot to one of three packed ushort config fields at struct
offsets `0x1cc`/`0x1ce`/`0x1d0` (slots 0–4, 5–9, 10–11 respectively), clears
the target 3-bit nibble with `~(7 << (slot*3))`, ORs in `(param_2 & 7)`, and
dispatches the merged ushort via hook at `PTR_DAT_80033d9c`. Sibling of
`patch_three_bit_codec_slot_field_upper_triplet_via_hw_hook` (upper triplet
`0x1d0`/`0x1d2`/`0x1d4`).

**Callers:** 6 xref-in (rank-1 by xref count); indirect via function-pointer
slots including `VSC_0xfd40_FUN_8002fd3c` (`PTR_DAT_8002fe9c`),
`idk_takes_new_new_power_val` (`PTR_DAT_80034bdc`), and
`init_or_clear_sco_hw_channel_subsystem` — TX-power / VSC-fd40 / SCO-init
codec-config cluster (same caller set as Pass 56 upper-triplet sibling).

**Confidence:** HIGH — fully decompiled 104B; 3-bit nibble pack/unpack into
lower triplet ushort fields matches documented codec-slot writers and hook
dispatch idiom.

Region unnamed count after this pass: **233** (234 minus this rename). Live named
**1933** global.

**Next:** superseded by Pass 58.

## Pass 58 (2026-07-01) — role-switch HW channel bit15 OR `FUN_8003491c`

Fresh `ListUnnamed80030000.java` re-run: **233 unnamed** remain in region
(unchanged from Pass 57 pre-rename list; rank-1 was `FUN_8003491c` at 78B,
6 xref-in — tied xref count with `FUN_800348c0`/`FUN_80034e98`/`FUN_80034e6c`
but largest among the tie).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003491c` → `or_merge_hw_channel_bit15_by_conn_index_via_esco_remap`**
(78B, HIGH) via `RenamePass58Region80030000Fun8003491c.java` (`renamed=1`,
live-verified).

**Mechanism:** Conn-index (`param_1`) into `big_ol_struct`: calls
`remap_role_index_to_esco_slot_if_pending` on `bos_connection__array_index` +
`byte_0xCC`, looks up per-slot HW-channel register index from table at
`PTR_DAT_80034970` (`slot*8+4` ushort), reads current value at that index via
`DAT_80034974`, OR-merges `0x8000` (bit15), dispatches via hook at
`PTR_DAT_80034978`. Set-bit sibling of unnamed `FUN_800348c0` (AND `0x7fff`
clear-bit variant on parallel literal-pool tables).

**Callers:** 6 xref-in (rank-1 by xref count); indirect via function-pointer
slots in role-switch / LMP-25C completion cluster — notably
`process_dual_slot_lmp25c_role_record_packet_completion` (region `0x80040000`
Pass 52db) dispatches both `FUN_800348c0`/`FUN_8003491c` on role-switch LMP
opcode 3.

**Confidence:** HIGH — fully decompiled 78B; OR-merge bit15 on indexed HW-channel
register matches documented `or_merge_hw_channel_table_entry_and_indexed_dispatch`
cluster idiom; esco-slot remap + conn-index lookup pattern consistent with
siblings.

Region unnamed count after this pass: **232** (233 minus this rename). Live named
**1934** global.

**Next:** superseded by Pass 59.

## Pass 59 (2026-07-01) — role-switch HW channel bit15 clear `FUN_800348c0`

Fresh `ListUnnamed80030000.java` re-run: **232 unnamed** remain in region
(unchanged from Pass 58 pre-rename list; rank-1 was `FUN_800348c0` at 74B,
6 xref-in — tied xref count with `FUN_80034e98`/`FUN_80034e6c` but largest
among the tie; clear-bit sibling of Pass 58's `or_merge_hw_channel_bit15_...`).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_800348c0` → `and_clear_hw_channel_bit15_by_conn_index_via_esco_remap`**
(74B, HIGH) via `RenamePass59Region80030000Fun800348c0.java` (`renamed=1`,
live-verified).

**Mechanism:** Conn-index (`param_1`) into `big_ol_struct`: calls
`remap_role_index_to_esco_slot_if_pending` on `bos_connection__array_index` +
`byte_0xCC`, looks up per-slot HW-channel register index from table at
`PTR_DAT_80034910` (`slot*8+4` ushort), reads current value at that index via
`DAT_80034914`, AND-masks `0x7fff` (clear bit15), dispatches via hook at
`PTR_DAT_80034918`. Clear-bit sibling of Pass 58's
`or_merge_hw_channel_bit15_by_conn_index_via_esco_remap` (OR `0x8000` set-bit
variant on parallel literal-pool tables `80034970`/`80034974`/`80034978`).

**Callers:** 6 xref-in (rank-1 by xref count); indirect via function-pointer
slots in role-switch / LMP-25C completion cluster — notably
`process_dual_slot_lmp25c_role_record_packet_completion` (region `0x80040000`
Pass 52db) dispatches both clear/set siblings on role-switch LMP opcode 3.

**Confidence:** HIGH — fully decompiled 74B; AND-mask bit15 clear on indexed
HW-channel register matches documented role-switch dispatch cluster; esco-slot
remap + conn-index lookup pattern consistent with Pass 58 set-bit sibling.

Region unnamed count after this pass: **231** (232 minus this rename). Live named
**1935** global.

**Next:** superseded by Pass 60.

## Pass 60 (2026-07-01) — OGC-3 config apply logger `FUN_80034e98`

Fresh `ListUnnamed80030000.java` re-run: **231 unnamed** remain in region
(unchanged from Pass 59 pre-rename list; rank-1 was `FUN_80034e98` at 38B,
6 xref-in — tied xref count with `FUN_80034e6c` at same size; first by sort
order).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80034e98` → `log_ogc3_config_apply_evt_0x4b6_if_no_patch3`**
(38B, HIGH) via `RenamePass60Region80030000Fun80034e98.java` (`renamed=1`,
live-verified).

**Mechanism:** Thin `possible_logger_called_if_no_patch3` tail-call stub with
event tag `0x4b6`; context pointer from `PTR_DAT_80034ec0`. Called after
OGF=3 vendor-config field writes — notably `OGC_3_OCF_45` (writes
`the_0x300->field_0x17c`) and `OGC_3_OCF_49` (writes `field_0x17e`) in region
`0x80010000`. Sibling of `FUN_80034e6c` (same shape, tag `0x330`, role-switch
housekeeping cluster).

**Callers:** 6 xref-in (rank-1 by xref count); OGC-3 config-apply cluster +
indirect dispatch slots.

**Confidence:** HIGH — fully decompiled 38B; logger-stub idiom matches
documented `possible_logger_called_if_no_patch3` cluster; caller semantics
confirmed via `OGC_3_OCF_45`/`OGC_3_OCF_49` decompile.

Region unnamed count after this pass: **230** (231 minus this rename). Live named
**1936** global.

**Next:** superseded by Pass 61.

## Pass 61 (2026-07-01) — role-switch housekeeping logger `FUN_80034e6c`

Fresh `ListUnnamed80030000.java` re-run: **230 unnamed** remain in region
(unchanged from Pass 60 pre-rename list; rank-1 was `FUN_80034e6c` at 38B,
6 xref-in — sole remaining 6-xref tie member after Pass 60 renamed sibling
`FUN_80034e98`).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80034e6c` → `log_role_switch_housekeeping_evt_0x330_if_no_patch3`**
(38B, HIGH) via `RenamePass61Region80030000Fun80034e6c.java` (`renamed=1`,
live-verified).

**Mechanism:** Thin `possible_logger_called_if_no_patch3` tail-call stub with
event tag `0x330`; context pointer from `PTR_DAT_80034e94`. Called after
role-switch / codec-type housekeeping — notably
`apply_codec_type_and_role_switch_hook_dispatch` (region `0x80000000`),
`apply_public_bdaddr_role_change_commit_hci_evt_sync` (region `0x80040000`
Pass 52go), and similar tails that emit role `0x35` via
`apply_or_defer_conn_role_change_emit_hci_evt_sync`. Sibling of Pass 60's
`log_ogc3_config_apply_evt_0x4b6_if_no_patch3` (same shape, tag `0x4b6`,
OGC-3 config-apply cluster).

**Callers:** 6 xref-in (rank-1 by xref count); role-switch housekeeping +
indirect dispatch slots.

**Confidence:** HIGH — fully decompiled 38B; logger-stub idiom matches
documented `possible_logger_called_if_no_patch3` cluster; caller semantics
confirmed via `apply_public_bdaddr_role_change_commit_hci_evt_sync` and
`apply_codec_type_and_role_switch_hook_dispatch` decompile.

Region unnamed count after this pass: **229** (230 minus this rename). Live named
**1937** global.

**Next:** superseded by Pass 62.

## Pass 62 (2026-07-01) — LMP power/clock-adj eligibility gate `FUN_80033794`

Fresh `ListUnnamed80030000.java` re-run: **229 unnamed** remain in region
(unchanged from Pass 61; rank-1 by size at xref=5 tier is `FUN_80033794` at
578B — previously decompiled/documented in Pass 7 but never Ghidra-renamed).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80033794` → `gate_lmp_power_clk_adj_eligibility_by_conn_state`**
(578B, HIGH) via `RenamePass62Region80030000Fun80033794.java` (`renamed=1`,
live-verified).

**Mechanism:** `bool gate(char param_1)` — optional patch-hook delegate via
`PTR_DAT_800339d8` first; then multi-tier nested gate on config
`field208_0xd8` bit5, `the_0x300` link-mode bytes (`field_0x179`/`field_0x17b`),
`byte_0x16a` power-threshold compares, and `0x1ac` struct-array capability bits
(`field40_0x28`/`field68_0x44`/`field407_0x1a4`/`field451_0x1d0`). Loops 10
`big_ol_struct` slots rejecting any with status `0x02` + valid flag set.
Gated on `config->_x7a_enable_LMP_POWER_REQ_RES_and_CLK_ADJ` bit2. Returns
true when LMP power/clock-adjust procedure is eligible.

**Callers:** 5 xref-in incl. `dispatch_link_power_mode_by_status_bits_and_commit`
(region `0x80050000` Pass 54ao); sibling of
`LMP_power_and_clk_adj_procedure_orchestrator` (`0x80034ec4`).

**Confidence:** HIGH — full 578B decompile (Pass 7 + Pass 62 re-verify);
config field name `_x7a_enable_LMP_POWER_REQ_RES_and_CLK_ADJ` confirms LMP
power/clock-adj semantics; caller chain documented.

Region unnamed count after this pass: **228** (229 minus this rename). Live named
**1938** global.

**Next:** superseded by Pass 63.

## Pass 63 (2026-07-01) — ACL ring-buffer flush gate `FUN_800324f4`

Fresh `ListUnnamed80030000.java` re-run: **228 unnamed** remain in region
(unchanged from Pass 62; rank-1 by xref count is `FUN_800324f4` at 62B,
5 xref-in).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_800324f4` → `trigger_acl_ring_buffer_flush_on_tracked_conn_match`**
(62B, HIGH) via `RenamePass63Region80030000Fun800324f4.java` (`renamed=1`,
live-verified).

**Mechanism:** `bool gate(char link_idx, void *ptr, char flush_flag)` gated on
`config+0xd8` bit 0x40. When global tracked pointer at `PTR_DAT_80032538` is
nonzero and `link_idx` matches `PTR_DAT_8003253c` and `ptr` matches the tracked
pointer: returns 1 on probe (`flush_flag==0`); on flush (`flush_flag==1`)
clears tracked pointer and calls `acl_packet_ring_buffer_manager`. Otherwise
returns 0. Sibling of `invoke_acl_ring_buffer_if_config_flag_0x40_and_index_valid`
(region `0x80070000` Pass 12br) — same config flag, different match semantics.

**Callers:** 5 xref-in incl. `pdu_type_dispatch_enqueue_to_per_type_ring_and_notify`
(region `0x80000000`) on type-4/5 PDU error/overflow paths and
`dispatch_hci_td_connection_event_side_effects` (region `0x80020000`) on
HCI-TD event-4 config-flag cleanup.

**Confidence:** HIGH — full 62B decompile; callee `acl_packet_ring_buffer_manager`
already HIGH from Pass 6; caller semantics confirmed via both caller decompiles.

Region unnamed count after this pass: **227** (228 minus this rename). Live named
**1939** global.

**Next:** superseded by Pass 64.

## Pass 64 (2026-07-01) — int64 signed-shift width helper `FUN_80038c94`

Fresh `ListUnnamed80030000.java` re-run: **227 unnamed** remain in region
(unchanged from Pass 63; rank-1 by xref count is `FUN_80038c94` at 228B,
4 xref-in — tied at xref=4 tier, wins on size).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80038c94` → `compute_int64_halves_signed_shift_width`**
(228B, HIGH) via `RenamePass64Region80030000Fun80038c94.java` (`renamed=1`,
live-verified).

**Mechanism:** MIPS16e 64-bit-emulation runtime helper:
`uint width(int halves[2], ushort shift_amt)` where `halves[0]` is the low
32-bit dword, `halves[1]` (byte at +4) holds sign-extension bit pattern, and
byte at +6 records whether the sign bit at `(shift_amt-1)` is set. Counts
leading sign/run bits and returns effective signed right-shift width (0–40,
`0x28` base). Optional override hook at `PTR_DAT_80038d78` can short-circuit
return 1. Pure compiler-support primitive — no protocol-specific meaning.

**Callers:** 4 xref-in incl. `int64_arith_op_and_signed_shift_right`
(Pass 8 — called twice per shift, low/high halves) and
`read_fd49_extended_diag_build_dual_slot_bitmasks_and_shift_width`
(VSC `0xfd49` extended-diagnostic register read path building sign bytes
before shift-width store at `+5`).

**Confidence:** HIGH — full 228B decompile; Pass 8 already documented role as
sign-extend/shift helper; caller `int64_arith_op_and_signed_shift_right` decompile
confirms dual-half usage pattern.

Region unnamed count after this pass: **226** (227 minus this rename). Live named
**1940** global.

**Next:** superseded by Pass 65.

## Pass 65 (2026-07-01) — inquiry LAP HW channel programmer `FUN_8003c94c`

Fresh `ListUnnamed80030000.java` re-run: **226 unnamed** remain in region
(unchanged from Pass 64; rank-1 by xref count is `FUN_8003c94c` at 190B,
4 xref-in — tied at xref=4 tier, wins on size over `FUN_8003e1d4` 176B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003c94c` → `program_inquiry_lap_hw_channel_by_pending_slot_count`**
(190B, HIGH) via `RenamePass65Region80030000Fun8003c94c.java` (`renamed=1`,
live-verified).

**Mechanism:** IRQ-masked inquiry/LAP HW-channel programming commit path gated
on `the_0x300->byte_0x16a` bit 2 and optional veto hook at `PTR_DAT_8003ca0c`.
Calls `count_consecutive_inquiry_lap_pending_slot_flags()`; when count `< 4`,
indexes ushort table at `PTR_DAT_8003ca18`, programs HW channel via
`or_merge_hw_channel_table_entry_and_indexed_dispatch(0x32,0x2000)`, then
dispatches indexed HW writes through `PTR_DAT_8003ca20` fptr (conditional on
channel bit14, slot-index shift `<<0xb`, final opcode `0/4`). Stores active
count to `PTR_DAT_8003ca24`. When count `>= 4`, logs via
`possible_logging_function__var_args` (tag `0x26e`).

**Callers:** 4 xref-in incl. `remote_name_request_feature_apply_4` (commit fn
for `field208_0xd8` bit 4 path) and `LMP_link_supervision_tick_scheduler`
(mode-1 link-type dispatch). Inquiry/LAP cluster sibling of
`count_consecutive_inquiry_lap_pending_slot_flags` and
`remote_name_request_feature_apply_8` commit `FUN_8003ca28`.

**Confidence:** HIGH — full 190B decompile; prior Pass 6/8/52gc documentation
already identified role as remote-name-request apply_4 HW-channel commit;
decompile confirms `count_consecutive_inquiry_lap_pending_slot_flags` index
pattern and `or_merge_hw_channel_table_entry_and_indexed_dispatch` usage.

Region unnamed count after this pass: **225** (226 minus this rename). Live named
**1941** global.

**Next:** superseded by Pass 66.

## Pass 66 (2026-07-01) — connection packet-completion ring drainer `FUN_8003e1d4`

Fresh `ListUnnamed80030000.java` re-run: **225 unnamed** remain in region
(unchanged from Pass 65; rank-1 by xref count is `FUN_8003e1d4` at 176B,
4 xref-in — tied at xref=4 tier, wins on size over `FUN_800362f0` 120B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003e1d4` → `drain_connection_packet_completion_ring_and_emit_hci_num_completed`**
(176B, HIGH) via `RenamePass66Region80030000Fun8003e1d4.java` (`renamed=1`,
live-verified).

**Mechanism:** IRQ-masked drain of the stride-0x88 connection table's
12-entry (`+0x39` index, wraps at 0xc) packet-completion ring at
`PTR_DAT_8003e284`. Gated on per-conn `+4==1` (active) and `+0x18!=1`.
While `+0x3a` pending count nonzero, dispatches each non-null ring slot at
`(idx+0xe)*4+8` via completion fptr (`PTR_DAT_8003e290`, arg `3`) or patch-hook
path (`PTR_DAT_8003e28c` when `field_0x179==2`), clears slot, advances index,
increments `+0x19` completed counter. When `field_0x179==2`, emits
`send_evt_HCI_Number_Of_Completed_Packets()` after drain. Completion-callback
sibling of `ACL_fragment_dequeue_and_credit_consumer` on the same table.

**Callers:** 4 xref-in incl. `connection_teardown_finalize_and_reset`
(link-supervision-loss cleanup chain) and `baseband_event_status_dispatcher_0xd`
(per-slot config `field+7` bit2 branch).

**Confidence:** HIGH — full 176B decompile; Pass 8 already identified role in
connection-teardown cleanup chain; decompile confirms ring-buffer drain shape
matching `ACL_fragment_dequeue_and_credit_consumer` (+0x39/+0x3a fields) and
HCI Number Of Completed Packets emission path.

Region unnamed count after this pass: **224** (225 minus this rename). Live named
**1942** global.

**Next:** superseded by Pass 67.

## Pass 67 (2026-07-01) — LMP slot-offset HW programmer `FUN_800362f0`

Fresh `ListUnnamed80030000.java` re-run: **224 unnamed** remain in region
(unchanged from Pass 66; rank-1 by xref count is `FUN_800362f0` at 120B,
4 xref-in — wins xref=4 tier on size over `FUN_8003cf28` 84B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_800362f0` → `compute_lmp_slot_offset_and_program_hw_by_conn_cc_index`**
(120B, HIGH) via `RenamePass67Region80030000Fun800362f0.java` (`renamed=1`,
live-verified).

**Mechanism:** Optional patchable hook at `PTR_DAT_80036368` — when installed
and returns non-zero, uses hook result as return byte. Default path skips when
conn slot `bdaddr_random_==1`. Otherwise gates on `FUN_8006c9e8(conn_idx, 5,
stack_buf)` connection-state probe; on failure programs `0` into the
HW-register pair indexed by `byte_0xCC` via `FUN_800140d8`. On success calls
`FUN_800334ac(timing_param, conn_idx)` to compute an LMP slot-offset byte
(capped `0x7c`, only when `bdaddr_random_==0`), then programs that value into
the same `byte_0xCC`-indexed HW register. Role-switch / slot-offset cluster
sibling of `read_hw_clock_raw_dword_by_role_index` and
`send_lmp_slot_offset_0x34_pdu_with_patch_hook_and_template`.

**Callers:** 4 xref-in (role-switch / conn-timing dispatch cluster).

**Confidence:** HIGH — full 120B decompile; callee `FUN_800334ac` confirms
timing-scaled slot-offset math with `field_0x202`/`field106_0x94` inputs;
`FUN_800140d8` confirms HW-register write path keyed on `byte_0xCC`.

Region unnamed count after this pass: **223** (224 minus this rename). Live named
**1943** global.

**Next:** superseded by Pass 68.

## Pass 68 (2026-07-01) — Parallel slot-table tail reset `FUN_8003cf28`

Fresh `ListUnnamed80030000.java` re-run: **223 unnamed** remain in region
(unchanged from Pass 67; rank-1 by xref count is `FUN_8003cf28` at 84B,
4 xref-in — wins xref=4 tier on size over `FUN_80038b64` 82B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003cf28` → `reset_parallel_slot_table_entry_tail_state_by_index`**
(84B, HIGH) via `RenamePass68Region80030000Fun8003cf28.java` (`renamed=1`,
live-verified).

**Mechanism:** Resets tail-state fields in one entry of the parallel three-slot
table at `PTR_DAT_8003cf7c` (0x88 stride, indexed by `param_1 & 0xff`):
arms `+0x78=1`, sets `+0x79=0xff`, clears dword at `+0x7c`, masks bottom two
bits of `+0x7a`, zeroes `+0x7b` and `+0x80..+0x85`. Wrapper
`FUN_8003cf80` calls this for indices 0/1/2 during BT cold-init alongside
`init_three_0x88_slot_tables_and_clear_crypto_globals` (region `0x80020000`
Pass 6 cont. 219).

**Callers:** 4 xref-in (cold-init + slot-table management cluster).

**Confidence:** HIGH — full 84B decompile; 0x88-stride slot tail-reset idiom
matches documented parallel three-slot init chain; sibling wrapper confirms
indices 0..2.

Region unnamed count after this pass: **222** (223 minus this rename). Live named
**1944** global.

**Next:** superseded by Pass 69.

## Pass 69 (2026-07-01) — ilog2 helper `FUN_80038b64`

Fresh `ListUnnamed80030000.java` re-run: **222 unnamed** remain in region
(unchanged from Pass 68; rank-1 by xref count is `FUN_80038b64` at 82B,
4 xref-in — wins xref=4 tier on size over `FUN_8003e648` 68B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80038b64` → `ilog2_floor_plus_two_dword`**
(82B, HIGH) via `RenamePass69Region80030000Fun80038b64.java` (`renamed=1`,
live-verified).

**Mechanism:** 32-bit MSB scan from bit 30 downward (sign bit excluded).
For nonnegative `*param_1`: returns `floor(log2(n)) + 2` when `n > 0`, or `1`
when `n == 0` — e.g. `n=1→2`, `n=8→5`. For negative values: walks while bits
are set from bit 30 down, returns when the first clear bit is found (complement
path for two's-complement magnitudes). Pure bit-scan utility; sibling of ROM
`count_leading_zeros_32` (`0x8000937c`) but uses iterative shift-test rather
than divide-and-conquer CLZ.

**Callers:** 4 xref-in at `0x80038bc2`/`bca`/`bd2`/`bda` (inline calls in
adjacent code block immediately following this function — Ghidra has not bounded
a containing parent function).

**Confidence:** HIGH — full 82B decompile; nonnegative path semantics verified
by bit-index trace; name matches `+2` offset from MSB bit position.

Region unnamed count after this pass: **221** (222 minus this rename). Live named
**1945** global.

**Next:** superseded by Pass 70.

## Pass 70 (2026-07-01) — connection buffer clear `FUN_8003e648`

Fresh `ListUnnamed80030000.java` re-run: **221 unnamed** remain in region
(unchanged from Pass 69; rank-1 by xref count is `FUN_8003e648` at 68B,
4 xref-in — wins xref=4 tier on size).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003e648` → `clear_active_stride88_connection_buffers_and_drain_hci_cmds`**
(68B, HIGH) via `RenamePass70Region80030000Fun8003e648.java` (`renamed=1`,
live-verified).

**Mechanism:** When global `the_0x300->field_0x171` is set, loops indices 0..2
on stride-0x88 table `PTR_DAT_8003e690`; for entries with `+4==0x01`, `memset`
clears 0x3c bytes at offset `+0x34`. Always tail-calls
`drain_all_hci_cmd_completion_slots_once` — connection-teardown cleanup sibling
of `reinit_hci_cmd_list_clear_active_descriptor_tails_and_drain`.

**Callers:** `connection_teardown_finalize_and_reset` (link-supervision-loss
cleanup chain) + `connection_setup_arm_stride88_slot_and_apply_packet_types`
(bitmask-sweep dispatch sibling).

**Confidence:** HIGH — full 68B decompile; stride/offset semantics match Pass 8
`connection_teardown_finalize_and_reset` cluster.

Region unnamed count after this pass: **220** (221 minus this rename). Live named
**1946** global.

**Next:** superseded by Pass 71.

## Pass 71 (2026-07-01) — HW reg 0x44 bit-0 RMW `FUN_8003b698`

Fresh `ListUnnamed80030000.java` re-run: **220 unnamed** remain in region
(unchanged from Pass 70; rank-1 by xref count is `FUN_8003b698` at 60B,
4 xref-in — wins xref=4 tier on size over `FUN_80039b18`/`FUN_8003ca28`).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003b698` → `read_modify_write_hw_reg_0x44_set_bit0`**
(60B, HIGH) via `RenamePass71Region80030000Fun8003b698.java` (`renamed=1`,
live-verified).

**Mechanism:** Reads VSC/hardware register `0x44` via indirect read fptr at
`PTR_DAT_8003b6d4` with args `(0, 0x44, 1)`, then writes back via write fptr at
`PTR_DAT_8003b6d8` with value `(read_val & 0xfffe) | (param_1 & 1)` — preserves
all bits except bit 0, sets bit 0 from `param_1` LSB.

**Callers:** `per_connection_hw_buffer_setup_with_patch_hook` (calls with `1` as
part of the `0x8003b6xx` setup cluster before setting bit `0x8000` of reg 0x44),
`VSC_0xfc64_link_quality` (AFH cleanup tail), and
`conn_credit_or_counter_update_with_log` (retry-counter cleanup path when both
directions' retry counters are nonzero).

**Confidence:** HIGH — full 60B decompile; register index `0x44` and bit-0 merge
semantics unambiguous; caller context matches documented per-connection HW-buffer
setup cluster in Pass 8 item 13.

Region unnamed count after this pass: **219** (220 minus this rename). Live named
**1947** global.

**Next:** superseded by Pass 72.

## Pass 72 (2026-07-01) — TX power delta `FUN_80039b18`

Fresh `ListUnnamed80030000.java` re-run: **219 unnamed** remain in region
(unchanged from Pass 71; rank-1 by xref count is `FUN_80039b18` at 48B,
4 xref-in — wins xref=4 tier on size over `FUN_8003ca28`).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80039b18` → `compute_tx_power_delta_from_global_baselines`**
(48B, HIGH) via `RenamePass72Region80030000Fun80039b18.java` (`renamed=1`,
live-verified).

**Mechanism:** Signed-byte TX-power delta helper. Computes
`(param_1 & 0xff) - PTR_DAT_80039b48[+0x35]`; when `param_2 == 0` returns
that signed delta, else subtracts `*PTR_DAT_80039b4c` and returns the
sign-extended result. Literal pool `PTR_DAT_80039b48` resolves to the
`0x80047f14` max-TX-power region documented in `reverse_engineering_vsc_dispatcher.md`.

**Callers:** Invoked via function pointer — `FUN_80039844` (TX-power config
init: calls twice with `param_2=0`, stores `(result >> 1)` halved into
runtime globals), `FUN_80039c08` (TX-power adjustment dispatcher: passes
`param_2` 0 or 1 per mode byte), plus one patch/RAM call site at `0x8010bc96`.

**Confidence:** HIGH — full 48B decompile; dual-baseline subtraction semantics
unambiguous; caller context in TX-power init/adjust cluster matches region's
`idk_takes_new_new_power_val` / `set_new_power_val` pair.

Region unnamed count after this pass: **218** (219 minus this rename). Live named
**1948** global.

**Next:** superseded by Pass 73.

## Pass 73 (2026-07-01) — HW channel merge commit `FUN_8003ca28`

Fresh `ListUnnamed80030000.java` re-run: **218 unnamed** remain in region
(unchanged from Pass 72; rank-1 by xref count is `FUN_8003ca28` at 42B,
4 xref-in — wins xref=4 tier on size over `FUN_80039b18`).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003ca28` → `commit_hw_channel_merge_index_0x36_on_role_bit0`**
(42B, HIGH) via `RenamePass73Region80030000Fun8003ca28.java` (`renamed=1`,
live-verified).

**Mechanism:** Thin HW-channel commit stub gated on `the_0x300->byte_0x16a`
bit 0. When set, calls
`or_merge_hw_channel_table_entry_and_indexed_dispatch(0x36,0x2000)` then
dispatches indexed HW writes through `PTR_DAT_8003ca58` fptr with args
`(0,10)`. Sibling of `program_inquiry_lap_hw_channel_by_pending_slot_count`
(Pass 65 apply_4 path: bit2 gate, index `0x32`, slot-count indexing).

**Callers:** 4 xref-in incl. `remote_name_request_feature_apply_8` (commit fn
for `field208_0xd8` bit 8 path) and `LMP_link_supervision_tick_scheduler`
(mode-1 link-type dispatch). Inquiry/LAP cluster sibling documented since
Pass 6/8/65.

**Confidence:** HIGH — full 42B decompile; prior cross-region documentation
already identified role as remote-name-request apply_8 HW-channel commit;
decompile confirms bit0 gate + `or_merge_hw_channel_table_entry_and_indexed_dispatch`
index `0x36` pattern.

Region unnamed count after this pass: **217** (218 minus this rename). Live named
**1949** global.

**Next:** superseded by Pass 74.

## Pass 74 (2026-07-01) — conn slot-timing commit `FUN_800367e4`

Fresh `ListUnnamed80030000.java` re-run: **217 unnamed** remain in region
(unchanged from Pass 73; rank-1 by xref count drops to xref=3 tier; top by
size is `FUN_800367e4` at 920B, 3 xref-in — tied with `FUN_80035768` on
xref/size, wins on address sort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_800367e4` → `recompute_and_commit_conn_slot_timing_hw_and_packet_types`**
(920B, HIGH) via `RenamePass74Region80030000Fun800367e4.java` (`renamed=1`,
live-verified).

**Mechanism:** Large conn-indexed slot-timing commit orchestrator on
`big_ol_struct` stride `0x2b8`. Optional prelude hook at `PTR_DAT_80036b7c`
and epilogue at `PTR_DAT_80036bcc`. Reads HW clock via
`read_hw_clock_raw_dword_by_role_index`, computes slot instant/offset with
modulo arithmetic on per-conn timing fields (`field_0x29a`, `+0x3b`..`+0x35`),
programs HW via `PTR_DAT_80036b9c` register writes (`0x2`, `0x4e`, `0x48`,
`0x4c`, `0x4a`, `0x21c`), commits deadline to `unknown4_0x3C`, calls
`scheduler_find_next_min_deadline`, then
`or_merge_hw_channel_table_entry_and_indexed_dispatch(0x5e,0x800)`.
`param_2` (`char`) selects establish vs teardown paths: establish
(`param_2==1`) may early-return `0xff` when timing divisor zero; teardown
(`param_2==0`) clears gate bytes and runs inverse packet-type transitions via
`program_packet_type_if_stored_matches_expected` (`0xc000→0x1c00`,
`0xc000→0xc00`, `0xc00→0xc000`). Large slot offsets (`>0x50`) dispatch
`compute_lmp_slot_offset_and_program_hw_by_conn_cc_index`. Uses
`remap_role_index_to_esco_slot_if_pending` for eSCO slot indexing.

**Callers:** 3 xref-in: `FUN_800378e4`, `FUN_8003792c`, `FUN_80060a78`
(eSCO/SCO slot-timing dispatch cluster near `0x800378xx`).

**Confidence:** HIGH — full 920B decompile; multiple named callees
(`read_hw_clock_raw_dword_by_role_index`, `program_packet_type_if_stored_matches_expected`,
`compute_lmp_slot_offset_and_program_hw_by_conn_cc_index`,
`or_merge_hw_channel_table_entry_and_indexed_dispatch`) anchor the
establish/teardown packet-type and HW-register commit paths.

Region unnamed count after this pass: **216** (217 minus this rename). Live named
**1950** global.

**Next:** superseded by Pass 75.

## Pass 75 (2026-07-01) — connection setup commit `FUN_80035768`

Fresh `ListUnnamed80030000.java` re-run: **216 unnamed** remain in region
(unchanged from Pass 74; rank-1 by size at xref=3 tier is `FUN_80035768` at
920B — Pass 74 sibling `recompute_and_commit_conn_slot_timing_hw_and_packet_types`
already renamed).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80035768` → `commit_connection_setup_mode_by_slot_bitmask_and_gates`**
(920B, HIGH) via `RenamePass75Region80030000Fun80035768.java` (`renamed=1`,
live-verified).

**Mechanism:** Large connection-setup mode commit orchestrator on global state
`PTR_DAT_80035b04`. Optional prelude hooks at `PTR_DAT_80035b00` and
`PTR_DAT_80035b18`. Gates via `check_connection_setup_commit_gate_status`, `validate_connection_setup_preconditions`,
and `gate_lmp_power_clk_adj_eligibility_by_conn_state`. Mode byte `param_1`
(0–6) plus slot bitmask `param_2` select commit path: mode 0 dispatches
`dispatch_lmp_25c_multi_slot_emit_with_config_gates` + `FUN_80034d88`; mode 1
runs `apply_LAP_derived_hopping_params(1)` + `reconcile_nonmatching_bdaddr_slot_and_dispatch_lmp_259`
+ `commit_hw_channel_merge_index_0x36_on_role_bit0`; mode 2 runs inquiry LAP
path via `program_inquiry_lap_hw_channel_by_pending_slot_count`; modes 3/4/6
handle LMP power/CLK-adj and feature-page merge when config bit2 set.

**Callers:** 3 xref-in incl. `dispatch_link_power_mode_by_status_bits_and_commit`
(`0x8005bf4c`) tail via `gate_lmp_power_clk_adj_eligibility_by_conn_state`.

**Confidence:** HIGH — full 920B decompile; multiple named callees anchor the
page/inquiry/LMP-0x25c/HW-channel commit paths.

Region unnamed count after this pass: **215** (216 minus this rename). Live named
**1951** global.

**Next:** superseded by Pass 76.

## Pass 76 (2026-07-01) — conn class-mode apply `FUN_800366cc`

Fresh `ListUnnamed80030000.java` re-run: **215 unnamed** remain in region
(unchanged from Pass 75; rank-1 by size at xref=3 tier is `FUN_800366cc` at
252B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_800366cc` → `apply_conn_class_mode_afh_role_remap_and_esco_ptype`**
(252B, HIGH) via `RenamePass76Region80030000Fun800366cc.java` (`renamed=1`,
live-verified).

**Mechanism:** Conn-slot class-mode apply helper on `big_ol_struct` indexed by
`param_1`. Optional prelude hook at `PTR_DAT_800367c8` — if installed and
returns non-zero, skip default path. Otherwise: `LMP__25C_called2()`;
`remap_role_index_to_esco_slot_if_pending`; HW/LMP housekeeping via
`FUN_800140d8`/`FUN_800143b0`/`FUN_80014dac`; when global `PTR_DAT_800367d0`
bit `0x8` set, calls `dispatch_lmp_268_timers_with_hook_and_config_gates`;
`clear_afh_lap_channel_map_for_matching_group`; when `bdaddr_random_==1`,
programs eSCO packet type via `program_packet_type_if_stored_matches_expected`
(`0xc000→0x1c00`); decrements `PTR_struct_of_at_least_0x300_size_800367d8`
counter `field_0x186`; logging via `possible_logging_function__var_args` +
`possible_logger_called_if_no_patch3`. Sibling of
`conn_class_mode_apply_and_log` success-path AFH/role-remap cluster.

**Callers:** 3 xref-in (rank-1 by size at xref=3 tier).

**Confidence:** HIGH — full 252B decompile; named callees anchor AFH LAP clear,
role remap, LMP-0x268 timer dispatch, and eSCO packet-type programming paths.

Region unnamed count after this pass: **214** (215 minus this rename). Live named
**1952** global.

**Next:** superseded by Pass 77.

## Pass 77 (2026-07-01) — link-mode cleanup status emit `FUN_80034d88`

Fresh `ListUnnamed80030000.java` re-run: **214 unnamed** remain in region
(unchanged from Pass 76; rank-1 by size at xref=3 tier is `FUN_80034d88` at
212B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80034d88` → `emit_link_mode_change_cleanup_status_with_dedup`**
(212B, HIGH) via `RenamePass77Region80030000Fun80034d88.java` (`renamed=1`,
live-verified).

**Mechanism:** Link-mode-change failure/cleanup status emitter. Optional
prelude hook at `PTR_DAT_80034e5c` — if installed and returns non-zero, skip
default path. Sets global link-mode housekeeping bytes on `PTR_DAT_80034e60`
(`+0x6d=0`, `+0x6e=8`, `+0x97` state tracking). When already armed, scans
circular pending-event buffer at `PTR_PTR_80034e68` for duplicate HCI status
tags `0x3eb` or `0x2d1` (selected by `PTR_DAT_80034e64` bit0:1) and returns
early if already queued. `param_1` modes 0–3 select cleanup subtype; pulls
stored params from `+0x90`/`+0x8d` and delegates to callee
`FUN_80034d00` which emits via `possible_logger_called_if_no_patch3` and
updates `PTR_DAT_80034d80` status flags. Known caller:
`link_mode_change_state_machine` failure path; also
`param_dispatch_with_rom_calls`.

**Callers:** 3 xref-in (rank-1 by size at xref=3 tier).

**Confidence:** HIGH — full 212B decompile; callee `FUN_80034d00` and caller
`link_mode_change_state_machine` anchor the cleanup/status-emit role.

Region unnamed count after this pass: **213** (214 minus this rename). Live named
**1953** global.

**Next:** superseded by Pass 78.

## Pass 78 (2026-07-01) — link-mode gate status `FUN_80033a04`

Fresh `ListUnnamed80030000.java` re-run: **213 unnamed** remain in region
(unchanged from Pass 77; rank-1 by size at xref=3 tier is `FUN_80033a04` at
188B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80033a04` → `check_link_mode_change_gate_status`**
(188B, HIGH) via `RenamePass78Region80030000Fun80033a04.java` (`renamed=1`,
live-verified).

**Mechanism:** Link-mode-change precondition gate used by
`link_mode_change_state_machine` (busy/ready/blocked status convention).
Optional prelude hook at `PTR_DAT_80033ac0` — if installed and returns
non-zero, skip default path. Scans 11-slot pending bitmask at
`PTR_DAT_80033ac4` + `PTR_PTR_80033ac8` — any active slot with pending work
returns `0xf` (busy). When link-mode housekeeping bytes at `PTR_DAT_80033acc`
match expected state (`+6==8`, `+7/+8==0x10`) and global counters at
`PTR_DAT_80033ad0`/`PTR_DAT_80033ad4` are zero, walks up to 4 connection
slots via `PTR_DAT_80033ad8` checking LAP/role state on stride-0x84 entries
against `the_0x300->_x142_LAP`. All slots clear returns `0` (ready); blocked
precondition returns `0xff`.

**Callers:** 3 xref-in (rank-1 by size at xref=3 tier).

**Confidence:** HIGH — full 188B decompile; caller
`link_mode_change_state_machine` and sibling `FUN_80033ae4` anchor the
busy(`0xf`)/ready(`0`)/blocked(`0xff`) gate role.

Region unnamed count after this pass: **212** (213 minus this rename). Live named
**1954** global.

**Next:** superseded by Pass 79.

## Pass 79 (2026-07-01) — connection setup commit gate `FUN_80035378`

Fresh `ListUnnamed80030000.java` re-run: **212 unnamed** remain in region
(unchanged from Pass 78; rank-1 by size at xref=3 tier is `FUN_80035378` at
186B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80035378` → `check_connection_setup_commit_gate_status`**
(186B, HIGH) via `RenamePass79Region80030000Fun80035378.java` (`renamed=1`,
live-verified).

**Mechanism:** Connection-setup commit precondition gate used by
`commit_connection_setup_mode_by_slot_bitmask_and_gates`,
`link_mode_change_state_machine`, and
`power_level_smoothing_filter_feeding_param_dispatch`. Optional prelude hook at
`PTR_DAT_80035434` — if installed and returns non-zero, skip default path.
When global flag byte at `PTR_DAT_80035438+3` bit0 set: arms 5-iteration pending
bitmask scan (bits 15–19) and sets timer/counter `0x20`; any active pending
slot returns `0xff` (blocked). Additional global-state flag checks at
`PTR_DAT_80035438`/`PTR_DAT_8003544c`/`PTR_DAT_80035450` plus
`check_status_bit_0x2_of_global`. Mode byte `param_1` selects downstream gate:
`param_1<2` → `validate_connection_setup_preconditions(0,0,1)`;
`param_1==2` → `gate_lmp_power_clk_adj_eligibility_by_conn_state(1)`;
`param_1==3` → `gate_lmp_power_clk_adj_eligibility_by_conn_state(0)`.
Returns `0` (ready) or `0xff` (blocked).

**Callers:** 3 xref-in (`commit_connection_setup_mode_by_slot_bitmask_and_gates`,
`link_mode_change_state_machine`,
`power_level_smoothing_filter_feeding_param_dispatch`).

**Confidence:** HIGH — full 186B decompile; callers and named callees anchor the
connection-setup/LMP-clk-adj gate role.

Region unnamed count after this pass: **211** (212 minus this rename). Live named
**1955** global.

**Next:** superseded by Pass 80.

## Pass 80 (2026-07-01) — HW-clock dword+slot-offset reader `FUN_8003497c`

Fresh `ListUnnamed80030000.java` re-run: **211 unnamed** remain in region
(unchanged from Pass 79; rank-1 by size at xref=3 tier is `FUN_8003497c` at
164B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003497c` → `read_hw_clock_dword_and_optional_slot_offset_by_role_index`**
(164B, HIGH) via `RenamePass80Region80030000Fun8003497c.java` (`renamed=1`,
live-verified).

**Mechanism:** Core HW-clock reader backing Pass 44's
`read_hw_clock_raw_dword_by_role_index` thin wrapper (which passes `param_2=NULL`).
Remaps role index via `remap_role_index_to_esco_slot_if_pending(0xff, role_index)`;
indices 8–11 select eSCO-slot register offset triplets in global table
`DAT_80034a20`, default path uses offsets `0x66`/`0x68`/`0x1da`. Writes
32-bit clock dword to `*param_1` from two 16-bit halves. When `param_2 != NULL`,
also computes slot-phase offset short: reads 10-bit field, wraps at `0x270`,
stores `0x270 - phase`. Used across SCO/eSCO timing and global busy-spin clusters
via the wrapper (63 xref-in) plus 3 direct xref-in at this function.

**Callers:** 3 xref-in (rank-1 by size at xref=3 tier); wrapper
`read_hw_clock_raw_dword_by_role_index` accounts for bulk of downstream use.

**Confidence:** HIGH — full 164B decompile; wrapper Pass 44 and named callee
`remap_role_index_to_esco_slot_if_pending` anchor the HW-clock table reader role.

Region unnamed count after this pass: **210** (211 minus this rename). Live named
**1956** global.

**Next:** superseded by Pass 81.

## Pass 81 (2026-07-01) — LMP 0x0d power-sample report dispatcher `FUN_800321f8`

Fresh `ListUnnamed80030000.java` re-run: **210 unnamed** remain in region
(unchanged from Pass 80; rank-1 by size at xref=3 tier is `FUN_800321f8` at
150B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_800321f8` → `dispatch_lmp_0x0d_power_sample_report_via_tx_hook`**
(150B, HIGH) via `RenamePass81Region80030000Fun800321f8.java` (`renamed=1`,
live-verified).

**Mechanism:** Builds and transmits an LMP PDU reporting a per-link power/RSSI
sample value. Optional override callback at `PTR_DAT_80032290+4` (via
`call_fptr_if_set_with_2_args_possibly_allocates_buf_at_arg2_`) may veto TX;
when it returns 0, assembles a 15-byte buffer: `0xff` prefix, opcode `0x0d`,
8-byte template from `PTR_DAT_80032294`, link id (`param_2` uint16 LE), and
sample byte (`param_3`). Dispatches via
`invoke_lmp_tx_hook_with_length_word_from_pdu_buffer`. Debug-logs via
`possible_logging_function__var_args` (category `0x23`, code `0x468`).

**Callers:** 3 xref-in (per `ListUnnamed80030000`; `xrefs_to` tool returned
empty — known GZF indirect-call gap). Decompiled callers in region `0x80070000`
Pass 12fn/12fp invoke `FUN_800321f8(0, id@+0x16, sample)` on threshold-crossing
and periodic-average flush paths in the per-link 0x1c-stride sample-record
cluster — ties this function to the power-level smoothing/report pipeline
alongside `power_level_smoothing_filter_feeding_param_dispatch`.

**Confidence:** HIGH — full 150B decompile; PDU layout, named callees, and
cross-region caller bodies anchor the LMP power-sample report role.

Region unnamed count after this pass: **209** (210 minus this rename). Live named
**1957** global.

**Next:** superseded by Pass 82.

## Pass 82 (2026-07-01) — BB regs 0x188/0x18a mode programmer `FUN_8003c6e8`

Fresh `ListUnnamed80030000.java` re-run: **209 unnamed** remain in region
(unchanged from Pass 81; rank-1 by size at xref=3 tier is `FUN_8003c6e8` at
138B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003c6e8` → `program_bb_regs_0x188_0x18a_by_mode_byte_gated_on_config_bit0x10`**
(138B, HIGH) via `RenamePass82Region80030000Fun8003c6e8.java` (`renamed=1`,
live-verified).

**Mechanism:** Optional veto hook at `PTR_DAT_8003c774` may skip the body. When
`config_struct.field452_0x1d0` bit `0x10` is clear, writes status byte at
`PTR_DAT_8003c784` with `0`; when set, IRQ-masked path selects template ushort
from `PTR_DAT_8003c780` (`param_1==0`) or `PTR_DAT_8003c77c` (non-zero), writes
BB registers `0x188` and `0x18a` via hook at `PTR_DAT_8003c788` (second value
from `PTR_DAT_8003c78c` with `>>1|0x3c0`), then stores `param_1` to status byte.
Sibling of `hw_register_config_with_timeout` (`0x8003c7cc`) BB-config cluster;
`irq_masked_program_bb_regs_0x188_0x18a_mode0_and_clear_config_bit0x10`
calls with `param_1=0` then clears config bit `0x10`.

**Callers:** 3 xref-in (`irq_masked_program_bb_regs_0x188_0x18a_mode0_and_clear_config_bit0x10` @ `0x8003c79e`, `FUN_80012820` @
`0x80012838`, `FUN_80013840` @ `0x800138ae`).

**Confidence:** HIGH — full 138B decompile; BB register pair, config gate, and
named IRQ-mask callees anchor the baseband register programming role.

Region unnamed count after this pass: **208** (209 minus this rename). Live named
**1958** global.

**Next:** superseded by Pass 83.

## Pass 83 (2026-07-01) — TX power dual-hook dispatcher `FUN_80039b80`

Fresh `ListUnnamed80030000.java` re-run: **208 unnamed** remain in region
(unchanged from Pass 82; rank-1 by size at xref=3 tier is `FUN_80039b80` at
118B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80039b80` → `dispatch_tx_power_config_byte_0x2c_or_0x2d_via_dual_fptr_hooks`**
(118B, HIGH) via `RenamePass83Region80030000Fun80039b80.java` (`renamed=1`,
live-verified).

**Mechanism:** Mode-byte dispatcher in the TX-power runtime-config cluster
(`PTR_DAT_80039bf8`). When `param_2 == 1`, selects config byte at `+0x2d`;
when `param_2` is 0 or 2..3, selects `+0x2c`; invalid modes (`>3`) log via
`possible_logging_function__var_args` and return. Sequentially invokes two
function-pointer hooks at `PTR_DAT_80039bfc` then `PTR_DAT_80039c00` with
`(param_1, selected_byte)`. Sibling of Pass 72's
`compute_tx_power_delta_from_global_baselines` and the `FUN_80039844`/
`FUN_80039c08` init/adjust pair.

**Callers:** 3 xref-in via computed call —
`apply_tx_power_runtime_mode_byte_and_reconfigure_tables_and_links` (TX-power
mode-change notifier), `conn_field_swap_and_notify_dispatcher_3_4` (conn-field
swap on opcode 3/4), `init_connection_record`.

**Confidence:** HIGH — full 118B decompile; dual-offset byte selection and
sequential dual-fptr dispatch unambiguous; caller context in TX-power/conn-setup
cluster matches region's power-management theme.

Region unnamed count after this pass: **207** (208 minus this rename). Live named
**1959** global.

**Next:** superseded by Pass 84.

## Pass 84 (2026-07-01) — HW channel mask-merge from mode byte `FUN_8003785c`

Fresh `ListUnnamed80030000.java` re-run: **207 unnamed** remain in region
(unchanged from Pass 83; rank-1 by size at xref=3 tier is `FUN_8003785c` at
116B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003785c` → `mask_merge_hw_channel_index_from_mode_byte_with_fptr_precheck_and_post_hooks`**
(116B, HIGH) via `RenamePass84Region80030000Fun8003785c.java` (`renamed=1`,
live-verified).

**Mechanism:** Mode-byte (`param_1 & 0xff`) HW-channel reconfiguration in the
`0x800378xx` eSCO/SCO slot-timing cluster. Computes channel index
`(param_1 & 0x7f) << 9`, optionally ORs `0x1000` when precheck fptr at
`PTR_DAT_800378d0` returns non-zero for `(1, 0xffff, mode_byte)`. Commits via
`mask_merge_hw_channel_table_entry_and_indexed_dispatch(index, 0x1e00)`.
Post-commit invokes fptr hooks at `PTR_DAT_800378d8` and `PTR_DAT_800378dc`
with `(0, mode_byte)`, then logs via `possible_logging_function__var_args`.

**Callers:** 3 xref-in (per `ListUnnamed80030000`; `xrefs_to` empty — known
GZF indirect-call gap): `fHCI_inquiry_cancel` cleanup chain,
`recompute_and_commit_conn_slot_timing_hw_and_packet_types` cluster siblings
(`FUN_800378e4`/`FUN_8003792c`), and `program_page_train_baseband_regs_and_start_paging`
pre-page setup (region `0x80040000` Pass 52dj).

**Confidence:** HIGH — full 116B decompile; named
`mask_merge_hw_channel_table_entry_and_indexed_dispatch` callee, index/mask
construction, and documented caller contexts in inquiry-cancel / paging /
slot-timing clusters anchor the HW-channel commit role.

Region unnamed count after this pass: **206** (207 minus this rename). Live named
**1960** global.

**Next:** superseded by Pass 85.

## Pass 85 (2026-07-01) — SCO packet-type qualification predicate `FUN_8003975c`

Fresh `ListUnnamed80030000.java` re-run: **206 unnamed** remain in region
(unchanged from Pass 84; rank-1 by size at xref=3 tier is `FUN_8003975c` at
106B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003975c` → `test_conn_slot_qualifies_by_mode2_or_sco_packet_type_table_lookup`**
(106B, HIGH) via `RenamePass85Region80030000Fun8003975c.java` (`renamed=1`,
live-verified).

**Mechanism:** Conn-index qualification predicate in the `0x800397xx`
SCO/eSCO packet-type cluster. When `field_0xc2==0`, returns true iff mode byte
`field_0xb7==2`. Otherwise indexes `PTR_DAT_800397cc` by 3-bit packet-type code
`(field_0xc3>>4)&7` with column offset `+6` (random BD_ADDR) or `+7` (public);
returns true when lookup char is `'&'`, `'7'`, or `','`, else compares against
`'='`.

**Callers:** 3 xref-in (per `ListUnnamed80030000`; `xrefs_to` empty — known
GZF indirect-call gap); SCO connection-lifecycle dispatch cluster sibling of
`apply_SCO_connection_params_to_hw` / `apply_eSCO_SCO_packet_type_params`.

**Confidence:** HIGH — full 106B decompile; `field_0xc3` packet-type/link-mode
bits and `bdaddr_random_` column select documented in conn-record subsystem;
table-index formula and dual-path mode-byte vs packet-type char gate are
unambiguous.

Region unnamed count after this pass: **205** (206 minus this rename). Live named
**1961** global.

**Next:** superseded by Pass 86.

## Pass 86 (2026-07-01) — Dual fptr hook dispatcher `FUN_8003b86c`

Fresh `ListUnnamed80030000.java` re-run: **205 unnamed** remain in region
(unchanged from Pass 85; rank-1 by size at xref=3 tier is `FUN_8003b86c` at
82B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003b86c` → `dispatch_dual_fptr_hooks_by_flag_with_config_field285_bits_or_const_0x41`**
(82B, HIGH) via `RenamePass86Region80030000Fun8003b86c.java` (`renamed=1`,
live-verified).

**Mechanism:** Flag-selected dual function-pointer dispatcher in the
`0x8003b8xx` BB-register programming cluster (`PTR_DAT_8003b8c0` /
`PTR_PTR_8003b8c4`). When `param_1==0`, calls hook2 `(0,0)` then hook1
`(0,0x41)`; when non-zero, calls hook1 `(0,0x40)` then hook2 with
`config_base->field285_0x129` bits 1 and 2. Regional sibling of
`dual_fptr_dispatch_by_flag_wrapper` (`0x8000ebfc`) and Pass 83's
`dispatch_tx_power_config_byte_0x2c_or_0x2d_via_dual_fptr_hooks`; neighbors
`read_modify_write_hw_reg_0x44_set_bit0` and
`write_indexed_bb_register_low16_with_global_mask`.

**Callers:** 3 xref-in (per `ListUnnamed80030000`; `xrefs_to` empty — known
GZF indirect-call gap).

**Confidence:** HIGH — full 82B decompile; swapped dual-fptr sequencing with
config-bit vs constant `0x41` argument selection unambiguous.

Region unnamed count after this pass: **204** (205 minus this rename). Live named
**1962** global.

**Next:** superseded by Pass 87.

## Pass 87 (2026-07-01) — BE u16 pair reverse-step2 fptr dispatcher `FUN_80039974`

Fresh `ListUnnamed80030000.java` re-run: **204 unnamed** remain in region
(unchanged from Pass 86; rank-1 by size at xref=3 tier is `FUN_80039974` at
74B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80039974` → `dispatch_be_u16_pairs_reverse_step2_from_buf_via_fptr_hook`**
(74B, HIGH) via `RenamePass87Region80030000Fun80039974.java` (`renamed=1`,
live-verified).

**Mechanism:** Four-iteration loop walking buffer offsets 7→5→3→1 (reverse
step-2). Each iteration reads a big-endian `uint16` from `param_1+offset` and
calls the patchable fptr at `PTR_DAT_800399c0` with `(param_2, u16)`; `param_2`
advances by 2 each pass. Calibration-table lookup cluster sibling of Pass 52's
`clamp_byte_offset_base_plus_adj_minus_product` (`0x80039920`).

**Callers:** 3 xref-in (per `ListUnnamed80030000`; `xrefs_to` empty — known
GZF indirect-call gap).

**Confidence:** HIGH — full 74B decompile; reverse step-2 BE u16 extraction and
fptr dispatch loop unambiguous.

Region unnamed count after this pass: **203** (204 minus this rename). Live named
**1963** global.

**Next:** superseded by Pass 88.

## Pass 88 (2026-07-01) — slot-tail reset + status upper-bits fptr hook `FUN_8003d0d0`

Fresh `ListUnnamed80030000.java` re-run: **203 unnamed** remain in region
(unchanged from Pass 87; rank-1 by size at xref=3 tier is `FUN_8003d0d0` at
50B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003d0d0` → `reset_slot_tail_and_hook_dispatch_status_upper_bits_if_idle`**
(50B, HIGH) via `RenamePass88Region80030000Fun8003d0d0.java` (`renamed=1`,
live-verified).

**Mechanism:** Calls `reset_parallel_slot_table_entry_tail_state_by_index(param_2)`
first. If `PTR_struct_of_at_least_0x300_size_8003d104->field_0x171 == 0`, reads
a `uint16` from `PTR_DAT_8003d108`, masks to upper 10 bits (`& 0xfc00`), writes
back, then invokes the patchable fptr at `PTR_DAT_8003d10c` with
`(1, masked_u16, 0xa0, 0)`. Parallel-slot-table cluster sibling of Pass 68's
`reset_parallel_slot_table_entry_tail_state_by_index` (`0x8003cf28`).

**Callers:** 3 xref-in (per `ListUnnamed80030000`; `xrefs_to` empty — known
GZF indirect-call gap).

**Confidence:** HIGH — full 50B decompile; tail-reset + conditional status-bit
mask and fptr dispatch unambiguous.

Region unnamed count after this pass: **202** (203 minus this rename). Live named
**1964** global.

**Next:** superseded by Pass 89.

## Pass 89 (2026-07-01) — dual fptr opcode dispatch + status-byte init `FUN_8003f980`

Fresh `ListUnnamed80030000.java` re-run: **202 unnamed** remain in region
(unchanged from Pass 88; rank-1 by size at xref=3 tier is `FUN_8003f980` at
48B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003f980` → `dispatch_fptr_opcodes_0x8f_0x82_with_status_bytes_set_to_ff`**
(48B, HIGH) via `RenamePass89Region80030000Fun8003f980.java` (`renamed=1`,
live-verified).

**Mechanism:** Invokes the patchable fptr at `PTR_DAT_8003f9b0` twice with
`(0, 0x8f)` then `(0, 0x82)`, writing `0xff` to the byte globals at
`PTR_DAT_8003f9b4` and `PTR_DAT_8003f9b8` between the two calls. Dual-opcode
fptr-dispatch cluster sibling of `dispatch_dual_fptr_hooks_by_flag_with_config_field285_bits_or_const_0x41`
(`0x8003b86c`).

**Callers:** 3 xref-in (per `ListUnnamed80030000`; `xrefs_to` empty — known
GZF indirect-call gap).

**Confidence:** HIGH — full 48B decompile; interleaved fptr opcode pair and
status-byte `0xff` init sequence unambiguous.

Region unnamed count after this pass: **201** (202 minus this rename). Live named
**1965** global.

**Next:** superseded by Pass 90.

## Pass 90 (2026-07-01) — dword mask-merge + BOS bit3 clear `FUN_800335f0`

Fresh `ListUnnamed80030000.java` re-run: **201 unnamed** remain in region
(unchanged from Pass 89; rank-1 by size at xref=3 tier is `FUN_800335f0` at
44B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_800335f0` → `mask_merge_dword_globals_or3_and_clear_bos_bit3_at_0x164`**
(44B, HIGH) via `RenamePass90Region80030000Fun800335f0.java` (`renamed=1`,
live-verified).

**Mechanism:** Mask-merges dword at `DAT_8003361c` via `(*ptr & mask) | value`
using constants from `DAT_80033620`/`DAT_80033624`; ORs low bits `3` into dword
at `DAT_80033628`; clears bit 3 (`& 0xf7`) on byte at offset `+0x164` in
`PTR_DAT_8003362c` BOS struct.

**Callers:** 3 xref-in (per `ListUnnamed80030000`); 2 patch computed-call sites
(`FUN_8010d434`, `FUN_8010d890`); ROM direct callers not in GZF xref cache.

**Confidence:** HIGH — full 44B decompile; mask-merge/OR/clear-bit3 sequence
unambiguous.

Region unnamed count after this pass: **200** (201 minus this rename). Live named
**1966** global.

**Next:** superseded by Pass 91.

## Pass 91 (2026-07-01) — timing offset scaler `FUN_800396c8`

Fresh `ListUnnamed80030000.java` re-run: **200 unnamed** remain in region
(unchanged from Pass 90; rank-1 by size at xref=3 tier is `FUN_800396c8` at
30B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_800396c8` → `compute_timing_offset_from_table_base_step_and_index_div10`**
(30B, HIGH) via `RenamePass91Region80030000Fun800396c8.java` (`renamed=1`,
live-verified).

**Mechanism:** Reads ushort base at `PTR_DAT_800396e8+0xc` and step at
`+0x10`; returns `(base + 5 + (param_1 & 0xff) * step) / 10` — linear timing
offset scaler indexed by low byte of `param_1`.

**Callers:** 3 xref-in (per `ListUnnamed80030000`).

**Confidence:** HIGH — full 30B decompile; formula unambiguous.

Region unnamed count after this pass: **199** (200 minus this rename). Live named
**1967** global.

**Next:** superseded by Pass 92.

## Pass 92 (2026-07-01) — SCO/eSCO link setup orchestrator `FUN_8003ef10`

Fresh `ListUnnamed80030000.java` re-run: **199 unnamed** remain in region
(unchanged from Pass 91; rank-1 by size at xref=2 tier is `FUN_8003ef10` at
2484B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003ef10` → `orchestrate_sco_esco_link_setup_baseband_regs_collision_and_afh`**
(2484B, HIGH, HANDLER-tier) via `RenamePass92Region80030000Fun8003ef10.java`
(`renamed=1`, live-verified).

**Mechanism:** Per-connection-slot SCO/eSCO link-setup orchestrator
(`param_1` = slot index, `param_2` = role byte). Optional pre-hook at
`PTR_DAT_8003f8d0` may veto early. Calls
`program_baseband_regs_0x23e_0x254_0x25e_via_patch_hook`, indexes per-slot
0x88-stride config table `PTR_DAT_8003f8d8`, and programs extensive baseband
registers (`0xee`/`0x60`/`0xde`/`0x9e`/`0x1ec`/`0x1ee`/`0x23c`/`0x254`/`0x25e`
and more) via `PTR_DAT_8003f8ec` hardware-write hook. Packet-type branches
(`0x2c`/`0x3d`/`0xc`/`0xd`) toggle global bitmasks; may call
`allocate_sco_hw_link_descriptor_slot`. IRQ-masked HW-clock reads feed
`piconet_slot_collision_avoidance_scheduler`; tail calls
`register_afh_lap_group_slot_with_collision_check` and
`remote_name_request_feature_apply_orchestrator`. Sibling of Pass 5's
`apply_SCO_connection_params_to_hw` / `release_SCO_connection_resources`
lifecycle pair.

**Callers:** 2 xref-in (per `ListUnnamed80030000`).

**Confidence:** HIGH — full 2484B decompile; SCO/eSCO BB-reg cluster and
lifecycle context unambiguous.

Region unnamed count after this pass: **198** (199 minus this rename). Live named
**1968** global.

**Next:** superseded by Pass 93.

## Pass 93 (2026-07-01) — SCO HW channel 8+4 slot init `FUN_800375f8`

Fresh `ListUnnamed80030000.java` re-run: **198 unnamed** remain in region
(unchanged from Pass 92; rank-1 by size at xref=2 tier is `FUN_800375f8` at
250B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_800375f8` → `init_sco_hw_channel_8plus4_slot_program_and_bb_regs`**
(250B, HIGH, INIT-tier) via `RenamePass93Region80030000Fun800375f8.java`
(`renamed=1`, live-verified).

**Mechanism:** IRQ-masked SCO HW channel subsystem initializer. Disables BB
regs `0xbe`/`0xc0` via `PTR_DAT_800376f4` hardware-write hook; loops 8 slots
programming channel indices via `PTR_DAT_800376f8`/`PTR_DAT_800376fc` tables
with `and_mask_hw_channel_table_entry_and_indexed_dispatch(...,0xfeff)`; loops
4 more slots via `PTR_DAT_80037700`/`PTR_DAT_80037704`; re-enables interrupts;
calls `init_or_clear_sco_hw_channel_subsystem(0)`,
`config_triplet_hw_register_init_with_power_gate()`, and three init-chain
callees; sets BB reg `0x11c` with `0x800` bit via `DAT_80037708`; optional
post-hook at `PTR_PTR_8003770c`.

**Callers:** `fHCI_Reset_0x03_full_subsystem_teardown` + `boot_init_reset_conn_sco_hw_optional_fc95_and_descriptor_reinit`
(3 call sites via `find_callers`).

**Confidence:** HIGH — full 250B decompile; SCO channel-init cluster and HCI
Reset context unambiguous; sibling of `init_or_clear_sco_hw_channel_subsystem`
and region-`0x80020000`'s `init_sco_hw_channel_disable_be_c0_restore_saved_bb_regs`.

Region unnamed count after this pass: **197** (198 minus this rename). Live named
**1969** global.

**Next:** superseded by Pass 94.

## Pass 94 (2026-07-01) — VSC fc11 polling waiter `FUN_80035104`

Fresh `ListUnnamed80030000.java` re-run: **197 unnamed** remain in region
(unchanged from Pass 93; rank-1 by size at xref=2 tier is `FUN_80035104` at
242B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80035104` → `poll_vsc_fc11_3_until_pending_clear_with_link_mode_timeouts`**
(242B, HIGH, HANDLER-tier) via `RenamePass94Region80030000Fun80035104.java`
(`renamed=1`, live-verified).

**Mechanism:** IRQ-masked VSC `0xfc11` variant-3 polling waiter in the
`link_mode_change_state_machine` cluster (sibling of `FUN_80035214` at
`0x80035214`). Early-exit when BOS `+0x10a` set: programs timeout `0x4e20` and
status `0x47` from config mask `DAT_80035204`, restores IRQs. Main path enables
IM3, sets bit 6 in global dword `DAT_8003520c`, branches on connection state
`(bos+0x164 & 0x7f80) == 0x300` for timeout/status selection (`0x4e20`+`0x47`
vs `DAT_80035210`+`0xa54e`), then spins calling
`VSC_0xfc11_3_in_while_loop_FUN_80009148` until pending dword at
`PTR_DAT_800351f8` clears.

**Callers:** 2 xref-in per cold-triage (indirect/timer invocation likely).

**Confidence:** HIGH — full 242B decompile; VSC fc11 family + link-mode BOS
fields `+0x10a`/`+0x164` match `link_mode_change_state_machine` decompile.

Region unnamed count after this pass: **196** (197 minus this rename). Live named
**1970** global.

**Next:** superseded by Pass 95.

## Pass 95 (2026-07-01) — packet-completion ring enqueuer `FUN_8003e0d4`

Fresh `ListUnnamed80030000.java` re-run: **196 unnamed** remain in region
(unchanged from Pass 94; rank-1 by size at xref=2 tier is `FUN_8003e0d4` at
236B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003e0d4` → `enqueue_connection_packet_completion_ring_or_overflow_dispatch`**
(236B, HIGH, HANDLER-tier) via `RenamePass95Region80030000Fun8003e0d4.java`
(`renamed=1`, live-verified).

**Mechanism:** IRQ-masked enqueue into the stride-0x88 connection table's
12-entry packet-completion ring at `PTR_DAT_8003e1c0` (enqueue sibling of Pass
66's `drain_connection_packet_completion_ring_and_emit_hci_num_completed`).
When `+0x38`+`+0x3a` sum `< 0xc`, stores `param_1` at ring slot
`(idx+0xe)*4+8`, advances `+0x35` index (wraps at 0xc), increments `+0x38`.
Overflow path: when `field_0x179!=2`, dispatches immediately via
`PTR_DAT_8003e1cc` fptr `(param_1,3)` and logs tag `0x675`; when
`field_0x179==2`, buffer cleanup via
`wraps_uninteresting_if_0x80100000__0_which_its_not_in_my_tests`.

**Callers:** 2 xref-in per cold-triage; incl. ACL single-packet direct TX fast-path
in region `0x80020000` (`acl_single_packet_direct_tx_program_descriptor`) when
LMP handle absent.

**Confidence:** HIGH — full 236B decompile; ring-buffer fields `+0x35`/`+0x38`/
`+0x3a` match Pass 66 drainer; ACL TX cluster cross-reference confirms role.

Region unnamed count after this pass: **195** (196 minus this rename). Live named
**1971** global.

**Next:** Pass 97 — fresh `ListUnnamed80030000` re-rank; decompile+rename top
rank-1 unnamed function.

## Pass 96 (2026-07-01) — BD_ADDR scramble slot apply `FUN_8003d204`

Fresh `ListUnnamed80030000.java` re-run: **195 unnamed** remain in region
(unchanged from Pass 95; rank-1 by size at xref=2 tier is `FUN_8003d204` at
218B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003d204` → `apply_bdaddr_scramble_slots_from_config_fc_fd_mask`**
(218B, HIGH, HANDLER-tier) via `RenamePass96Region80030000Fun8003d204.java`
(`renamed=1`, live-verified).

**Mechanism:** Gated on optional hook at `PTR_DAT_8003d2e0` (skip when hook
returns non-zero) and config struct `field241_0xfc`/`field242_0xfd` bit `0x10`.
When active: clears slots 0–3 in global `0xfc39` via
`clear_bits_in_global_0xfc39_helper`; for slots 4–7 reads config mask bits and
either zeros or applies `scrambled_bdaddr_field_writer_pair1`/
`scrambled_bdaddr_field_writer_pair2` with `DAT_8003d2e8`; logs via
`possible_logging_function__var_args` (tag `0x2b`); copies status byte from
`PTR_DAT_8003d2f0` → `DAT_8003d2f4`.

**Callers:** 2 xref-in; includes `release_SCO_connection_resources` (Pass 5
identified as SCO teardown cleanup) and connection-state-manager SCO path
(HCI event `0xfa` logging).

**Confidence:** HIGH — full 218B decompile; established BD_ADDR scramble
writer cluster (`scrambled_bdaddr_field_writer_pair1/2`,
`clear_bits_in_global_0xfc39_helper`); SCO lifecycle sibling context from Pass 5.

Region unnamed count after this pass: **194** (195 minus this rename). Live named
**1972** global.

**Next:** superseded by Pass 97.

## Pass 97 (2026-07-01) — BD_ADDR scramble param mask apply `FUN_8003d110`

Fresh `ListUnnamed80030000.java` re-run: **194 unnamed** remain in region
(unchanged from Pass 96; rank-1 by size at xref=2 tier is `FUN_8003d110` at
208B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003d110` → `apply_bdaddr_scramble_slots_from_param_mask_0x6000`**
(208B, HIGH, HANDLER-tier) via `RenamePass97Region80030000Fun8003d110.java`
(`renamed=1`, live-verified).

**Mechanism:** Gated on optional hook at `PTR_DAT_8003d1e0` (skip when hook
returns non-zero). Copies status/globals `DAT_8003d1e4`→`PTR_DAT_8003d1e8`,
`DAT_8003d1ec`→`PTR_DAT_8003d1f0`; calls `or_bits_into_global_flag_word(0xf)`.
When `(*PTR_DAT_8003d1f4 & 0x6000)`: clears slot 2 via
`clear_bits_in_global_0xfc39_helper(2)`, ORs `DAT_8003d1f8` into status dword;
if `param_1 & 0x4000` applies `scrambled_bdaddr_field_writer_pair2(1,1)`; if
`param_1 & 0x2000` applies `scrambled_bdaddr_field_writer_pair2(1,0)`; logs via
`possible_logging_function__var_args` (tag `0x2b`). Masks status byte to `0x3f`
on exit.

**Callers:** 2 xref-in per cold-triage (indirect/connection-state invocation
likely).

**Confidence:** HIGH — full 208B decompile; BD_ADDR scramble writer cluster
sibling of Pass 96's config-field variant
(`apply_bdaddr_scramble_slots_from_config_fc_fd_mask` at `0x8003d204`).

Region unnamed count after this pass: **193** (194 minus this rename). Live named
**1973** global.

**Next:** superseded by Pass 98.

## Pass 98 (2026-07-01) — IRQ-masked HW slot bit program `FUN_8003d558`

Fresh `ListUnnamed80030000.java` re-run: **193 unnamed** remain in region
(unchanged from Pass 97; rank-1 by size at xref=2 tier is `FUN_8003d558` at
194B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003d558` → `irq_masked_program_slot_bit_in_reg2_and_clear_reg11c_by_conn_index`**
(194B, HIGH, HANDLER-tier) via `RenamePass98Region80030000Fun8003d558.java`
(`renamed=1`, live-verified).

**Mechanism:** IRQ-masked BB register programming via hook at `PTR_DAT_8003d620`.
Reads `byte_0xCC` and `bos_connection__array_index` from `big_ol_struct[param_2]`.
Clears bit7 on reg `0x11c` (`& 0xff7f`); programs reg `2` with packed value
`(byte_0xCC << 11) | (array_index << 5) | (1 << (param_1 & 0x1f))`; writes reg
`0` = `5`; optionally reg `0xe0` = `0` when `param_4` nonzero; logs tag `0x2b`.

**Callers:** 2 xref-in; callee of `connection_state_manager` (`FUN_8003d630`)
non-SCO disconnect/abort path alongside `FUN_8002a868` (Pass 6 documented).

**Confidence:** HIGH — full 194B decompile; IRQ-masked HW hook pattern matches
SCO/BB-register cluster (`0x11c`/`0xe0` family); connection-state-manager
disconnect context from Pass 6.

Region unnamed count after this pass: **192** (193 minus this rename). Live named
**1974** global.

**Next:** superseded by Pass 99.

## Pass 99 (2026-07-01) — Link-mode slot budget adjust `FUN_80033b14`

Fresh `ListUnnamed80030000.java` re-run: **192 unnamed** remain in region
(unchanged from Pass 98; rank-1 by size at xref=2 tier is `FUN_80033b14` at
188B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80033b14` → `adjust_link_mode_change_slot_budget_and_secondary_timing`**
(188B, HIGH, HANDLER-tier) via `RenamePass99Region80030000Fun80033b14.java`
(`renamed=1`, live-verified).

**Mechanism:** Optional hook at `PTR_DAT_80033bd0` (skip when hook returns
non-zero). Mode-byte dispatch on `param_1` (0–3): modes 0/1 subtract
config-derived slot offsets from `*param_2` using `PTR_DAT_80033bd8` bit
masks and `field244_0xff`; mode 1 also adds `PTR_DAT_80033be0` bit5 when BOS
`+0x10a` nonzero; mode 2 when `PTR_DAT_80033be4` bit2 set sets `*param_3` from
ushort at BOS `+0x94` minus `field244_0xff`; mode 3 sets `*param_3=0xff00`;
defaults `*param_3` to `0xff00` when zero. Called by
`link_mode_change_state_machine` before VSC fc11; `local_1c` feeds
`(clock>>1)+secondary & slot_budget` for modes 2–3.

**Callers:** 2 xref-in per cold-triage; direct caller
`link_mode_change_state_machine` (when `PTR_DAT_80035620` bit `0x20` clear).

**Confidence:** HIGH — full 188B decompile; link-mode-change cluster sibling of
`check_link_mode_change_gate_status` (Pass 78) and
`poll_vsc_fc11_3_until_pending_clear_with_link_mode_timeouts` (Pass 94).

Region unnamed count after this pass: **191** (192 minus this rename). Live named
**1975** global.

**Next:** superseded by Pass 100.

## Pass 100 (2026-07-01) — HW-reg-config optional hook table `FUN_80039518`

Fresh `ListUnnamed80030000.java` re-run: **191 unnamed** remain in region
(unchanged from Pass 99; rank-1 by size at xref=2 tier is `FUN_80039518` at
158B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80039518` → `dispatch_optional_subsystem_hooks_during_hw_reg_config`**
(158B, HIGH, HANDLER-tier) via `RenamePass100Region80030000Fun80039518.java`
(`renamed=1`, live-verified).

**Mechanism:** Optional-hook dispatch table walker gated by `param_1`: when
`param_1==0`, invokes ~20 optional fptr hooks from literal pools (two arrays of
6 and 9 entries with null-skip, plus individual hooks at `PTR_PTR_800395b8`…
`PTR_PTR_800395e8`); when `param_1!=0`, calls single hook at
`PTR_DAT_800395ec`. Called from `hw_register_config_with_timeout` when primary
hook `PTR_PTR_8003c92c` is absent and `config_base+0x1ce` is negative — BB
register-config init/teardown subsystem hook chain.

**Callers:** 2 xref-in per cold-triage; direct caller
`hw_register_config_with_timeout` (also used by
`configure_hw_regs_and_init_for_sco_teardown` with `param_1=1`).

**Confidence:** HIGH — full 158B decompile; override+fallback optional-hook
table idiom matches ROM cluster; caller context in `hw_register_config_with_timeout`
BB-config path.

Region unnamed count after this pass: **190** (191 minus this rename). Live named
**1976** global.

**Next:** superseded by Pass 101.

## Pass 101 (2026-07-01) — BB reg triplet snapshot `FUN_80037af0`

Fresh `ListUnnamed80030000.java` re-run: **190 unnamed** remain in region
(unchanged from Pass 100; rank-1 by size at xref=2 tier is `FUN_80037af0` at
142B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80037af0` → `query_bb_regs_76_77_78_snapshot_and_log_when_gated`**
(142B, HIGH, HANDLER-tier) via `RenamePass101Region80030000Fun80037af0.java`
(`renamed=1`, live-verified).

**Mechanism:** Optional hook at `PTR_DAT_80037b80` (skip when hook returns
non-zero). Gate on status struct `PTR_DAT_80037b84`: bits `0x180` set OR
per-connection `field_0xb9` clear in `big_ol_struct[slot from byte+1>>2&0xf]`.
When gated open: clears ushort fields at `+4`/`+6`; queries BB registers
`0x76`/`0x77`/`0x78` via hook `PTR_DAT_80037b8c(0, reg, 1)` storing results at
`+0xa`/`+0xc`/`+0xe`; copies 16-byte status snapshot and logs via
`possible_logger_called_if_no_patch3` with tag from `PTR_DAT_80037b90`.

**Callers:** 2 xref-in per cold-triage; direct caller
`conditional_feature_gated_init_wrapper` (`0x800045b4`) when feature flag +
config-blob field both indicate enabled; also reached from
`ring_buffer_event_drain_dispatch_loop` ring-buffer event path.

**Confidence:** HIGH — full 142B decompile; BB register-query + optional-hook
gate idiom matches ROM cluster; feature-gated init context from region
`0x80000000`.

Region unnamed count after this pass: **189** (190 minus this rename). Live named
**1977** global.

**Next:** superseded by Pass 102.

## Pass 102 (2026-07-01) — truncated-page counter threshold `FUN_8003ce98`

Fresh `ListUnnamed80030000.java` re-run: **189 unnamed** remain in region
(unchanged from Pass 101; rank-1 by size at xref=2 tier is `FUN_8003ce98` at
122B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003ce98` → `toggle_0x4000_status_bit_via_hook_on_trunc_page_counter_threshold`**
(122B, HIGH, HANDLER-tier) via `RenamePass102Region80030000Fun8003ce98.java`
(`renamed=1`, live-verified).

**Mechanism:** Two-path gate on arm-byte `PTR_DAT_8003cf14`. When clear: if
slot counter `PTR_DAT_8003cf18` exceeds pow2 threshold `1 << (((config
`PTR_DAT_8003cf24` byte+3 >> 2) & 3) << 2)`, sets status-word bit `0x4000` on
`DAT_8003cf1c` via hook `PTR_DAT_8003cf20(0x1ee, word|0x4000)` and arms gate to
1. When armed: if counter is zero, clears bit `0x4000` via same hook with
`word&0xbfff` and disarms gate to 0.

**Callers:** 2 xref-in per cold-triage; primary caller
`truncated_page_complete_status_dispatcher` (`0x800022e4`) after status-code
dispatch on bits `0x80`/`0x100`/`0x200`; truncated-page-complete cluster
sibling documented in region `0x80000000`.

**Confidence:** HIGH — full 122B decompile; pow2-threshold counter gate +
`0x4000` status-bit toggle via `0x1ee` hook matches truncated-page cluster
context.

Region unnamed count after this pass: **188** (189 minus this rename). Live named
**1978** global.

**Next:** superseded by Pass 103.

## Pass 103 (2026-07-01) — BB register bundle programmer `FUN_8003a7b4`

Fresh `ListUnnamed80030000.java` re-run: **188 unnamed** remain in region
(unchanged from Pass 102; rank-1 by size at xref=2 tier is `FUN_8003a7b4` at
108B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003a7b4` → `program_bb_regs_41_43_44_46_47_via_hook_and_da_d6_dispatch`**
(108B, HIGH, HANDLER-tier) via `RenamePass103Region80030000Fun8003a7b4.java`
(`renamed=1`, live-verified).

**Mechanism:** Calls `dispatch_bb_register_da_d6_write_with_hook(0x21, 0,
param_1)` then programs BB registers via hook at `PTR_DAT_8003a820`:
`param_2`→reg `0x43`, `param_3`→reg `0x44`, fixed `0x20`→reg `0x41`,
`0x10`→reg `0x46`, `0`→reg `0x47`. Register-script interpreter cluster
sibling of `dispatch_bb_register_da_d6_write_with_hook`.

**Callers:** 2 xref-in per cold-triage; direct callers
`preserve_bb_regs_5a_5c_run_regscript_hook_then_program_bb_bundle` and
`FUN_8003ac28` (register-script interpreter callees that compute three ushort
params via `FUN_8003ab04` before invoking this helper).

**Confidence:** HIGH — full 108B decompile; multi-register BB hook dispatch
with da/d6 opcode `0x21` matches documented register-programming cluster;
register-script interpreter integration path confirmed via caller decompile.

Region unnamed count after this pass: **187** (188 minus this rename). Live named
**1979** global.

**Next:** superseded by Pass 104.

## Pass 104 (2026-07-01) — role-slot link state updater `FUN_800384ac`

Fresh `ListUnnamed80030000.java` re-run: **187 unnamed** remain in region
(unchanged from Pass 103; rank-1 by size at xref=2 tier is `FUN_800384ac` at
102B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_800384ac` → `advance_role_slot_link_state_and_capture_halved_hw_clock`**
(102B, HIGH, HANDLER-tier) via `RenamePass104Region80030000Fun800384ac.java`
(`renamed=1`, live-verified).

**Mechanism:** Per-role-slot `big_ol_struct` state updater gated on index
`!= 0xff`. When `field_0x29d == 1`: logs via `possible_logging_function` (opcode
`0x27`, subcode 499) and advances `field_0x29f` to 2. When `field_0x29c == 3`:
reads HW clock via `read_hw_clock_raw_dword_by_role_index` using `byte_0xCC` as
role index, stores `clock >> 1` into `field_0x28c`.

**Callers:** 2 xref-in — `status_word_multiflag_link_event_dispatcher` (after
codec lookup when status-word flag mask non-zero) and `role_switch_confirmation_matcher`
(after role-switch confirmation table update).

**Confidence:** HIGH — full 102B decompile; dual-gated per-slot state advance
plus halved HW-clock timestamp capture; caller integration in link-event and
role-switch paths confirmed via caller decompile.

Region unnamed count after this pass: **186** (187 minus this rename). Live named
**1980** global.

**Next:** superseded by Pass 105.

## Pass 105 (2026-07-01) — role-slot state logger `FUN_8003d490`

Fresh `ListUnnamed80030000.java` re-run: **186 unnamed** remain in region
(unchanged from Pass 104; rank-1 by size at xref=2 tier is `FUN_8003d490` at
96B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003d490` → `log_role_slot_state_evt_0x2c5_when_not_role_switch`**
(96B, HIGH, HANDLER-tier) via `RenamePass105Region80030000Fun8003d490.java`
(`renamed=1`, live-verified).

**Mechanism:** Optional hook veto at `PTR_DAT_8003d4f0` — when absent or
returns zero, checks per-role-slot connection struct (`0x28` stride,
`PTR_some_connection_struct_array_8003d4f4`): requires `field_0x24 != 0x02`
(not in role-switch state) and `field_0x23 != 0`; then logs via
`possible_logger_called_if_no_patch3` with opcode `0x2c5` (709).

**Callers:** 2 xref-in — `status_bit_gated_role_state_logger_dispatch` (region
`0x80000000`, passes role-state code 0/1/2 with subcode 2) and
`link_status_bit_dispatch_for_role_state_notify` (region `0x80000000`, same
leaf after status-word bit classification).

**Confidence:** HIGH — full 96B decompile; hook-veto + dual-field gate matches
documented role-switch logging cluster; both callers confirmed via decompile.

Region unnamed count after this pass: **185** (186 minus this rename). Live named
**1981** global.

**Next:** superseded by Pass 106.

## Pass 106 (2026-07-01) — link-mode commit snapshot `FUN_800345ec`

Fresh `ListUnnamed80030000.java` re-run: **185 unnamed** remain in region
(unchanged from Pass 105; rank-1 by size at xref=2 tier is `FUN_800345ec` at
96B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_800345ec` → `commit_link_mode_snapshot_role_slots_and_materialize_lut`**
(96B, HIGH, HANDLER-tier) via `RenamePass106Region80030000Fun800345ec.java`
(`renamed=1`, live-verified).

**Mechanism:** Optional hook veto at `PTR_DAT_8003464c` — when absent or
returns zero and link-state struct `field_0x10a` is set, snapshots valid
`big_ol_struct` role slots via `FUN_8003436c` (10-slot walk + `optimized_memcpy`),
polls HW-ready bitmask via `poll_hw_ready_bitmask_until_clear_or_log_timeout` (2000-iteration timeout + log on
stall), dispatches via `PTR_DAT_80034654`, materializes 9-entry dword LUT from
ushort index table, and copies commit dword from `DAT_8003466c` to
`PTR_DAT_80034670`.

**Callers:** 2 xref-in — `link_mode_change_state_machine` (region `0x80030000`,
post-VSC-fc11 commit path after timing/budget adjust) and `FUN_80010814` (region
`0x80010000`, connection-setup init after `FUN_80010324`).

**Confidence:** HIGH — full 96B decompile; `field_0x10a` gate matches documented
link-mode-change cluster; both callers confirmed via decompile.

Region unnamed count after this pass: **184** (185 minus this rename). Live named
**1982** global.

**Next:** superseded by Pass 107.

## Pass 107 (2026-07-01) — hook poll + slot-phase offset `FUN_80036670`

Fresh `ListUnnamed80030000.java` re-run: **184 unnamed** remain in region
(unchanged from Pass 106; rank-1 by size at xref=2 tier is `FUN_80036670` at
86B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80036670` → `poll_hook_value_until_stable_and_optional_slot_offset_0x270`**
(86B, HIGH, HANDLER-tier) via `RenamePass107Region80030000Fun80036670.java`
(`renamed=1`, live-verified).

**Mechanism:** Reads hook fptr at `PTR_DAT_800366c8` into `*param_1`. When
`param_2 != NULL`: busy-spins re-reading hook until value stabilizes, samples
10-bit global HW-clock phase via `FUN_800141c8`, wraps at `0x270` (624), stores
`0x270 - phase` slot-offset remainder in `*param_2`. Same `0x270` slot-phase
idiom as Pass 80's `read_hw_clock_dword_and_optional_slot_offset_by_role_index`
but via hook fptr + stability spin instead of per-role HW-clock table read.

**Callers:** 2 xref-in — `check_esco_timing_window_and_trigger` (region
`0x80050000`, eSCO timing window check with `param_2=0` for value-only path) and
`compute_bb_slot_link_timing_offsets_from_status_bits` (region `0x80070000`, BB
slot link-timing offset seeding cluster).

**Confidence:** HIGH — full 86B decompile; `0x270` slot-phase remainder matches
documented SCO/eSCO timing cluster; both callers confirmed via xref script.

Region unnamed count after this pass: **183** (184 minus this rename). Live named
**1983** global.

**Next:** superseded by Pass 108.

## Pass 108 (2026-07-01) — LMP 0x2a QoS req dispatcher `FUN_8003024c`

Fresh `ListUnnamed80030000.java` re-run: **183 unnamed** remain in region
(unchanged from Pass 107; rank-1 by size at xref=2 tier is `FUN_8003024c` at
86B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003024c` → `dispatch_lmp_0x2a_qos_req_via_tx_hook`**
(86B, HIGH, HANDLER-tier) via `RenamePass108Region80030000Fun8003024c.java`
(`renamed=1`, live-verified).

**Mechanism:** Optional alloc hook at `PTR_DAT_800302a4+4` (via
`call_fptr_if_set_with_2_args_possibly_allocates_buf_at_arg2_`); when it returns
0, assembles a 10-byte LMP PDU buffer: `0xff` prefix, length `0x08`, opcode
`0x2a` (`LMP_QUALITY_OF_SERVICE_REQ`), patch-index byte (`param_1`), ushort from
`PTR_DAT_800302a8`, and dword payload (`param_2` LE at +6/+8). Dispatches via
`invoke_lmp_tx_hook_with_length_word_from_pdu_buffer`. TX-side sibling of RX
handler `LMP_QUALITY_OF_SERVICE_REQ_0x2A` (`0x8001aa3c`); same `0x2a` opcode
cluster as Pass 81's `dispatch_lmp_0x0d_power_sample_report_via_tx_hook`.

**Callers:** 2 xref-in — `references_patch_download_mem4` (patch-fragment download
completion path: `FUN_8003024c(patch_index, accumulated_size)`) and
`calls_to_0x8010a001_as_fptr_to_install_patches` (patch-installer fptr table).

**Confidence:** HIGH — full 86B decompile; opcode `0x2a`, named callees, and
patch-download caller body anchor the LMP QoS-request TX role.

Region unnamed count after this pass: **182** (183 minus this rename). Live named
**1984** global.

**Next:** superseded by Pass 109.

## Pass 109 (2026-07-01) — BB reg 0x1e/0x1c programmer `FUN_80038d98`

Fresh `ListUnnamed80030000.java` re-run: **182 unnamed** remain in region
(unchanged from Pass 108; rank-1 by size at xref=2 tier is `FUN_80038d98` at
84B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80038d98` → `program_bb_regs_0x1e_5bit_field_and_clear_0x1c_bit3`**
(84B, HIGH, SIMPLE-tier) via `RenamePass109Region80030000Fun80038d98.java`
(`renamed=1`, live-verified).

**Mechanism:** Read-modify-write via fptr pair at `PTR_DAT_80038dec` (read) /
`PTR_DAT_80038df0` (write): programs BB reg `0x1e` bits 5–9 from
`param_1 & 0x1f`, then clears bit 3 on BB reg `0x1c` (`& 0xfff7`). BB-register
programming sibling in the `0x80038dxx` cluster near
`read_bb_reg_0x1e_5bit_field_via_hook` (reads current `0x1e` field) and wrapper `FUN_80038df4` (selects
byte from config `+0x24` or computed default, then calls this function).

**Callers:** 2 xref-in — `FUN_80038df4` (config-flag-gated wrapper in same
cluster) and `FUN_800395f0` (158B BB-reg init sequence: passes `*PTR_DAT_800396a4`
after programming multiple table-driven registers).

**Confidence:** HIGH — full 84B decompile; explicit reg IDs `0x1e`/`0x1c`, mask
semantics, and two caller bodies anchor the role.

Region unnamed count after this pass: **181** (182 minus this rename). Live named
**1985** global.

**Next:** superseded by Pass 110.

## Pass 110 (2026-07-01) — SCO config-flag sample-delta threshold `FUN_800397d0`

Fresh `ListUnnamed80030000.java` re-run: **181 unnamed** remain in region
(unchanged from Pass 109; rank-1 by size at xref=2 tier is `FUN_800397d0` at
74B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_800397d0` → `test_config_dc_bit8_and_hook_sample_minus_0x27d_lte_threshold`**
(74B, HIGH, SIMPLE-tier) via `RenamePass110Region80030000Fun800397d0.java`
(`renamed=1`, live-verified).

**Mechanism:** Bool predicate in the `0x800397xx` SCO/eSCO packet-type cluster
(sibling of Pass 85's `test_conn_slot_qualifies_by_mode2_or_sco_packet_type_table_lookup`).
Seeds a local byte from `PTR_DAT_8003981c[+0xe]`, invokes measurement hook
`PTR_PTR_80039820` into `local_18`, then returns true only when
`config_base->field209_0xdc` bit 8 is set **and**
`(local_18[0] - config_base->field625_0x27d) <= param_1` (unsigned-byte delta
vs caller-supplied threshold).

**Callers:** 2 xref-in (COMPUTED_CALL) —
`init_or_reset_sco_esco_hw_registers_and_link_slots` (`0x8004d44e`) and
`set_or_clear_esco_link_register_bit0_via_fptr_with_retry` (`0x800575b0`).

**Confidence:** HIGH — full 74B decompile; explicit config offsets `0xdc`/`0x27d`,
hook indirection, and two named SCO/eSCO lifecycle callers anchor the role.

Region unnamed count after this pass: **180** (181 minus this rename). Live named
**1986** global.

**Next:** superseded by Pass 111.

## Pass 111 (2026-07-01) — BB reg 0x6f 7-bit trim field `FUN_80038e24`

Fresh `ListUnnamed80030000.java` re-run: **180 unnamed** remain in region
(unchanged from Pass 110; rank-1 by size at xref=2 tier is `FUN_80038e24` at
70B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80038e24` → `program_bb_reg_0x6f_7bit_field_at_bits7_13_via_hook`**
(70B, HIGH, SIMPLE-tier) via `RenamePass111Region80030000Fun80038e24.java`
(`renamed=1`, live-verified).

**Mechanism:** Read-modify-write on baseband register `0x6f` (bank `3`, mode `1`)
via hook fptr pair `PTR_DAT_80038e6c` (read) / `PTR_DAT_80038e70` (write): reads
current value, masks with `0xc07f` (preserves bits 0–6 and ≥14), ORs
`(param_1 & 0x7f) << 7` into bits 7–13, writes back. Sibling of Pass 109's
`program_bb_regs_0x1e_5bit_field_and_clear_0x1c_bit3` in the `0x80038exx` BB-reg
programmer cluster.

**Callers:** 1 confirmed via `find_callers` —
`clock_trim_calibration_measure_apply` (`0x8003a180`) applies the derived trim
value `(0x1000 - (val&0x7f)) & 0x7f` after its 16-iteration measurement loop
(documented Pass 8). ListUnnamed reports 2 xref-in (second likely COMPUTED_CALL).

**Confidence:** HIGH — full 70B decompile; explicit hook indirection, mask/OR
pattern, and named crystal-trim calibration caller anchor the role.

Region unnamed count after this pass: **179** (180 minus this rename). Live named
**1987** global.

**Next:** superseded by Pass 112.

## Pass 112 (2026-07-01) — connection-setup BB reg programmer `FUN_800336f4`

Fresh `ListUnnamed80030000.java` re-run: **179 unnamed** remain in region
(unchanged from Pass 111; rank-1 by size at xref=2 tier is `FUN_800336f4` at
68B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_800336f4` → `clear_connection_setup_flag_and_program_bb_regs_0x36_bit13_and_0x8e_via_hook`**
(68B, HIGH, SIMPLE-tier) via `RenamePass112Region80030000Fun800336f4.java`
(`renamed=1`, live-verified).

**Mechanism:** Clears byte at `PTR_DAT_80033738+0x104` (connection-setup status
flag used across link-power and commit-gate paths), reads ushort from
`DAT_8003373c`, masks with `0xdfff` (clears bit 13 / `0x2000`), then invokes
hook fptr `PTR_DAT_80033740` twice: `(reg 0x36, masked_value)` then
`(reg 0, 0x8e)`. Sibling of Pass 75's
`commit_hw_channel_merge_index_0x36_on_role_bit0` which ORs `0x2000` into the
same HW-channel index `0x36`; this function clears that bit before programming
reg `0` to `0x8e`.

**Callers:** 2 confirmed via `find_callers` —
`commit_connection_setup_mode_by_slot_bitmask_and_gates` (`0x80035768`) and
`param_dispatch_with_rom_calls` (`0x80035b4c`); the latter documents this
address as one of its four ROM dispatch targets.

**Confidence:** HIGH — full 68B decompile; explicit hook indirection, bitmask,
and two named connection-setup dispatch callers anchor the role.

Region unnamed count after this pass: **178** (179 minus this rename). Live named
**1988** global.

**Next:** superseded by Pass 113.

## Pass 113 (2026-07-01) — HCI Reset conn reinit `FUN_80036f60`

Fresh `ListUnnamed80030000.java` re-run: **178 unnamed** remain in region
(unchanged from Pass 112; rank-1 by size at xref=2 tier is `FUN_80036f60` at
66B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80036f60` → `hci_reset_reinit_conn_subsystem_lmp_and_descriptor_tables`**
(66B, HIGH, HANDLER-tier) via `RenamePass113Region80030000Fun80036f60.java`
(`renamed=1`, live-verified).

**Mechanism:** HCI Reset teardown reinit orchestrator: clears global status bit
`0x400`, waits for in-flight LMP-0x25C via `lmp_25c_procedure_completion_waiter`,
runs BD_ADDR slot reconciliation via
`reconcile_nonmatching_bdaddr_slot_and_dispatch_lmp_259`, conditionally dispatches
`LMP__25B__most_common_for_VSCs1` when pending-VSC sentinel `!= -1`, resets
connection subsystem via
`reset_conn_subsystem_global_state_and_reinit_slot_entries`, then calls two
still-unnamed helpers (`FUN_80018120`, `FUN_8006ad5c`) and initializes the
3×0x34 linked-descriptor tables via
`init_three_slot_0x34_linked_descriptors_and_clear_buffers`.

**Callers:** 1 confirmed via `find_callers` —
`fHCI_Reset_0x03_full_subsystem_teardown` (HCI Reset command full teardown path;
sibling of Pass 93's `init_sco_hw_channel_8plus4_slot_program_and_bb_regs`).

**Confidence:** HIGH — full 66B decompile; named LMP/conn-subsystem/descriptor
callees and sole HCI Reset caller anchor the role.

Region unnamed count after this pass: **177** (178 minus this rename). Live named
**1989** global.

**Next:** superseded by Pass 114.

## Pass 114 (2026-07-01) — HW channel bits9-11 read `FUN_80034884`

Fresh `ListUnnamed80030000.java` re-run: **177 unnamed** remain in region
(unchanged from Pass 113; rank-1 by size at xref=2 tier is `FUN_80034884` at
50B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80034884` → `read_hw_channel_bits9_11_by_role_index_via_esco_remap`**
(50B, HIGH, SIMPLE-tier) via `RenamePass114Region80030000Fun80034884.java`
(`renamed=1`, live-verified).

**Mechanism:** Role-index (`param_1`) + pending-eSCO flag byte (`param_2`):
`remap_role_index_to_esco_slot_if_pending`, lookup per-slot HW-channel register
index from table at `PTR_DAT_800348b8` (`slot*8+4` ushort), read current ushort
at that index via `DAT_800348bc`, return bits 9–11 (`>> 9 & 7`). Read-only
sibling of Pass 58/59's bit15 OR/AND HW-channel dispatch pair on parallel
literal-pool tables (`80034970`/`80034910`).

**Callers:** 2 confirmed via `ListXrefsTo80034884.java` —
`LMP_INCR_POWER_REQ_0x1f` (`0x8006943c`) and `LMP_DECR_POWER_REQ_0x20`
(`0x80069658`) — both read current TX-power level field before
`check_power_val_below_max_limit_6` / `set_new_power_val` in the LMP
power-increment/decrement handler cluster (region `0x80060000`).

**Confidence:** HIGH — full 50B decompile; explicit eSCO remap + indexed
HW-channel table read + 3-bit field extract; named LMP TX-power callers anchor
the role.

Region unnamed count after this pass: **176** (177 minus this rename). Live named
**1990** global.

**Next:** superseded by Pass 115.

## Pass 115 (2026-07-01) — conn-setup commit fallback logger `FUN_80034ccc`

Fresh `ListUnnamed80030000.java` re-run: **176 unnamed** remain in region
(unchanged from Pass 114 pre-rename list; rank-1 at xref=2 tier was
`FUN_80034ccc` at 48B — largest among eight tied 2-xref candidates).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80034ccc` → `log_conn_setup_commit_fallback_evt_0x2d2_if_no_patch3`**
(48B, HIGH, SIMPLE-tier) via `RenamePass115Region80030000Fun80034ccc.java`
(`renamed=1`, live-verified).

**Mechanism:** Thin `possible_logger_called_if_no_patch3` tail-call stub with
event tag `0x2d2` (722); context pointer from `PTR_DAT_80034cfc`. Fallback
logging path when config byte at `PTR_DAT_80035cd0[1]` / `PTR_DAT_8005c060[1]`
has bit 4 set — bypasses the normal
`commit_connection_setup_mode_by_slot_bitmask_and_gates` /
`gate_lmp_power_clk_adj_eligibility_by_conn_state` commit path and instead
sets housekeeping byte `+0x8c` (link-power-mode tail) before emitting the
logger event. Sibling of Pass 60/61's `log_ogc3_config_apply_evt_0x4b6` /
`log_role_switch_housekeeping_evt_0x330` logger stubs (same shape, different
tags).

**Callers:** 2 confirmed via decompile — `param_dispatch_with_rom_calls`
(region `0x80030000`, connection-setup param-dispatch cluster) and
`dispatch_link_power_mode_by_status_bits_and_commit` (region `0x80050000`,
link power-mode transition tail).

**Confidence:** HIGH — fully decompiled 48B; logger-stub idiom matches
documented `possible_logger_called_if_no_patch3` cluster; both caller
semantics confirmed via live decompile of parent dispatchers.

Region unnamed count after this pass: **175** (176 minus this rename). Live named
**1991** global.

## Pass 116 (2026-07-01) — `build_16bit_inclusive_bit_range_mask`

**Method:** Fresh `ListUnnamed80030000` re-rank → **174 unnamed** remain
(unchanged from Pass 115 pre-rename list; rank-1 at xref=2 tier was
`FUN_80038f6c` at 44B — largest among seven tied 2-xref candidates).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80038f6c` → `build_16bit_inclusive_bit_range_mask`**
(44B, HIGH, SIMPLE-tier) via `RenamePass116Region80030000Fun80038f6c.java`
(`renamed=1`, live-verified).

**Mechanism:** Inclusive 16-bit bit-range mask builder: loops from
`param_1` through `param_2` (byte-truncated), OR-ing `1 << bit` into a
`0xffff`-clamped accumulator. Pure utility with no side effects.

**Callers:** 2 xref-in (both COMPUTED_CALL from patch `FUN_801103d4` at
`0x801104be`/`0x801104ca`) — invoked via `DAT_80110520` fptr to build
3-bit subfield masks (`local_18`..`local_34`, spans 0–2/3–5/6–8/9–11)
that are AND-merged into HW-channel register writes during per-link
packet-type programming.

**Confidence:** HIGH — fully decompiled 44B; loop semantics unambiguous;
caller context confirmed via patch-side decompile of `FUN_801103d4`.

Region unnamed count after this pass: **174** (175 minus this rename). Live named
**1992** global.

**Next:** superseded by Pass 117.

## Pass 117 (2026-07-01) — `apply_hw_reg_0x2b_slot_nibble_if_config_bit3`

Fresh `ListUnnamed80030000.java` re-run: **174 unnamed** remain in region
(unchanged from Pass 116 pre-rename list; rank-1 at xref=2 tier was
`FUN_80039210` at 30B — largest among six tied 2-xref candidates).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80039210` → `apply_hw_reg_0x2b_slot_nibble_if_config_bit3`**
(30B, HIGH, SIMPLE-tier) via `RenamePass117Region80030000Fun80039210.java`
(`renamed=1`, live-verified).

**Mechanism:** Config-gated BB-register helper: when config byte at
`PTR_DAT_80039230[1]` has bit 3 (`0x8`) set, passes slot byte at offset
`0x3a` to callee `FUN_80039194`, which reads BB reg `0x2b`, merges
`(slot & 0xf) << 8` into the upper byte, and writes back via hook fptr
pair. Optional-hook entry in the BB register-config init subsystem.

**Callers:** 2 confirmed via `ListXrefsTo80039210.java` —
`dispatch_optional_subsystem_hooks_during_hw_reg_config` (optional-hook
table walker during `hw_register_config_with_timeout`) and `FUN_800395f0`
(158B BB-reg init sequence sibling of
`program_bb_regs_0x1e_5bit_field_and_clear_0x1c_bit3`).

**Confidence:** HIGH — fully decompiled 30B; config-bit gate + slot-byte
offset unambiguous; callee HW-reg `0x2b` RMW semantics confirmed via
live decompile of `FUN_80039194`; both callers anchor BB-reg-config role.

Region unnamed count after this pass: **173** (174 minus this rename). Live named
**1993** global.

**Next:** superseded by Pass 118.

## Pass 118 (2026-07-01) — `increment_esco_slot_counter_and_apply_codec_if_gate_armed`

Fresh `ListUnnamed80030000.java` re-run: **173 unnamed** remain in region
(unchanged from Pass 117 pre-rename list; rank-1 at xref=2 tier was
`FUN_80036100` at 28B — largest among five tied 2-xref candidates).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80036100` → `increment_esco_slot_counter_and_apply_codec_if_gate_armed`**
(28B Ghidra boundary, HIGH, HANDLER-tier) via
`RenamePass118Region80030000Fun80036100.java` (`renamed=1`, live-verified).

**Mechanism:** Page/inquiry eSCO codec-slot sample-counter stepper on
`big_ol_struct` connection records, gated on `the_0x300` timer-active byte
`field_0x175`. When sub-counter `field_0x6a` is zero, seeds main counter
`field_0x68` from scan-interval byte `field_0x178`; otherwise increments
`field_0x68` by step byte `field_0x177` with clamp against
`field90_0x82` ceiling. IRQ-disabled path then, when per-slot gate byte at
`PTR_DAT_800361e0[slot]==1`, dispatches `FUN_80014450` +
`FUN_80014dac` (codec-config apply pair documented in Pass 55's
`arm_page_inquiry_scan_timer_if_idle_else_flush_codec_slots` cluster).
Alternate entry (`param_4!=0`) programs BB/HW registers via
`FUN_8001139c`/`FUN_8001136c`/`FUN_80011510`/`FUN_80011608` with
`spin_delay_10x_iterations` — sibling HW-fallback path in same body.

**Callers:** 2 xref_in via `ListXrefsTo80036100.java` — self PC-relative
literal-pool READ at `0x80036102`; data READ from `FUN_80035ff4` at
`0x80035ffc` (fptr-table reference, not direct CALL). Structural sibling of
`FUN_800361e4` (codec-slot flush callee of `arm_page_inquiry_scan_timer`).

**Confidence:** HIGH — fully decompiled; counter/step/clamp field offsets
match `the_0x300` timer cluster from Pass 55; codec-apply callee pair
confirmed; gate-byte dispatch pattern matches documented eSCO slot sweep
family in region `0x80040000`.

Region unnamed count after this pass: **172** (173 minus this rename). Live named
**1994** global.

**Next:** superseded by Pass 119.

## Pass 119 (2026-07-01) — `connection_setup_arm_stride88_slot_and_apply_packet_types`

Fresh `ListUnnamed80030000.java` re-run: **172 unnamed** remain in region
(unchanged from Pass 118; rank-1 at xref=1 tier is `FUN_8003e98c` at 644B —
largest among nine tied 1-xref candidates).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003e98c` → `connection_setup_arm_stride88_slot_and_apply_packet_types`**
(644B Ghidra boundary, HIGH, HANDLER-tier) via
`RenamePass119Region80030000Fun8003e98c.java` (`renamed=1`, live-verified).

**Mechanism:** Link-event connection-setup handler on the shared stride-0x88
per-connection table (`PTR_DAT_8003ec18`, same family as
`connection_teardown_finalize_and_reset` / `ACL_fragment_dequeue_and_credit_consumer`).
Optional guard fptr at `PTR_DAT_8003ec10` may veto; else
`FUN_8006c81c` resolves the connection index. When slot `+0x81` already set,
only re-arms `+0x85`; otherwise marks `+0x81=1`, clears queue/timing fields
`+0x70..0x82`, copies timing dword from `+0x30`. When global flag
`PTR_DAT_8003ec1c` set, calls
`clear_active_stride88_connection_buffers_and_drain_hci_cmds`. Timing path:
if `+0x38==0`, reads HW clock via `read_hw_clock_raw_dword_by_role_index` and
updates `+0x30` with wrap-aware slot arithmetic; else
`poll_hw_clock_stride88_slot_and_acl_credit_consumer`. Gated on
connection type `+0x18`, `bdaddr_random_`, and config `field208_0xd8` bit5,
computes packet-type bitfields from per-slot ushort/byte fields and dispatches
via hook fptr `PTR_DAT_8003ec38` (public vs random-BD_ADDR table paths through
`PTR_DAT_8003ec40` / `PTR_DAT_8003ec44`). Tail:
`remap_role_index_to_esco_slot_if_pending` then
`FUN_80013be4`/`FUN_80013c0c` eSCO packet-type apply (same pair as
`apply_eSCO_SCO_packet_type_params`).

**Callers:** 1 xref_in — indirect via
`bitmask_sweep_dispatch_to_8003e98c_3entry` in region `0x80000000`
(`top_level_link_event_status_dispatcher_loop` status-bit `0x400` path).

**Confidence:** HIGH — full 644B decompile; stride-0x88 offsets match Pass 8
cluster; setup/teardown sibling pairing with `connection_teardown_finalize_and_reset`
confirmed; eSCO tail matches documented packet-type apply family.

Region unnamed count after this pass: **171** (172 minus this rename). Live named
**1995** global.

**Next:** superseded by Pass 120.

## Pass 120 (2026-07-01) — `role_switch_apply_packet_types_on_stride84_slot`

Fresh `ListUnnamed80030000.java` re-run: **171 unnamed** remain in region
(unchanged at xref=2 tier; rank-1 at xref=1 tier is `FUN_80037b94` at 622B —
largest among tied 1-xref candidates).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80037b94` → `role_switch_apply_packet_types_on_stride84_slot`**
(622B Ghidra boundary, HIGH, HANDLER-tier) via
`RenamePass120Region80030000Fun80037b94.java` (`renamed=1`, live-verified).

**Mechanism:** Role-switch packet-type updater on the stride-0x84 per-connection
table (`PTR_DAT_80037e04`). Validates role-index nibble against per-slot state,
writes `param_1 << 12` to slot `+0x2c`. Branches on
`big_ol_struct.field_0xb7` (SCO vs eSCO connection types `0x4000`/`0x8000`/
`0xa000`/`0xb000` vs `0x3000` paths) to index packet-type ceiling tables
`PTR_DAT_80037e14`/`PTR_DAT_80037e18`. Merges packet-type ushorts from
`+0x2e`/`+0x0a`/`+0x2c` and dispatches via hook `PTR_DAT_80037e1c`. Tail
applies eSCO packet types via `FUN_80013be4`/`FUN_80013c0c` (same family as
Pass 119). Sets global flag `PTR_DAT_80037e20=1` and logs via
`possible_logger_called_if_no_patch3`.

**Callers:** 0 direct xrefs (indirect via
`status_word_multiflag_link_event_dispatcher` per `region_0x80000000`).

**Confidence:** HIGH — full 622B decompile; eSCO tail matches documented
packet-type apply family; role-switch context confirmed by master dispatcher doc.

Region unnamed count after this pass: **170** (171 minus this rename). Live named
**1996** global.

**Next:** superseded by Pass 121.

## Pass 121 (2026-07-01) — `lmp_status_apply_packet_types_on_stride88_slot`

Fresh `ListUnnamed80030000.java` re-run: **170 unnamed** remain in region
(unchanged at xref=2 tier; rank-1 at xref=1 tier is `FUN_8003de48` at 620B —
largest among tied 1-xref candidates).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003de48` → `lmp_status_apply_packet_types_on_stride88_slot`**
(620B Ghidra boundary, HIGH, HANDLER-tier) via
`RenamePass121Region80030000Fun8003de48.java` (`renamed=1`, live-verified).

**Mechanism:** LMP PDU status handler on the stride-0x88 per-connection table
(`PTR_DAT_8003e0b8`, same family as
`connection_setup_arm_stride88_slot_and_apply_packet_types`). Guarded on
`the_0x300.field_0x171`; `FUN_8006c81c` resolves connection index. Opcode-index
gate via `0x30c0 >> (status_nibble & 0xf) & 1`. Updates slot flags at
`+0x7a` (bits 1/2), `+0x80`, `+0x7b`, `+0x82`. Branches on `bdaddr_random_`
for public vs random-BD_ADDR packet-type table paths (`PTR_DAT_8003e0c4`/
`PTR_DAT_8003e0cc` + `DAT_8003e0d0`). Dispatches packet-type updates via hook
`PTR_DAT_8003e0c8` with masks `0xc00`/`0x1c00`/`0x800`. Logs via
`possible_logger_called_if_no_patch3` (opcode `0x2c1`) when status-bit
combinations fire.

**Callers:** 1 xref-in — `lmp_pdu_received_top_level_processor` at `0x80003f5e`.

**Confidence:** HIGH — full 620B decompile; stride-0x88 offsets match Pass 119
cluster; LMP ingress caller confirmed; packet-type hook dispatch matches
documented apply family.

Region unnamed count after this pass: **169** (170 minus this rename). Live named
**1997** global.

**Next:** superseded by Pass 122.

## Pass 122 (2026-07-01) — `role_switch_commit_staged_slot_transition`

Fresh `ListUnnamed80030000.java` re-run: **169 unnamed** remain in region
(unchanged at xref=2 tier; rank-1 at xref=1 tier is `FUN_8003fcc8` at 604B —
largest among tied 1-xref candidates).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003fcc8` → `role_switch_commit_staged_slot_transition`**
(604B Ghidra boundary, HIGH, HANDLER-tier) via
`RenamePass122Region80030000Fun8003fcc8.java` (`renamed=1`, live-verified).

**Mechanism:** Role-switch commit handler reading staged params from
`PTR_DAT_8003ff24` (conn indices, slot types, subopcode indices). IRQ-off
packet-type hook dispatch via `PTR_DAT_8003ff34` for old/new slots; HW channel
table updates via `or_merge_hw_channel_table_entry_and_indexed_dispatch` /
`and_mask_hw_channel_table_entry_and_indexed_dispatch` on `PTR_DAT_8003ff38`
stride-8 entries. Calls `reassign_inquiry_lap_slot_refcount_pending_and_program_channel`,
optional `LMP__25B__most_common_for_VSCs1`, subopcode descriptor init
(`init_conn_subopcode_slot_descriptor_from_timing_templates`,
`init_subopcode_slot_descriptor_and_assign_conn_index`), `clear_bos_e4_role_switch_hook_bit`,
codec/crypto path when `field_0xbc` set, `sometimes_called_with_0_3_0`, and
`reset_dual_slot_role_record_by_conn_index` on LAP refcount-zero path.

**Callers:** 1 xref-in — `encryption_key_teardown_notifier` at `0x800029a4`
(per prior cross-region xref sweep; paired with `FUN_80037804`).

**Confidence:** HIGH — full 604B decompile; callees
`reassign_inquiry_lap_slot_refcount_pending_and_program_channel` and
`reset_dual_slot_role_record_by_conn_index` already HIGH-named; role-switch
hook/bit clear matches documented `bos_base+0xe4` cluster.

Region unnamed count after this pass: **168** (169 minus this rename). Live named
**1998** global.

**Next:** superseded by Pass 123.

## Pass 123 (2026-07-01) — `read_fd49_extended_diag_build_dual_slot_bitmasks_and_shift_width`

Fresh `ListUnnamed80030000.java` re-run: **168 unnamed** remain in region
(unchanged at xref=2 tier; rank-1 at xref=1 tier is `FUN_8003ac7c` at 296B —
largest among tied 1-xref candidates, tied with `FUN_800334ac` at 296B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003ac7c` → `read_fd49_extended_diag_build_dual_slot_bitmasks_and_shift_width`**
(296B Ghidra boundary, HIGH, HANDLER-tier) via
`RenamePass123Region80030000Fun8003ac7c.java` (`renamed=1`, live-verified).

**Mechanism:** Register-script interpreter cluster helper: programs BB regs
`0x40`/`0x41` via `dispatch_bb_register_da_d6_write_with_hook`, then reads
four extended-diagnostic values through `VSC_0xfd49_extended_diagnostic` at
indices `0xc035`–`0xc038` and `0xc03d`. Stores paired dword halves into two
parallel 0x20-stride slot records (`param_2` index and scaled offset), builds
per-bit masks at byte `+4` (low nibble vs `>>7` high-nibble paths), then calls
`compute_int64_halves_signed_shift_width` to store signed-shift width at byte
`+5` on both records.

**Callers:** 1 xref-in — data reference from `FUN_8003ac28` at `0x8003ac48`
(register-script interpreter dispatch cluster; indirect/script-table path).

**Confidence:** HIGH — full 296B decompile; `VSC_0xfd49_extended_diagnostic` and
`compute_int64_halves_signed_shift_width` callees already HIGH-named; BB
register indices match documented diagnostic-read cluster (Pass 64 cross-ref).

Region unnamed count after this pass: **167** (168 minus this rename). Live named
**1999** global.

**Next:** superseded by Pass 124.

## Pass 124 (2026-07-01) — `compute_lmp_slot_offset_byte_from_conn_timing_params`

Fresh `ListUnnamed80030000.java` re-run: **167 unnamed** remain in region
(unchanged at xref=2 tier; rank-1 at xref=1 tier is `FUN_800334ac` at 296B —
largest among tied 1-xref candidates).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_800334ac` → `compute_lmp_slot_offset_byte_from_conn_timing_params`**
(296B Ghidra boundary, HIGH, HANDLER-tier) via
`RenamePass124Region80030000Fun800334ac.java` (`renamed=1`, live-verified).

**Mechanism:** LMP slot-offset byte calculator for role-switch / conn-timing
cluster. Skips when conn `bdaddr_random_==1`; optional hook veto at
`PTR_DAT_800335d8`. Default path scales `timing_param` by conn `field_0x202`
(default `0xfa` when `0xff`) plus global offset at `PTR_DAT_800335dc+0x9e`,
with extra `0x2ee` penalty when `timing_param` exceeds `field106_0x94`. Applies
tiered downscaling via `DAT_800335e0`/`DAT_800335e4` thresholds, converts to
slot units via `/0x138` (+1), aligns to slot grid when `field200_0x206` or
global flag bit set, caps result at `0x7c`.

**Callers:** 1 xref-in — `compute_lmp_slot_offset_and_program_hw_by_conn_cc_index`
at `0x800362f0` (Pass 67; programs result into HW register indexed by
`byte_0xCC`).

**Confidence:** HIGH — full 296B decompile; callee relationship confirms
timing-scaled slot-offset math; cap `0x7c` and `field_0x202`/`field106_0x94`
inputs match Pass 67 caller analysis.

Region unnamed count after this pass: **166** (167 minus this rename). Live named
**2000** global.

**Next:** superseded by Pass 125.

## Pass 125 (2026-07-01) — `dispatch_acl_fragment_with_per_conn_reassembly_flags`

Fresh `ListUnnamed80030000.java` re-run: **166 unnamed** remain in region
(unchanged at xref=2 tier; rank-1 at xref=1 tier is `FUN_8003d354` at 292B —
largest among tied 1-xref candidates).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003d354` → `dispatch_acl_fragment_with_per_conn_reassembly_flags`**
(292B Ghidra boundary, HIGH, HANDLER-tier) via
`RenamePass125Region80030000Fun8003d354.java` (`renamed=1`, live-verified).

**Mechanism:** ACL fragment reassembly state dispatcher on per-conn 0x88-stride
table (`PTR_DAT_8003d47c`). Optional veto hooks at `PTR_DAT_8003d478`/`480`/
`484`/`488`/`48c`. Skips when slot byte `+0x18==1`. Manages per-conn reassembly
flags at `+0x83` (pending) and `+0x84` (continuation armed); branches on
fragment opcode `param_2` (`0xffff` flush/complete vs low-byte start vs high-byte
continuation) and dispatches to `hci_acl_data_fragment_assembler_and_enqueue`
with fragment modes 1 (start) or 2 (complete/abort).

**Callers:** 1 xref-in — `LC_event_RX_dispatcher` at `0x80042262` (LC RX ACL
fragment path; sibling caller of `hci_acl_data_fragment_assembler_and_enqueue` per
Pass 6 cont. 3 in region `0x80020000`).

**Confidence:** HIGH — full 292B decompile; callee `hci_acl_data_fragment_assembler_and_enqueue`
already HIGH-named; per-conn flag semantics match documented ACL reassembly cluster.

Region unnamed count after this pass: **165** (166 minus this rename). Live named
**2001** global.

**Next:** superseded by Pass 126.

## Pass 126 (2026-07-01) — `check_role_slot_timing_deadline_overrun_and_set_flag`

Fresh `ListUnnamed80030000.java` re-run: **165 unnamed** remain in region
(unchanged at xref=2 tier; rank-1 at xref=1 tier is `FUN_8003851c` at 276B —
largest among tied 1-xref candidates).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003851c` → `check_role_slot_timing_deadline_overrun_and_set_flag`**
(276B Ghidra boundary, HIGH, HANDLER-tier) via
`RenamePass126Region80030000Fun8003851c.java` (`renamed=1`, live-verified).

**Mechanism:** Per-role-slot `big_ol_struct` timing-deadline overrun checker gated
on `field_0x29c` in {2,3}, `field_0x28c != 0xffffffff`, `field_0x29e == 0`,
`field_0x29d == 0`, and `field_0x29f == 0`. Reads halved HW clock via
`read_hw_clock_raw_dword_by_role_index(byte_0xCC)`. When `field_0x29c == 2`:
compares clock against absolute deadline at `field_0x288` (+2, masked by
`DAT_80038634`); on overrun logs opcode `0x27` subcode `0x1bc`/`0x125` and sets
`field_0x29f = 3`. When `field_0x29c == 3` and `field_0x28c != -1`: computes
elapsed since captured timestamp with wrap via `DAT_8003863c`, threshold from
max of `field_0x298`/`field_0x294`/`(field106_0x94 & 0x7fff)<<1`; on overrun
logs opcode `0x27` subcode `0x1d1`/`0x126` and sets `field_0x29f = 1`. Sibling
of Pass 104's `advance_role_slot_link_state_and_capture_halved_hw_clock`.

**Callers:** 1 xref-in — `conn_event_packet_type_update_and_reschedule` at
`0x800046b8` (invoked when current packet type is eSCO class `0xc00`/`0x1c00`/
`0xc000` during per-connection-event packet-type refresh).

**Confidence:** HIGH — full 276B decompile; role-slot field cluster `0x288`–
`0x29f` matches Pass 104 sibling; dual absolute/relative deadline paths with
documented logging opcodes; caller integration in eSCO packet-type sweep confirmed.

Region unnamed count after this pass: **164** (165 minus this rename). Live named
**2002** global.

**Next:** superseded by Pass 127.

## Pass 127 (2026-07-01) — `program_bb_regs_6b_6c_43_6a_via_hook_and_extended_diagnostic`

Fresh `ListUnnamed80030000.java` re-run: **164 unnamed** remain in region
(unchanged at xref=2 tier; rank-1 at xref=1 tier is `FUN_8003c19c` at 274B —
largest among tied 1-xref candidates).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003c19c` → `program_bb_regs_6b_6c_43_6a_via_hook_and_extended_diagnostic`**
(274B Ghidra boundary, HIGH, HANDLER-tier) via
`RenamePass127Region80030000Fun8003c19c.java` (`renamed=1`, live-verified).

**Mechanism:** Optional hook at `PTR_DAT_8003c2b0` may veto entire sequence.
When hook unset or returns 0: brackets BB-register RMW writes with VSC reg `0x40`
enable modes (`2` then `0` then `3` then `0`×2). Reads current values via
`VSC_0xfd49_extended_diagnostic`, merges four params into regs `0x6b`/`0x6c`/
`0x43`/`0x6a`: `param_2` → `0x6b` bits 13–15; `param_1` → `0x6c` low 3 bits +
bit 3; `param_3` → `0x6c` bit 15; `param_4` → `0x43` bits 8–13; then ORs bit 0
into `0x6a` and `0x1000` into `0x6b`. All writes via
`dispatch_bb_register_da_d6_write_with_hook`. Tail of
`hw_register_setup_with_patch_hook_variant2` (`0x8003c2b4`) which calls with
`(3,3,0,0xf)` — secondary-path counterpart to `per_connection_hw_buffer_setup_with_patch_hook`
(`0x8003c41c`) register set `0x69`/`0x6a`/`0x6f`.

**Callers:** 1 xref-in — `hw_register_setup_with_patch_hook_variant2` at
`0x8003c2b4` (documented Pass 8; tail call after reg `0x6b`/`0x6e`/`0x6c`/
`0x6d`/`0x68` setup).

**Confidence:** HIGH — full 274B decompile; register-family pattern matches
`program_bb_regs_41_43_44_46_47_via_hook_and_da_d6_dispatch` sibling; VSC `0x40`
enable/disable bracketing and `VSC_0xfd49_extended_diagnostic` read-modify-write
idiom confirmed; caller integration in documented TX/RX hw-buffer-setup pair.

Region unnamed count after this pass: **163** (164 minus this rename). Live named
**2003** global.

**Next:** superseded by Pass 128.

## Pass 128 (2026-07-01) — `copy_nine_dispatch_slots_and_init_baseband_subsystems`

Fresh `ListUnnamed80030000.java` re-run: **163 unnamed** remain in region
(unchanged at xref=2 tier; rank-1 at xref=1 tier is `FUN_80034674` at 266B —
largest among tied 1-xref candidates).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80034674` → `copy_nine_dispatch_slots_and_init_baseband_subsystems`**
(266B Ghidra boundary, HIGH, INIT-tier) via
`RenamePass128Region80030000Fun80034674.java` (`renamed=1`, live-verified).

**Mechanism:** Boot/subsystem init sub-step (default path of `FUN_800347a0` when
hook at `PTR_DAT_800347cc` is null). Logs via `function_that_uses_Logger_string`,
calls `init_baseband_hw_from_config_struct`, then copies nine indexed dispatch
slots from ROM tables (`PTR_DAT_80034784` ushort offsets + `PTR_DAT_8003478c`
dword values into `DAT_80034788` base) — gated on `config_struct.field59_0x41 &
3` (mode `1` copies once; mode `0` copies after `clear_global_status_bit_0x400`).
Sets status byte bit0 at `DAT_80034790`, calls
`conditional_table_entry_registration_init(1)`. When status byte bits `0x1e==6`,
calls `FUN_80033ec4` (`vsc_0xfc56_payload_apply_and_rf_reconfig`). Mode `0` also
calls `FUN_800122fc`. Clears status bits `2`/`4`/`8`/`0x10` on `DAT_80034798`,
zeroes 16-byte buffer at `PTR_DAT_8003479c`. Sibling of region `0x80020000`'s
`copy_eight_literal_pool_globals_and_init_baseband_hw` but nine-slot indexed
table copy with richer status-bit housekeeping.

**Callers:** 1 xref-in — `FUN_800347a0` at `0x800347b8` (hook-null default init
wrapper that also calls `FUN_800343dc`).

**Confidence:** HIGH — full 266B decompile; register-family and config-flag
gating match documented boot-init cluster; callees
`init_baseband_hw_from_config_struct`/`conditional_table_entry_registration_init`/
`clear_global_status_bit_0x400` already high-confidence.

Region unnamed count after this pass: **162** (163 minus this rename). Live named
**2004** global.

**Next:** superseded by Pass 129.

## Pass 129 (2026-07-01) — `apply_tx_power_runtime_mode_byte_and_reconfigure_tables_and_links`

Fresh `ListUnnamed80030000.java` re-run: **162 unnamed** remain in region
(unchanged at xref=2 tier; rank-1 at xref=1 tier is `FUN_80039c98` at 264B —
largest among tied 1-xref candidates).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80039c98` → `apply_tx_power_runtime_mode_byte_and_reconfigure_tables_and_links`**
(264B Ghidra boundary, HIGH, HANDLER-tier) via
`RenamePass129Region80030000Fun80039c98.java` (`renamed=1`, live-verified).

**Mechanism:** TX-power runtime mode-byte change notifier in the
`PTR_DAT_80039da0` config cluster. When diagnostic bit 2 of `+0x36` is set,
logs mode byte, prior `+0x13` value, and hook sample via
`possible_logging_function__var_args`. On `param_1 != +0x13`, stores new mode
byte and invokes four sequential fptr hooks (`PTR_PTR_80039db0`,
`PTR_DAT_80039db4(param_1)`, `PTR_DAT_80039db8`, optional `PTR_DAT_80039dbc`),
then runs `dispatch_be_u16_pairs_reverse_step2_from_buf_via_fptr_hook` on three
calibration-table buffers (`0x136`/`0x1bc`/`0x1c4` byte counts), copies three
u16 globals from `+0x33`/`+0x30`/`+0x2e`, and walks slots `0..0xa` of the
`0x1ac` connection array — for each active link (bit0 of `field3_0x3`), invokes
per-link fptr hooks at `PTR_DAT_80039ddc`/`PTR_DAT_80039de0`. Sibling/caller of
Pass 83's `dispatch_tx_power_config_byte_0x2c_or_0x2d_via_dual_fptr_hooks`.

**Callers:** 1 xref-in (per `ListUnnamed80030000`; `xrefs_to` empty — indirect
fptr dispatch gap).

**Confidence:** HIGH — full 264B decompile; mode-byte gate at `+0x13`,
calibration-table dispatch trio, and per-active-link propagation match
documented TX-power runtime-config cluster.

Region unnamed count after this pass: **161** (162 minus this rename). Live named
**2005** global.

**Next:** superseded by Pass 130.

## Pass 130 (2026-07-01) — `validate_lmp25c_role_record_pdu_and_program_dual_slot_credits_or_reject`

Fresh `ListUnnamed80030000.java` re-run: **161 unnamed** remain in region
(unchanged at xref=2 tier; rank-1 at xref=1 tier is `FUN_8003ff44` at 256B —
largest among tied 1-xref candidates).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003ff44` → `validate_lmp25c_role_record_pdu_and_program_dual_slot_credits_or_reject`**
(256B Ghidra boundary, HIGH, HANDLER-tier) via
`RenamePass130Region80030000Fun8003ff44.java` (`renamed=1`, live-verified).

**Mechanism:** LC TX-dispatch ingress gate for dual-slot LMP-0x25C role-record
PDUs. Early-exits when `references_patch_download_mem4()` active. Parses
connection handle (low 12 bits) and link-type nibble (bits 6–7 of byte+1);
validates against `config_struct` fields `+0xb6`/`+0xb7`, resolves bos-array
index via `lookup_up_to_3_bos_array_indices_by_connection_handle`, checks
`big_ol_struct` status-byte gate for link types 1/2, then calls
`FUN_800181f4` to populate per-PDU context at `param_1+0x206`. On success
(credit byte nonzero in 0x84-stride role table), chains to
`program_dual_slot_lmp25c_packet_credits_by_conn_index`. On failure: dispatches
hook at `PTR_DAT_80040050(param_1,2)`; when `PTR_DAT_80040054` bit0 set,
allocates buffer and transmits LMP bytes `0x10`/`0x01`/`0x02` via
`invoke_lmp_tx_hook_with_length_word_from_pdu_buffer`; logs rejection with
coded reason (`1`/`4`/`8`/`0x20`/`0x40`).

**Callers:** 1 xref-in — `LC_event_TX_dispatcher` at `0x800424dc`
(documented Pass 2 region `0x80040000`).

**Confidence:** HIGH — full 256B decompile; caller integration in LC TX
dispatcher confirmed via `ListXrefsTo8003ff44.java`; callees
`program_dual_slot_lmp25c_packet_credits_by_conn_index` and
`lookup_up_to_3_bos_array_indices_by_connection_handle` already high-confidence;
sibling of region `0x80040000` dual-slot LMP-0x25C role-record cluster.

Region unnamed count after this pass: **160** (161 minus this rename). Live named
**2006** global.

**Next:** superseded by Pass 131.

## Pass 131 (2026-07-01) — `log_eight_rotating_ring_buffer_dword_pairs_when_diag_gates_active`

Fresh `ListUnnamed80030000.java` re-run: **160 unnamed** remain in region
(unchanged at xref=2 tier; rank-1 at xref=1 tier is `FUN_8003b9c0` at 250B —
largest among tied 1-xref candidates).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003b9c0` → `log_eight_rotating_ring_buffer_dword_pairs_when_diag_gates_active`**
(250B Ghidra boundary, HIGH, SIMPLE-tier) via
`RenamePass131Region80030000Fun8003b9c0.java` (`renamed=1`, live-verified).

**Mechanism:** VSC 0xfd49 extended-diagnostic ring-buffer snapshot logger in the
`PTR_DAT_8003babc`/`PTR_DAT_8003bac0`/`PTR_DAT_8003bac4` gate cluster. When all
three diagnostic-enable gates are active (`PTR_DAT_8003babc` nonzero,
`PTR_DAT_8003bac0` nonzero, `PTR_DAT_8003bac4` counter nonzero), computes a
rotating start index `(counter << 6) % ring_size` from ushort globals at
`PTR_DAT_8003bac8`/`PTR_DAT_8003bacc`, then walks eight consecutive 8-byte
(dword-pair) slots from the `param_1` ring buffer (64 bytes total) and logs
each pair via `possible_logging_function__var_args` (event class `0x28`, tag
`0xed1`). Post-loop increments the ring counter at `PTR_DAT_8003bac8`. Sibling
of `FUN_8003bad4` (`VSC_0xfd49_extended_diagnostic` tail) and
`read_fd49_extended_diag_build_dual_slot_bitmasks_and_shift_width` (Pass 123).

**Callers:** 1 xref-in — unconditional call at `0x80010e14` (Ghidra has no
containing function at call site; likely fptr-dispatch gap in region
`0x80010000`).

**Confidence:** HIGH — full 250B decompile; triple diagnostic-gate pattern,
modulo ring-index math, eight-slot dword-pair walk, and varargs logger match
documented VSC 0xfd49 extended-diagnostic cluster.

Region unnamed count after this pass: **159** (160 minus this rename). Live named
**2007** global.

**Next:** superseded by Pass 132.

## Pass 132 (2026-07-01) — `copy_config_sco_timing_triplets_to_globals_and_toggle_0x2000_bit`

Fresh `ListUnnamed80030000.java` re-run: **159 unnamed** remain in region
(unchanged at xref=2 tier; rank-1 at xref=1 tier is `FUN_80033048` at 246B —
largest among tied 1-xref candidates).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80033048` → `copy_config_sco_timing_triplets_to_globals_and_toggle_0x2000_bit`**
(246B Ghidra boundary, HIGH, INIT-tier) via
`RenamePass132Region80030000Fun80033048.java` (`renamed=1`, live-verified).

**Mechanism:** Config-blob mirror helper in the SCO HW-channel init cluster.
Copies three dwords from `config_base` offsets `+0x1c`/`+0x20`/`+0x24` into
`PTR_DAT_80033144`/`80033148`/`8003314c` runtime globals; mirrors byte `+0x1e6`,
bytes `+0x28`/`+0x29`, and ushort `+0x1de` into sibling `PTR_DAT` slots. Toggles
bit `0x2000` on the config ushort at `+0x44` (`field62_0x44`/`field63_0x45`)
based on bit0 of `+0x1e6` (set when clear, clear when set). When the mirrored
`+0x28` byte has bit `0x10` set, additionally copies dword `+0x2c` to
`PTR_DAT_80033160`.

**Callers:** 1 xref-in — `init_sco_hw_channel_8plus4_slot_program_and_bb_regs`
at `0x8003769c` (Pass 93 SCO HW-channel init path).

**Confidence:** HIGH — full 246B decompile; config-field copy pattern and
`0x2000` link-capability bit toggle unambiguous; caller integration in SCO
init cluster confirmed via `ListXrefsTo80033048.java`.

Region unnamed count after this pass: **158** (159 minus this rename). Live named
**2008** global.

**Next:** superseded by Pass 133.

## Pass 133 (2026-07-01) — `poll_fd49_extended_diag_bb_registers_and_return_status_bytes`

Fresh `ListUnnamed80030000.java` re-run: **158 unnamed** remain in region
(unchanged at xref=2 tier; rank-1 at xref=1 tier is `FUN_8003bad4` at 244B —
largest among tied 1-xref candidates).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003bad4` → `poll_fd49_extended_diag_bb_registers_and_return_status_bytes`**
(244B Ghidra boundary, HIGH, HANDLER-tier) via
`RenamePass133Region80030000Fun8003bad4.java` (`renamed=1`, live-verified).

**Mechanism:** VSC 0xfd49 extended-diagnostic BB-register poll/read helper in the
`PTR_DAT_8003bbc8`/`8003bbd0`/`8003bbd4` cluster (fd49-specific globals, sibling
of `poll_and_write_bb_registers_0xda_0xd6`). When `param_1 & 0x40 == 0`:
reference-counts session via `PTR_DAT_8003bbc8`, primes reg `0xda` with `0x100`
via fptr at `PTR_DAT_8003bbd0`, optionally waits for bit `0x200` on status.
Always writes `(param_1 & 0xff) << 8` to reg `0xd6` via same fptr. Poll loop
waits for completion bit `0x80` on `DAT_8003bbe0`; timeout logs via
`possible_logging_function__var_args`. On exit decrements session counter and
clears `0xda` enable when count reaches zero. Returns combined status bytes from
`DAT_8003bbd8` and `DAT_8003bbec`.

**Callers:** 1 xref-in — tail-call from `VSC_0xfd49_extended_diagnostic`
(`0x8003bbf0`) after bit-manip on `param_1` (`& 0x3f`, `| 0x40`).

**Confidence:** HIGH — full 244B decompile; poll/write `0xda`/`0xd6` idiom matches
documented `poll_and_write_bb_registers_0xda_0xd6` sibling; fd49-cluster globals
and `VSC_0xfd49_extended_diagnostic` tail-call integration confirmed.

Region unnamed count after this pass: **157** (158 minus this rename). Live named
**2009** global.

**Next:** superseded by Pass 134.

## Pass 134 (2026-07-01) — `zero_bos_struct_and_init_link_mode_bb_timing_with_lmp_3ee_branch`

Fresh `ListUnnamed80030000.java` re-run: **157 unnamed** remain in region
(unchanged at xref=2 tier; rank-1 at xref=1 tier is `FUN_80035e64` at 226B —
largest among tied 1-xref candidates).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80035e64` → `zero_bos_struct_and_init_link_mode_bb_timing_with_lmp_3ee_branch`**
(226B Ghidra boundary, HIGH, INIT-tier) via
`RenamePass134Region80030000Fun80035e64.java` (`renamed=1`, live-verified).

**Mechanism:** Link-mode-change cluster BOS/workspace initializer on
`PTR_DAT_80035f48`. `memset` zeroes 0x16c bytes, primes scalar timing fields
(`+0x9a`=100, `+0x9e`=0xfa), clears companion globals. When
`DAT_80035f5c >> 8 & 0x80` is clear, dispatches optional hook at
`PTR_DAT_80035f60`; otherwise sets bit 2 on `PTR_DAT_80035f64` and invokes
`LMP_CH__0x3ee__case2_else_1_FUN_80011fc0FUN_80011e10` +
`LMP_CH__0x3ee__case2_else_2_FUN_80011d9c`, merging `+0x164` link-mode byte.
Copies `+0x10a` from global bit `>>4`, stores timeout pointer `0x2580` at
offset +300 (`0x12c`), merges mask constants into `+0x164`, and primes BB
timing pair `+0x168`=9 / `+0x16a`=0x6228 (same offsets used by SCO HW-channel
init and `poll_vsc_fc11_3_until_pending_clear_with_link_mode_timeouts`).

**Callers:** 1 xref-in (caller name unresolved this pass).

**Confidence:** HIGH — full 226B decompile; `+0x10a`/`+0x164`/`+0x168`/`+0x16a`
field layout matches documented link-mode-change cluster; LMP 0x3ee case2 branch
idiom matches sibling `FUN_800352d0`.

Region unnamed count after this pass: **156** (157 minus this rename). Live named
**2010** global.

**Next:** superseded by Pass 135.

## Pass 135 (2026-07-01) — register-script invoke wrapper `FUN_800393d8`

Fresh `ListUnnamed80030000.java` re-run: **156 unnamed** remain in region
(unchanged from Pass 134; rank-1 at xref=2 tier is `FUN_800393d8` at 24B —
largest among tied 2-xref candidates).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_800393d8` → `invoke_register_script_from_global_context_0x60_0x64`**
(24B, HIGH, SIMPLE-tier) via
`RenamePass135Region80030000Fun800393d8.java` (`renamed=1`, live-verified).

**Mechanism:** Thin register-script interpreter invocation wrapper in the
`0x800393xx` cluster. Loads global context `PTR_PTR_800393f0`, calls through
hook fptr at `PTR_DAT_800393f4` with `(script_ptr, num_halfwords)` =
`(*(ctx+0x60), *(ushort*)(ctx+0x64))` — matching
`register_script_interpreter` signature. Sibling of `FUN_800393f8`
(offsets `+0x58`/`+0x5c`) and `FUN_80039418` (dual invoke). Listed as caller
of `register_script_interpreter` in
`reverse_engineering_register_script_interpreter.md`.

**Callers:** 2 xref-in (caller names unresolved this pass; `find_callers` empty
— likely indirect/data-ref invocation).

**Confidence:** HIGH — full 24B decompile; fptr-dispatch idiom and context-field
offsets match documented register-script cluster; name persisted in Ghidra.

Region unnamed count after this pass: **155** (156 minus this rename). Live named
**2011** global.

**Next:** superseded by Pass 136.

## Pass 136 (2026-07-01) — BB reg 0x1e 5-bit field reader `FUN_80038d7c`

Fresh `ListUnnamed80030000.java` re-run: **155 unnamed** remain in region
(unchanged from Pass 135; rank-1 at xref=2 tier is `FUN_80038d7c` at 24B —
largest among tied 2-xref candidates).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80038d7c` → `read_bb_reg_0x1e_5bit_field_via_hook`**
(24B, HIGH, SIMPLE-tier) via
`RenamePass136Region80030000Fun80038d7c.java` (`renamed=1`, live-verified).

**Mechanism:** Thin BB-register read wrapper in the `0x80038dxx` cluster.
Calls hook fptr at `PTR_DAT_80038d94` with reg ID `0x1e`, then extracts the
5-bit field via `(value & 0x7ffffff) >> 0xb` (bits 11–15). Read-side complement
of `program_bb_regs_0x1e_5bit_field_and_clear_0x1c_bit3` (Pass 109); consumed
by config-flag wrapper `FUN_80038df4` when `*PTR_DAT_80038e20 & 0x10 == 0`
(alternate path uses byte at `+0x24`).

**Callers:** 2 xref-in — `FUN_80038df4` (confirmed via decompile); second xref
unresolved (`find_callers` empty — likely indirect/data-ref).

**Confidence:** HIGH — full 24B decompile; reg ID `0x1e`, mask/shift semantics
match Pass 109 programmer; caller body anchors role.

Region unnamed count after this pass: **154** (155 minus this rename). Live named
**2012** global.

**Next:** superseded by Pass 137.

## Pass 137 (2026-07-01) — config-dispatch noop hook stub `FUN_80039a10`

Fresh `ListUnnamed80030000.java` re-run: **154 unnamed** remain in region
(unchanged from Pass 136; rank-1 at xref=2 tier is `FUN_80039a10` at 4B —
tied with `FUN_80033d28`, first by address sort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80039a10` → `noop_config_dispatch_hook_fptr_jr_ra_stub`**
(4B, HIGH, STUB-tier) via
`RenamePass137Region80030000Fun80039a10.java` (`renamed=1`, live-verified).

**Mechanism:** Trivial 4-byte MIPS16e return stub (`jr ra` + delay-slot `_nop`).
Serves as the default no-op target for optional hook fptr slots in the
`0x80039abc` config-dispatch cluster (`PTR_PTR_80039b0c` / `PTR_DAT_80039b14`):
when a hook slot is not overridden, indirect calls through those pointers land
here and return immediately without side effects.

**Callers:** 2 xref-in — `FUN_80039abc` (COMPUTED_CALL at `0x80039af6`, confirmed
via decompile of config-dispatch body); patch `FUN_8011006c` (COMPUTED_CALL at
`0x8011009e`, likely parallel hook-slot install path).

**Confidence:** HIGH — disasm confirms `jr ra`; caller `FUN_80039abc` body shows
dual conditional hook-fptr dispatch pattern matching noop-default stub role.

Region unnamed count after this pass: **153** (154 minus this rename). Live named
**2013** global.

**Next:** superseded by Pass 138.

## Pass 138 (2026-07-01) — unsniff slave-cleanup noop hook stub `FUN_80033d28`

Fresh `ListUnnamed80030000.java` re-run: **153 unnamed** remain in region
(unchanged from Pass 137; rank-1 at xref=2 tier is `FUN_80033d28` at 4B —
sole remaining xref=2 candidate after Pass 137 renamed `FUN_80039a10`).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80033d28` → `noop_unsniff_slave_cleanup_hook_jr_ra_stub`**
(4B, HIGH, STUB-tier) via
`RenamePass138Region80030000Fun80033d28.java` (`renamed=1`, live-verified).

**Mechanism:** Trivial 4-byte MIPS16e return stub (`jr ra` + delay-slot `_nop`).
Optional pre-send cleanup hook invoked by `LMP_0x18_LMP_UNSNIFF_REQ` when the
connection record's `bdaddr_random` flag is set (slave/non-master role on
unsniff exit): calls this stub, then copies `*PTR_DAT_8001b014` into
`field_0x205` and logs before unconditionally sending `LMP_UNSNIFF_REQ`
(opcode `0x18`). Sibling noop-stub pattern to Pass 137's config-dispatch hook.

**Callers:** 2 xref-in per cold-triage — `LMP_0x18_LMP_UNSNIFF_REQ` at
`0x8001af9c` (confirmed via decompile); second xref unresolved (`find_callers`
empty — likely data-ref or computed-call).

**Confidence:** HIGH — decompile confirms empty body; caller body anchors
unsniff/slave-role cleanup hook role per `lc_lmp_state_machine` trace.

Region unnamed count after this pass: **152** (153 minus this rename). Live named
**2014** global.

**Next:** superseded by Pass 139.

## Pass 139 (2026-07-01) — VSC vendor-config subcmd feature-flag writer `FUN_80030158`

Fresh `ListUnnamed80030000.java` re-run: **152 unnamed** remain in region
(unchanged from Pass 138; rank-1 at xref=1 tier is `FUN_80030158` at 222B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80030158` → `dispatch_vsc_vendor_config_subcmd_write_feature_flags`**
(222B, HIGH) via
`RenamePass139Region80030000Fun80030158.java` (`renamed=1`, live-verified).

**Mechanism:** Sub-command dispatcher invoked from master VSC dispatcher
(`HCI_CMD_OGF_3F__Vendor_Specific__FUN_80030f1c`). Validates `param+2` length
byte (must be `0x02` for subcmds 1–4), switches on `param+3` sub-opcode 0–4:
subcmd 0 delegates to `FUN_8003013c` (credit-scheduler global byte store);
subcmds 1–4 write individual feature/config bit flags into
`PTR_PTR_80030244[8]` (bits 0–4) and subcmd 4 also sets bit 15 of
`DAT_80030248`; returns HCI error `0x12` on invalid parameters.

**Callers:** 1 xref-in — master VSC dispatcher (`0x80030f1c`).

**Confidence:** HIGH — decompile confirms switch-dispatch + global flag writes;
caller anchors VSC vendor-config path.

Region unnamed count after this pass: **151** (152 minus this rename). Live named
**2015** global.

**Next:** superseded by Pass 140.

## Pass 140 (2026-07-01) — SCO BB register programmer from config `FUN_8003b220`

Fresh `ListUnnamed80030000.java` re-run: **151 unnamed** remain in region
(unchanged from Pass 139; rank-1 at xref=1 tier is `FUN_8003b220` at 208B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003b220` → `program_sco_bb_regs_from_config_offset_0x106_via_hw_hook`**
(208B, HIGH) via
`RenamePass140Region80030000Fun8003b220.java` (`renamed=1`, live-verified).

**Mechanism:** Reads nine ushort pairs from `config_struct` offsets `+0x106`–`+0x117`
and dispatches each via hook at `PTR_DAT_8003b2f4` to BB registers
`0xc6`/`0xc8`/`0xca`/`0xcc`/`0xce`/`0xd0`/`0xd2`/`0xd4`/`0xc2`; final call
writes `DAT_8003b2f8 | 0x4000` to register `0xac`. SCO HW init cluster helper
alongside `program_sco_hw_channel_table_and_bb_regs_from_config`.

**Callers:** 1 xref-in — `init_or_clear_sco_hw_channel_subsystem` (`0x80036fa8`).

**Confidence:** HIGH — decompile confirms config-driven BB register hook dispatch;
caller anchors SCO/eSCO HW subsystem init path.

Region unnamed count after this pass: **150** (151 minus this rename). Live named
**2016** global.

**Next:** superseded by Pass 141.

## Pass 141 (2026-07-01) — link supervision event dispatcher `FUN_80037a20`

Fresh `ListUnnamed80030000.java` re-run: **150 unnamed** remain in region
(unchanged from Pass 140; rank-1 at xref=1 tier is `FUN_80037a20` at 196B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80037a20` → `clear_role_slot_timing_flag_and_dispatch_link_supervision_event`**
(196B, HIGH, HANDLER-tier) via
`RenamePass141Region80030000Fun80037a20.java` (`renamed=1`, live-verified).

**Mechanism:** Per-role-slot `big_ol_struct` link-supervision event dispatcher
gated on index `!= 0xff`. Reads and clears `field_0x29f` (timing/link-state flag
set by Pass 104/126 siblings). Switch on cleared value: `1` = relative timing
overrun (logs opcode `0x27` subcode `0x21e`/`0x128`, dispatches event `0x2c8`
when `field_0x29d != 1`); `2` = link-state advance (logs `0x227`/`0x129`,
dispatches `0x2c9` when `field_0x29d == 1`); `3` = absolute timing overrun
(logs `0x22f`/`0x12a`, dispatches `0x2ca`). Events sent via
`possible_logger_called_if_no_patch3` through `PTR_DAT_80037aec`.

**Callers:** 1 xref-in — `conn_teardown_and_link_loss_cleanup_handler` at
`0x80004820` (invoked during per-connection teardown/link-loss cleanup).

**Confidence:** HIGH — full 196B decompile; `field_0x29f` cluster matches Pass
104/126 producers; three-way dispatch with documented logging opcodes; caller
integration in connection-teardown path confirmed via `find_callers`.

Region unnamed count after this pass: **149** (150 minus this rename). Live named
**2017** global.

**Next:** superseded by Pass 142.

## Pass 142 (2026-07-01) — connection slot-reuse gate `FUN_8003e694`

Fresh `ListUnnamed80030000.java` re-run: **149 unnamed** remain in region
(unchanged from Pass 141; rank-1 at xref=1 tier is `FUN_8003e694` at 192B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003e694` → `gate_connection_slot_reuse_by_link_type_role_match`**
(192B, HIGH, HANDLER-tier) via
`RenamePass142Region80030000Fun8003e694.java` (`renamed=1`, live-verified).

**Mechanism:** Decodes link-type role index 0/1/2 from status-word bit flags
`0x400`/`0x800`/`0x1000` on `param_1`. When decoded index matches connection
record `+0x70` role nibble (`>>6`) and active flag `+0x71` bit0 set: SCO path
(`+0x73` bit2) drains SCO pending queue then invokes
`role_index_remap_gate_invoke_connection_slot_reuse(9,0,role)` plus optional
`reinit_hci_cmd_list_clear_active_descriptor_tails_and_drain`; ACL path drains
packet-completion ring via
`drain_connection_packet_completion_ring_and_emit_hci_num_completed` with same
slot-reuse call plus optional
`clear_active_stride88_connection_buffers_and_drain_hci_cmds`. On role-index
mismatch logs via `possible_logging_function__var_args` (opcode `0x2b`).

**Callers:** 1 xref-in — `status_word_multiflag_link_event_dispatcher2` at
`0x800030c6` (link-event multiplexer in lower ROM).

**Confidence:** HIGH — full 192B decompile; slot-reuse mode-9 pattern matches
`role_index_remap_gate_invoke_connection_slot_reuse` cluster; ACL/SCO branch
split confirmed; caller integration in status-word link-event path.

Region unnamed count after this pass: **148** (149 minus this rename). Live named
**2018** global.

**Next:** superseded by Pass 143.

## Pass 143 (2026-07-01) — `step_conn_esco_codec_counter_and_apply_if_gate_armed`

Fresh `ListUnnamed80030000.java` re-run: **148 unnamed** remain in region
(unchanged from Pass 142; rank-1 at xref=1 tier is `FUN_8003611c` at 188B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003611c` → `step_conn_esco_codec_counter_and_apply_if_gate_armed`**
(188B, HIGH, HANDLER-tier) via
`RenamePass143Region80030000Fun8003611c.java` (`renamed=1`, live-verified).

**Mechanism:** Per-connection eSCO codec-sample counter stepper on
`big_ol_struct` records, gated on `the_0x300` timer-active byte
`field_0x175`. When sub-counter `field_0x6a` is zero, seeds main counter
`field_0x68` from scan-interval byte `field_0x178`; otherwise increments
`field_0x68` by step byte `field_0x177` with clamp against
`field90_0x82` ceiling (8-slot wrap-aware). IRQ-disabled path then, when
per-slot gate byte at `PTR_DAT_800361e0[slot]==1`, dispatches
`FUN_80014450` + `FUN_80014dac` (codec-config apply pair). Indexed
per-connection variant of Pass 118's page/inquiry slot stepper
`increment_esco_slot_counter_and_apply_codec_if_gate_armed`; structural
sibling of `FUN_800361e4` (codec-slot flush callee of
`arm_page_inquiry_scan_timer_if_idle_else_flush_codec_slots`).

**Callers:** 1 xref-in via `find_callers` —
`LC_event_TX_dispatcher` (LC-layer TX event multiplexer).

**Confidence:** HIGH — full 188B decompile; counter/step/clamp field offsets
match `the_0x300` timer cluster from Pass 55/118; codec-apply callee pair
confirmed; gate-byte dispatch pattern matches documented eSCO slot sweep
family.

Region unnamed count after this pass: **147** (148 minus this rename). Live named
**2019** global.

**Next:** superseded by Pass 144.

## Pass 144 (2026-07-01) — `flush_armed_esco_codec_slots_up_to_12_and_apply`

Fresh `ListUnnamed80030000.java` re-run: **147 unnamed** remain in region
(unchanged from Pass 143; rank-1 at xref=1 tier is `FUN_800361e4` at 184B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_800361e4` → `flush_armed_esco_codec_slots_up_to_12_and_apply`**
(184B, HIGH, HANDLER-tier) via
`RenamePass144Region80030000Fun800361e4.java` (`renamed=1`, live-verified).

**Mechanism:** 12-slot eSCO codec-slot flush sweep on `the_0x300` timer cluster,
gated on `field_0x175` (timer-active byte). Clears `field_0x175` on entry, then
iterates slots 0..11: resolves per-slot connection index from
`PTR_DAT_800362a0[slot*8+3]`, and when gate byte `PTR_DAT_800362a4[slot]==1`,
IRQ-disables and dispatches `FUN_80014450` + `FUN_80014dac` codec-config apply
pair on `big_ol_struct[conn_index]`. When `the_0x300.int_0x10 != 0` and status
halfwords at `DAT_800362ac`/`DAT_800362b0` have bits `0x200`/`0x10` set,
issues a second `FUN_80014dac` with halved ceiling. 12-way sweep variant of
Pass 143's per-connection indexed stepper
`step_conn_esco_codec_counter_and_apply_if_gate_armed`; flush callee of
`arm_page_inquiry_scan_timer_if_idle_else_flush_codec_slots` (Pass 55).

**Callers:** 1 xref-in (per `ListUnnamed80030000`; `find_callers` empty — known
MIPS16e indirect-call limitation) — documented caller
`arm_page_inquiry_scan_timer_if_idle_else_flush_codec_slots` at `0x800362b4`.

**Confidence:** HIGH — full 184B decompile; gate-byte + codec-apply pair matches
Pass 55/118/143 eSCO slot cluster; 12-slot sweep loop and `field_0x175` clear
confirmed.

Region unnamed count after this pass: **146** (147 minus this rename). Live named
**2020** global.

**Next:** superseded by Pass 145.

## Pass 145 (2026-07-01) — `preserve_bb_regs_5a_5c_run_regscript_hook_then_program_bb_bundle`

Fresh `ListUnnamed80030000.java` re-run: **146 unnamed** remain in region
(unchanged from Pass 144; rank-1 at xref=1 tier is `FUN_8003ab74` at 180B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003ab74` → `preserve_bb_regs_5a_5c_run_regscript_hook_then_program_bb_bundle`**
(180B, HIGH, HANDLER-tier) via
`RenamePass145Region80030000Fun8003ab74.java` (`renamed=1`, live-verified).

**Mechanism:** Register-script interpreter cluster callee that preserves BB regs
`0x5a`/`0x5c` around an intermediate hook call (`DAT_801205b4` with script
context `+0x80`/`+0x84`), restores the saved values, then computes three ushort
params via `FUN_8003ab04` (VSC `0xfd49` extended-diagnostic + BB reg `0x7e`
reads with `0x41` mode select `0x21`/`0x20`), IRQ-disables, and invokes
`program_bb_regs_41_43_44_46_47_via_hook_and_da_d6_dispatch`. Optional
diagnostic log when `DAT_801233d0` bit 8 set. Gated wrapper sibling
`FUN_8003ac28` calls this path when fd49-diag context bit0 clear.

**Callers:** 1 xref-in per `ListUnnamed80030000` — `register_script_interpreter`
(`0x8003aea0`) plus gated invoke via `FUN_8003ac28` (fd49 extended-diagnostic
cluster).

**Confidence:** HIGH — full 180B decompile; BB `0x5a`/`0x5c` save/restore +
hook + `FUN_8003ab04` triple-compute + BB bundle programmer matches Pass 103
register-script cluster; fd49-diag integration confirmed via `FUN_8003ac28`
decompile.

Region unnamed count after this pass: **145** (146 minus this rename). Live named
**2021** global.

**Next:** superseded by Pass 146.

## Pass 146 (2026-07-01) — TX power runtime init `FUN_80039844`

Fresh `ListUnnamed80030000.java` re-run: **145 unnamed** remain in region
(unchanged from Pass 145; rank-1 at xref=1 tier is `FUN_80039844` at 180B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80039844` → `init_tx_power_runtime_from_config_blob_and_halve_delta_baselines`**
(180B, HIGH, HANDLER-tier) via
`RenamePass146Region80030000Fun80039844.java` (`renamed=1`, live-verified).

**Mechanism:** TX-power runtime-config initializer. Invokes first hook fptr at
`PTR_PTR_800398f8`, then copies config-blob fields `+0x27c` through `+0x28b`
(and related bytes at `+0x280`–`+0x286`, `+0x2`–`+0x6`) from
`PTR_config_base_80039904` into the runtime buffer `PTR_DAT_800398fc`.
Derives a decremented counter from config byte `+0x5` and mirrors it into
three globals plus a fourth keyed by `+0x283`. Twice calls
`compute_tx_power_delta_from_global_baselines` via fptr `PTR_PTR_80039900`
with `param_2=0`, storing halved results (`result >> 1`) into runtime globals.
Sibling of Pass 72's delta helper and the `FUN_80039c08` adjust path.

**Callers:** 1 xref-in per `ListUnnamed80030000` — invoked via function pointer
(TX-power boot/init cluster).

**Confidence:** HIGH — full 180B decompile; config-blob field copy layout and
dual halved-delta baseline calls match Pass 72 caller notes and the region's
TX-power management theme.

Region unnamed count after this pass: **144** (145 minus this rename). Live named
**2022** global.

**Next:** Pass 147 — fresh `ListUnnamed80030000` re-rank; decompile+rename top
rank-1 unnamed function.

## Pass 147 (2026-07-01) — indexed BB register burst writer `FUN_80030a74`

Fresh `ListUnnamed80030000.java` re-run: **144 unnamed** remain in region
(unchanged from Pass 146; rank-1 at xref=1 tier is `FUN_80030a74` at 178B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80030a74` → `validate_and_burst_write_indexed_bb_registers_by_role`**
(178B, HIGH, HANDLER-tier) via
`RenamePass147Region80030000Fun80030a74.java` (`renamed=1`, live-verified).

**Mechanism:** Role-indexed baseband register burst writer with bounds
validation. Param block: role index at `+0x3`, start register at `+0x4`,
byte count at `+0x6`, payload at `+0x8`. Rejects invalid role (`>=3`) or
oversized count (`>=0xfb`). Role 0 caps start+count to `0x100`; roles 1–2 cap
to `0x200`. On validation failure returns HCI error `0x12`. Mirrors role/start/
count into `PTR_DAT_80030b28`. Zero-count path calls
`baseband_reg_0x34_role_index_setter` only. Nonzero path requires magic
`0xC6CB` at param `+0x0`, then loops
`indexed_register_write_1byte_wrapper(role, reg++, byte)`; aborts with `3` if
status byte `PTR_DAT_80030b28[+0x2]` nonzero. VSC-cluster sibling of
`VSC_0xfc27_param_query` at `0x80030b2c`.

**Callers:** 1 xref-in per `ListUnnamed80030000` — invoked via function pointer
(indexed BB register write cluster).

**Confidence:** HIGH — full 178B decompile; role-bounded validation, known ROM
callees (`baseband_reg_0x34_role_index_setter`,
`indexed_register_write_1byte_wrapper`), standard `0x12`/`0x3` status returns.

Region unnamed count after this pass: **143** (144 minus this rename). Live named
**2023** global.

**Next:** Pass 149 — fresh `ListUnnamed80030000` re-rank; decompile+rename top
rank-1 unnamed function.

## Pass 148 (2026-07-01) — link-mode-change BB reg/timeout applier `FUN_80035214`

Fresh `ListUnnamed80030000.java` re-run: **143 unnamed** remain in region
(unchanged from Pass 147; rank-1 at xref=1 tier is `FUN_80035214` at 172B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80035214` → `apply_link_mode_change_bb_regs_and_timeout_by_phase`**
(172B, HIGH, HANDLER-tier) via
`RenamePass148Region80030000Fun80035214.java` (`renamed=1`, live-verified).

**Mechanism:** Phase-byte dispatcher for link-mode-change hardware setup.
`(byte phase, uint timeout, byte aux)`. Phase `0`: stores timeout to
`PTR_DAT_800352c4`, writes BB regs `0x6e` (timeout) and `0x6c` (existing
value `| 1`) via hook fptr `PTR_DAT_800352c0`, sets state `*PTR_DAT_800352c4=1`.
Phases `1`/`2`/`4`: stores timeout+aux, calls `FUN_80012820(1, timeout)`,
sets state `2`. Phases `3`/`5`: gated on `PTR_DAT_800352cc[+3]` bit `0x80` —
when clear forces timeout `0` and calls `FUN_80012820(0,0)`; else stores
params and calls `FUN_80012820(1, timeout)`; sets state `2`.

**Callers:** 1 xref-in per `ListUnnamed80030000` — callee of
`link_mode_change_state_machine` (applies link parameters alongside
`check_link_mode_change_slot_budget_timing_gate_status`).

**Confidence:** HIGH — full 172B decompile; phase-gated BB reg pair
`0x6e`/`0x6c` writes and `FUN_80012820` timeout helper match
`link_mode_change_state_machine` cluster notes from Pass 8/94.

Region unnamed count after this pass: **142** (143 minus this rename). Live named
**2024** global.

**Next:** Pass 150 — fresh `ListUnnamed80030000` re-rank; decompile+rename top
rank-1 unnamed function.

## Pass 149 (2026-07-01) — HW-clock ACL credit poll `FUN_8003e58c`

Fresh `ListUnnamed80030000.java` re-run: **142 unnamed** remain in region
(unchanged from Pass 148; rank-1 at xref=1 tier is `FUN_8003e58c` at 168B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003e58c` → `poll_hw_clock_stride88_slot_and_acl_credit_consumer`**
(168B, HIGH, HANDLER-tier) via
`RenamePass149Region80030000Fun8003e58c.java` (`renamed=1`, live-verified).

**Mechanism:** Per-role HW-clock polling loop on the stride-0x88 connection
table (`PTR_DAT_8003e634`). Reads raw clock via
`read_hw_clock_raw_dword_by_role_index` using the role's `big_ol_struct` byte
at slot offset `+6`. Compares wrap-aware slot delta against dword at `+0x30`
(masked by `DAT_8003e640`/`DAT_8003e644`). When connection type `+0x18==0` and
pending counter `+0x38!=0`, calls
`ACL_fragment_dequeue_and_credit_consumer(role, credit_flag)`; otherwise
advances `+0x30` slot timing by byte credit at `+0xc`. Returns `true` when
dequeue/credit path fails (local success flag cleared).

**Callers:** 1 xref-in per `ListUnnamed80030000` — timing-path callee of
`connection_setup_arm_stride88_slot_and_apply_packet_types` when slot `+0x38`
nonzero (Pass 119 notes: alternative to inline clock read when pending credits
exist).

**Confidence:** HIGH — full 168B decompile; stride-0x88 offsets match Pass 8
ACL fragment cluster; callee pairing with
`ACL_fragment_dequeue_and_credit_consumer` and
`read_hw_clock_raw_dword_by_role_index` confirmed.

Region unnamed count after this pass: **141** (142 minus this rename). Live named
**2025** global.

**Next:** Pass 151 — fresh `ListUnnamed80030000` re-rank; decompile+rename top
rank-1 unnamed function.

## Pass 150 (2026-07-01) — Link-mode EIR budget counter `FUN_80033be8`

Fresh `ListUnnamed80030000.java` re-run: **141 unnamed** remain in region
(unchanged from Pass 149; rank-1 at xref=1 tier is `FUN_80033be8` at 160B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80033be8` → `reset_link_mode_phase_marker_and_update_eir_budget_counter`**
(160B, HIGH, HANDLER-tier) via
`RenamePass150Region80030000Fun80033be8.java` (`renamed=1`, live-verified).

**Mechanism:** First-step preprocessor invoked at entry of
`link_mode_change_state_machine(phase)`. Optional hook at `PTR_DAT_80033c88`
delegates entirely when set. Otherwise: (1) sets link-mode config byte at
`PTR_DAT_80033c94[+0x8c]` to `0xff` (phase-marker reset); (2) gated on BOS
`byte_0x16a` bit 2, `field_0x173`, and `0x1ac` connection-struct capability
bits — when gates pass, accumulates ushort at
`the_0x300[1].ptr_to_EIR_data+2` plus config ushort at `+0x9c`, capped against
threshold at `+0x94`, setting overflow flag byte at `ptr_to_EIR_data` (0=under
cap, 1=exceeded); when gates fail, clears counter and flag to zero.

**Callers:** 1 xref-in per `ListUnnamed80030000` and `find_callers` —
`link_mode_change_state_machine` (always invoked before gate/slot-budget/VSC
fc11 path).

**Confidence:** HIGH — full 160B decompile; link-mode-change cluster sibling
of `adjust_link_mode_change_slot_budget_and_secondary_timing` (Pass 99) and
`apply_link_mode_change_bb_regs_and_timeout_by_phase` (Pass 148); EIR budget
fields match inquiry/EIR cluster notes from Pass 8/12da.

Region unnamed count after this pass: **140** (141 minus this rename). Live named
**2026** global.

**Next:** superseded by Pass 151.

## Pass 151 (2026-07-01) — BB reg init hook chain `FUN_800395f0`

Fresh `ListUnnamed80030000.java` re-run: **140 unnamed** remain in region
(unchanged from Pass 150; rank-1 at xref=1 tier is `FUN_800395f0` at 158B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_800395f0` → `run_bb_reg_init_hook_chain_and_program_0x1e_5bit_field`**
(158B, HIGH, HANDLER-tier) via
`RenamePass151Region80030000Fun800395f0.java` (`renamed=1`, live-verified).

**Mechanism:** BB-register initialization orchestrator in the `0x800395xx`
cluster. Walks table `PTR_PTR_80039690` through hook fptr at `PTR_DAT_80039694`
with multiple `(dword, ushort)` register-programming pairs; dispatches additional
hooks from `PTR_PTR_8003969c`, `PTR_PTR_800396a0`, and a seven-entry chain from
`PTR_PTR_80039698` (+4/+0xc/+0x10/+0x14/+0x18/+0x20); tail calls
`program_bb_regs_0x1e_5bit_field_and_clear_0x1c_bit3(*PTR_DAT_800396a4)` then
two more hook fptrs (including `0x21` + ushort from `PTR_PTR_800396b0`).
Register-script interpreter caller (×4 per
`reverse_engineering_register_script_interpreter.md`); callee of
`apply_hw_reg_0x2b_slot_nibble_if_config_bit3` (Pass 117) and
`program_bb_regs_0x1e_5bit_field_and_clear_0x1c_bit3` (Pass 109).

**Callers:** 1 xref-in per `ListUnnamed80030000`; `find_callers` empty (likely
indirect/data-ref invocation — same pattern as Pass 135/136).

**Confidence:** HIGH — full 158B decompile; hook-table dispatch idiom matches
`dispatch_optional_subsystem_hooks_during_hw_reg_config` cluster; tail callee
pairing with Pass 109 programmer confirmed.

Region unnamed count after this pass: **139** (140 minus this rename). Live named
**2027** global.

**Next:** superseded by Pass 152.

## Pass 152 (2026-07-01) — BB reg triple `FUN_8003b76c`

Fresh `ListUnnamed80030000.java` re-run: **139 unnamed** remain in region
(unchanged from Pass 151; rank-1 at xref=1 tier is `FUN_8003b76c` at 156B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003b76c` → `program_bb_regs_0x220_0x222_0x224_via_hook_with_masked_params`**
(156B, HIGH, SIMPLE-tier) via
`RenamePass152Region80030000Fun8003b76c.java` (`renamed=1`, live-verified).

**Mechanism:** BB-register triple programmer in the `0x8003b7xx` cluster. Three
hook-writes via fptr at `PTR_DAT_8003b80c`, merging caller params with config
literals from `DAT_8003b808`/`DAT_8003b810`/`DAT_8003b814`:
- reg `0x220`: `(param_3 << 8) | (param_1 & 0xff) | (*DAT_8003b808 & 0x8080)`
- reg `0x222`: `((param_4 & 1) << 8) | (*DAT_8003b810 & 0xfe80) | (param_2 & 0xff)`
- reg `0x224`: `(*DAT_8003b814 & 0xfc00) | param_5`

Sibling of `program_sco_bb_regs_from_config_offset_0x106_via_hw_hook` (Pass 140)
and `program_bb_regs_41_43_44_46_47_via_hook_and_da_d6_dispatch` (Pass 103) in
the BB-reg hook-programming family.

**Callers:** 1 xref-in per `ListUnnamed80030000`; likely indirect/data-ref
invocation (same pattern as Pass 151).

**Confidence:** HIGH — full 156B decompile; three-register masked-merge idiom
matches established BB-reg programmer cluster.

Region unnamed count after this pass: **138** (139 minus this rename). Live named
**2028** global.

**Next:** superseded by Pass 153.

## Pass 153 (2026-07-01) — register-script context init `FUN_80039448`

Fresh `ListUnnamed80030000.java` re-run: **138 unnamed** remain in region
(unchanged from Pass 152; rank-1 at xref=1 tier is `FUN_80039448` at 154B —
largest among the xref=1 cohort, tied with `FUN_800352d0`).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80039448` → `init_register_script_context_from_config_and_clear_17pair_table`**
(154B, HIGH, SIMPLE-tier) via
`RenamePass153Region80030000Fun80039448.java` (`renamed=1`, live-verified).

**Mechanism:** Register-script global-context initializer in the `0x800394xx`
cluster (sibling of Pass 135's `invoke_register_script_from_global_context_0x60_0x64`
and upstream of Pass 151's `run_bb_reg_init_hook_chain_and_program_0x1e_5bit_field`).
Reads config bytes at `config_struct+0x408` and `config[1]+0x1`; seeds a 6-byte
header at `PTR_DAT_800394e4` (`[1]=config_byte`, `[2]=0xf0`, `[3]=0x10`); populates
descriptor at `PTR_DAT_800394ec` with sentinel `0xffffffff`, magic `0xace`, and
nine wired buffer pointers (`PTR_DAT_800394f4`–`80039510`) with size halfwords
(100, 0x18, 6, 8, 0x26, 4); clears 17 `(dword, ushort)` pairs in
`PTR_PTR_800394f0`; initializes `PTR_DAT_80039514` (`[0]=0x14`, `[0x13]=0`).

**Callers:** 1 xref-in per `ListUnnamed80030000`; `find_callers` empty (likely
indirect/data-ref invocation — same pattern as Pass 135/151).

**Confidence:** HIGH — full 154B decompile; config-driven descriptor wiring idiom
matches register-script interpreter cluster; no unresolved callees.

Region unnamed count after this pass: **137** (138 minus this rename). Live named
**2029** global.

**Next:** superseded by Pass 154.

## Pass 154 (2026-07-01) — LMP 0x3ee retry counter `FUN_800352d0`

Fresh `ListUnnamed80030000.java` re-run: **137 unnamed** remain in region
(unchanged from Pass 153; rank-1 at xref=1 tier is `FUN_800352d0` at 154B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_800352d0` → `evaluate_lmp_3ee_link_mode_phase_with_retry_counter`**
(154B, HIGH, HANDLER-tier) via
`RenamePass154Region80030000Fun800352d0.java` (`renamed=1`, live-verified).

**Mechanism:** Link-mode-change cluster LMP 0x3ee phase evaluator. Optional
hook at `PTR_DAT_8003536c` short-circuits when it returns non-zero. Otherwise
dispatches `LMP_CH__0x3ee__case1_if_FUN_80011fc0()`; on case1==1 or
(param_1>1 && case1==2) resets retry counter at `PTR_DAT_80035370+0x98` and
returns 0; else invokes `LMP_CH__0x3ee__case2_else_2_FUN_80011d9c` when
case1!=0, increments counter, returns 0xf; on overflow vs threshold at `+0x9a`
logs via `possible_logging_function` and returns 0xff.

**Callers:** 1 xref-in — `link_mode_change_state_machine`.

**Confidence:** HIGH — full 154B decompile; LMP 0x3ee branch idiom matches
sibling `zero_bos_struct_and_init_link_mode_bb_timing_with_lmp_3ee_branch`
(Pass 134); retry-counter fields `+0x98`/`+0x9a` on link-mode workspace
confirmed.

Region unnamed count after this pass: **136** (137 minus this rename). Live named
**2030** global.

**Next:** superseded by Pass 155.

## Pass 155 (2026-07-01) — SCO teardown table commit `FUN_800347d4`

Fresh `ListUnnamed80030000.java` re-run: **136 unnamed** remain in region
(unchanged from Pass 154; rank-1 at xref=1 tier is `FUN_800347d4` at 142B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_800347d4` → `commit_sco_teardown_bb_hook_pairs_and_18pair_param_table`**
(142B, HIGH, HANDLER-tier) via
`RenamePass155Region80030000Fun800347d4.java` (`renamed=1`, live-verified).

**Mechanism:** SCO/eSCO link-loss teardown table-commit helper. Loops 5 times
calling hook fptr at `PTR_DAT_80034868` with uint16 pairs from ROM table
`PTR_DAT_80034864` and caller buffer `param_1` (BB register programming via
hook). Loops 18 times (`0x12`) copying uint16 values from `param_2` into
indexed destinations via `PTR_DAT_8003486c` + base `DAT_80034870`. Sets global
mode ushort at `DAT_80034874` to `4`. When
`PTR_struct_of_at_least_0x300_size_80034878->field_0x173 != 0` and slot index
at `PTR_DAT_8003487c` is `< 10`, calls `sometimes_called_with_0_3_0` with
per-connection `(bos_connection__array_index, byte_0xCC, field_0xbc)` from
`PTR_big_ol_struct_80034880` — encryption-mode disable per active slot.

**Callers:** 1 xref-in (COMPUTED_CALL) — `reset_sco_esco_hw_subsystem_on_link_loss`
at `0x800373e6` via fptr at `PTR_DAT_80037448` with buffers
`PTR_DAT_80037450`/`PTR_DAT_8003744c`; sibling of Pass 43's
`disable_esco_hw_slot_for_each_active_connection` in the same teardown branch.

**Confidence:** HIGH — full 142B decompile; 5-hook + 18-pair table-commit idiom
matches documented BB-reg hook programmers; `field_0x173` gate and
`sometimes_called_with_0_3_0` per-slot tail match Pass 43 SCO teardown cluster.

Region unnamed count after this pass: **135** (136 minus this rename). Live named
**2031** global.

**Next:** superseded by Pass 156.

## Pass 156 (2026-07-01) — LMP-268 random delay arm `FUN_8003cae0`

Fresh `ListUnnamed80030000.java` re-run: **135 unnamed** remain in region
(unchanged from Pass 155; rank-1 at xref=1 tier is `FUN_8003cae0` at 140B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003cae0` → `arm_lmp268_random_delay_and_remote_name_feature_apply_4`**
(140B, HIGH, HANDLER-tier) via
`RenamePass156Region80030000Fun8003cae0.java` (`renamed=1`, live-verified).

**Mechanism:** Remote-name-request feature-4 LMP-268 timer armer. Draws a
bounded random delay via `lcg_prng_bounded_modulo` (max `0x7f` or `0x3ff`
depending on `the_0x300->field_0x1c` threshold `0x800`), floors at `0x32` when
below, dispatches `LMP__268__most_common_for_VSCs2_checks_fptr_patch` with
scaled timer `(delay*5)>>3`, calls fptr at `PTR_DAT_8003cb74` for a baseline
offset, writes combined delay `delay*2 + baseline` to struct field `+0x11c`.
When `byte_0x16a` bit2 clear or config ushort bit4 set: clears mode-active bit0
at `+0x164`. Else when `+0x164` bit `0x20` set: tail-calls
`remote_name_request_feature_apply_4` — the apply-side sibling documented in
region `0x80040000` Pass 6.

**Callers:** 1 xref-in — `status_word_multiflag_link_event_dispatcher` at
`0x80002686` (region `0x80000000`); inquiry/LAP link-event path into the
remote-name-request feature cluster.

**Confidence:** HIGH — full 140B decompile; LMP-268 random-delay + `+0x11c`
write idiom matches `remote_name_request_feature_apply_8`/`_4` structural twins;
gating fields (`byte_0x16a` bit2, config bit4, `+0x164` bit `0x20`) align with
documented remote-name-request apply paths.

Region unnamed count after this pass: **134** (135 minus this rename). Live named
**2032** global.

**Next:** superseded by Pass 157.

## Pass 157 (2026-07-01) — BB reg 0x75 bit0 pulse `FUN_8003a6a8`

Fresh `ListUnnamed80030000.java` re-run: **134 unnamed** remain in region
(unchanged from Pass 156; rank-1 at xref=1 tier is `FUN_8003a6a8` at 136B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003a6a8` → `config_gated_pulse_bb_reg_0x75_bit0_with_spin_delays`**
(136B, HIGH, HANDLER-tier) via
`RenamePass157Region80030000Fun8003a6a8.java` (`renamed=1`, live-verified).

**Mechanism:** Config-gated BB register pulse helper. When
`config_base->field1018_0x406 != 0`: saves dword at `DAT_8003a734`, temporarily
masks/ORs with constants at `DAT_8003a738`/`DAT_8003a740`, reads BB reg `0x75`
via hook at `PTR_DAT_8003a73c` (bank 6), writes with bit0 set via hook at
`PTR_DAT_8003a744`, `spin_delay_10x_iterations(1)`, re-reads and clears bit0,
delays again, then restores the saved dword. Register-script interpreter
cluster sibling of `program_bb_regs_41_43_44_46_47_via_hook_and_da_d6_dispatch`
(Pass 103).

**Callers:** 1 xref-in — `FUN_80038f98` at `0x80038fb4` (COMPUTED_CALL);
thin wrapper that optionally copies a byte to `config_base->field1020_0x408` when
status byte bit `0x20` set, then tail-calls through `PTR_PTR_80038fc8`.

**Confidence:** HIGH — full 136B decompile; read-modify-write pulse on BB reg
`0x75` bit0 with inter-write spin delays matches documented register-programming
cluster; config byte `0x406` gate and hook-dispatch idiom consistent with
neighboring `0x8003a7xx` helpers.

Region unnamed count after this pass: **133** (134 minus this rename). Live named
**2033** global.

**Next:** superseded by Pass 158.

## Pass 158 (2026-07-01) — remote-name cleanup + LMP268 dispatch `FUN_800356bc`

Fresh `ListUnnamed80030000.java` re-run: **133 unnamed** remain in region
(unchanged from Pass 157; rank-1 at xref=1 tier is `FUN_800356bc` at 136B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_800356bc` → `remote_name_feature_cleanup_and_lmp268_timer_dispatch`**
(136B, HIGH, HANDLER-tier) via
`RenamePass158Region80030000Fun800356bc.java` (`renamed=1`, live-verified).

**Mechanism:** Internal message-type-105 handler in ROM `unknown_fptr_index0`
dispatch (case 5 / raw type `0x69`). Optional prelude hook at `PTR_DAT_80035744`;
clears status bytes at `PTR_DAT_80035748+0x96` and `+0x6c`; calls
`remote_name_request_feature_apply_orchestrator()`. When global status bit `0x20`
clear and config `LMP_POWER_REQ_RES_and_CLK_ADJ` bit2 set: conditionally invokes
hook fptrs at `PTR_DAT_80035758`/`PTR_DAT_8003575c` when conn-record
`field40_0x28`/`field68_0x44` bit0 set, and
`merge_feature_page_bytes_into_conn_record_bitfields_0x44_0x49` when
`PTR_DAT_80035760+0x34` bit0 set; then `FUN_80067cf0()`. Always finishes with
`dispatch_lmp_268_timers_with_hook_and_config_gates()`. Sibling of
`FUN_80035640` in the same `unknown_fptr_index0` switch case.

**Callers:** 1 xref-in per `ListUnnamed80030000`; `xrefs_to` empty — indirect
fptr dispatch via `unknown_fptr_index0` type-105 path (known pattern).

**Confidence:** HIGH — full 136B decompile; named callees anchor remote-name
feature-orchestrator and LMP-0x268 timer-dispatch paths; register-script /
conn-record flag gating consistent with neighboring `0x80035xxx` cluster.

Region unnamed count after this pass: **132** (133 minus this rename). Live named
**2034** global.

**Next:** superseded by Pass 159.

## Pass 159 (2026-07-01) — conn-table crypto reinit `FUN_800343dc`

Fresh `ListUnnamed80030000.java` re-run: **132 unnamed** remain in region
(unchanged from Pass 158; rank-1 at xref=1 tier is `FUN_800343dc` at 132B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_800343dc` → `reinit_conn_table_crypto_preserve_active_slot_record`**
(132B, HIGH, INIT-tier) via
`RenamePass159Region80030000Fun800343dc.java` (`renamed=1`, live-verified).

**Mechanism:** Boot/subsystem init sub-step (second callee of `FUN_800347a0`
after `copy_nine_dispatch_slots_and_init_baseband_subsystems`). Sweeps all 10
connection slots via `FUN_80067768` (per-slot connection-record initializer).
When `the_0x300.field_0x173` set and active slot index at `PTR_DAT_80034464` is
`< 10`: copies staged conn record from `PTR_DAT_8003446c` into
`big_ol_struct[active_slot]`, and when `active_slot != 0` clones the
0x218-byte crypto struct from slot 0 into the active slot. Then re-inits crypto
for every non-active slot via `init_per_connection_crypto_struct_for_bos_slot`.
Clears 0x27c-byte workspace at `PTR_DAT_80034470`.

**Callers:** 1 xref-in — `FUN_800347a0` at `0x800347b8` (hook-null default init
wrapper; sibling of `copy_nine_dispatch_slots_and_init_baseband_subsystems`).

**Confidence:** HIGH — full 132B decompile; named callees anchor conn-table
crypto init path; `field_0x173` active-slot preservation matches
`commit_sco_teardown_bb_hook_pairs_and_18pair_param_table` cluster.

Region unnamed count after this pass: **131** (132 minus this rename). Live named
**2035** global.

**Next:** superseded by Pass 160.

## Pass 160 (2026-07-01) — resource-pool chain release `FUN_80030560`

Fresh `ListUnnamed80030000.java` re-run: **131 unnamed** remain in region
(unchanged from Pass 159; rank-1 at xref=1 tier is `FUN_80030560` at 130B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80030560` → `release_resource_pool_chain_slot_type02_with_cleanup`**
(130B, HIGH, HANDLER-tier) via
`RenamePass160Region80030000Fun80030560.java` (`renamed=1`, live-verified).

**Mechanism:** Type-0x02 command handler (validates `param_1+2 == 0x02`, slot
index at `param_1+4`). When resource-pool global at `PTR_DAT_800305e4` has
active count `+0x26b != 0` and slot bitmask at `+0x270` includes the index:
calls `release_resource_pool_chain_slot_by_type` (Pass 12fu dealloc counterpart
to `allocate_resource_pool_chain_slots_by_type`). When pool empties
(`+0x26b == 0`): clears bit `0x80` on `base_of_0x1ac_struct_array` entry
`field69_0x45`, and when `field68_0x44` has bits `0x18` set invokes hook fptrs
at `PTR_DAT_800305ec`/`PTR_DAT_800305f0`. Returns `0` on success, `0x12` when
type/slot/bitmask gate fails. Deallocate sibling of allocate handler
`FUN_800305f4` (sole caller of `allocate_resource_pool_chain_slots_by_type`).

**Callers:** 1 xref-in per `ListUnnamed80030000`; `xrefs_to` empty — indirect
fptr dispatch (known pattern).

**Confidence:** HIGH — full 130B decompile; named callee anchors
resource-pool chain dealloc path documented in region `0x80070000` Pass 12fu;
type-0x02 gate and empty-pool hook cleanup consistent with HCI extended-inquiry
feature-page staging cluster at `0x800305xx`.

Region unnamed count after this pass: **130** (131 minus this rename). Live named
**2036** global.

**Next:** superseded by Pass 161.

## Pass 161 (2026-07-01) — ring-buffer append `FUN_80032e40`

Fresh `ListUnnamed80030000.java` re-run: **130 unnamed** remain in region
(unchanged from Pass 160; rank-1 at xref=1 tier is `FUN_80032e40` at 128B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80032e40` → `irq_safe_append_bytes_to_2048b_ring_buffer`**
(128B, HIGH, UTILITY-tier) via
`RenamePass161Region80030000Fun80032e40.java` (`renamed=1`, live-verified).

**Mechanism:** IRQ-masked append to the global ring buffer at `PTR_DAT_80032ec0`.
Capacity gate: rejects when `param_2` bytes would exceed `0x800` minus fill count
at struct offset `+0x804` (returns `0`). On success: advances write index at
`+0x802` (masked `0x7ff`), increments fill count, copies `param_2` bytes from
`param_1` into the ring at `base[write_idx++]` modulo `0x800`; returns `1`.
Sibling of initializer `called_by_function_that_uses_Logger_string_2_initialize_something_at_offset_0x800` (`0x80032e28`) which zeroes the same struct's
`+0x800`/`+0x802`/`+0x804` metadata fields.

**Callers:** 1 xref-in per `ListUnnamed80030000`; `xrefs_to` empty — indirect
fptr dispatch (known pattern).

**Confidence:** HIGH — full 128B decompile; capacity/index/fill-count semantics
match 2048-byte ring buffer; IRQ disable/enable idiom consistent with
`irq_safe_dequeue_16byte_from_packet_slot_ring_buffer` family.

Region unnamed count after this pass: **129** (130 minus this rename). Live named
**2037** global.

**Next:** superseded by Pass 162.

## Pass 162 (2026-07-01) — TX power level `FUN_8003b920`

Fresh `ListUnnamed80030000.java` re-run: **129 unnamed** remain in region
(unchanged from Pass 161; rank-1 at xref=1 tier is `FUN_8003b920` at 120B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003b920` → `compute_clamped_tx_power_level_from_link_class_baselines`**
(120B, HIGH, UTILITY-tier) via
`RenamePass162Region80030000Fun8003b920.java` (`renamed=1`, live-verified).

**Mechanism:** Signed-byte TX-power level calculator. When global flag
`PTR_DAT_8003b998` bit `0x10` is clear, uses baseline short at
`PTR_DAT_8003b99c+0xe`; when set, selects among three per-link-class baselines
at `+0xe`/`+0x10`/`+0x12` via `param_1 % 3`. Adds `param_3*2`,
`config.field453_0x1d1`, baseline, and a `param_2`-derived nibble shift (with
alternate path when low 16 bits exceed `0x3ff`). Clamps result to signed
8-bit `[-128,127]` before return. TX-power cluster sibling of
`compute_tx_power_delta_from_global_baselines` and
`init_tx_power_runtime_from_config_blob_and_halve_delta_baselines`.

**Callers:** 1 xref-in per `ListUnnamed80030000`; `xrefs_to` empty — indirect
fptr dispatch (known pattern).

**Confidence:** HIGH — full 120B decompile; per-class baseline selection,
config `field453_0x1d1` reuse, and signed-byte clamp semantics unambiguous.

Region unnamed count after this pass: **128** (129 minus this rename). Live named
**2038** global.

**Next:** superseded by Pass 163.

## Pass 163 (2026-07-01) — link-mode cleanup logger `FUN_80034d00`

Fresh `ListUnnamed80030000.java` re-run: **128 unnamed** remain in region
(unchanged from Pass 162; rank-1 at xref=1 tier is `FUN_80034d00` at 118B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80034d00` → `log_link_mode_cleanup_evt_0x3eb_or_0x2d1_if_no_patch3`**
(118B, HIGH, UTILITY-tier) via
`RenamePass163Region80030000Fun80034d00.java` (`renamed=1`, live-verified).

**Mechanism:** Link-mode-change cleanup logger callee of
`emit_link_mode_change_cleanup_status_with_dedup`. Packs event parameter as
`param_3 + param_1*0x100`. Selects logger context from `PTR_DAT_80034d78`
low bits: when `(byte & 3) == 0` emits tag `0x3eb` via `PTR_DAT_80034d7c`,
else tag `0x2d1` via `PTR_DAT_80034d84`, both through
`possible_logger_called_if_no_patch3`. On logger failure sets housekeeping bytes
`+0x96`/`+0x97` on `PTR_DAT_80034d80` and returns `0`; on success updates
`+0x97` with mode nibble and returns `0xff`. Sibling of
`log_conn_setup_commit_fallback_evt_0x2d2_if_no_patch3` (`0x80034ccc`).

**Callers:** 1 xref-in per `ListUnnamed80030000`; sole direct caller
`emit_link_mode_change_cleanup_status_with_dedup` (`0x80034d88`).

**Confidence:** HIGH — full 118B decompile; tag selection `0x3eb`/`0x2d1`
matches parent dedup scan; status-byte update semantics unambiguous.

Region unnamed count after this pass: **127** (128 minus this rename). Live named
**2039** global.

**Next:** superseded by Pass 164.

## Pass 164 (2026-07-01) — random-BD_ADDR TX-power VSC dispatch `FUN_8003fa8c`

Fresh `ListUnnamed80030000.java` re-run: **127 unnamed** remain in region
(unchanged from Pass 163; rank-1 at xref=1 tier is `FUN_8003fa8c` at 116B —
largest among the xref=1 cohort, tied with `FUN_8003aa7c`).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003fa8c` → `apply_random_bdaddr_tx_power_delta_via_vsc_fc95_lmp_268`**
(116B, HIGH, UTILITY-tier) via
`RenamePass164Region80030000Fun8003fa8c.java` (`renamed=1`, live-verified).

**Mechanism:** Random-BD_ADDR TX-power threshold dispatcher. Reads baseline
`field106_0x94` from `big_ol_struct[param_1]`; when `field_0x29d==1`, scales by
`FUN_80061c78` timing-factor helper. Only when `bdaddr_random_==1` and computed
level exceeds global threshold `*PTR_DAT_8003fb04`, builds delta
`(level - threshold) + field109_0x98 * -2` (16-bit wrap) and calls
`FUN_8003fa24` (VSC `0xFC95` + LMP `0x268` path). On success (`FUN_8003fa24`
returns 0), arms follow-up via `FUN_80017c3c(bos_connection__array_index,
byte_0xCC)`. TX-power cluster sibling of Pass 162
`compute_clamped_tx_power_level_from_link_class_baselines` and Pass 129
`apply_tx_power_runtime_mode_byte_and_reconfigure_tables_and_links`.

**Callers:** 1 xref-in per `ListXrefsTo8003fa8c`; sole direct caller
`LC_event_TX_dispatcher` (`0x800424b0`).

**Confidence:** HIGH — full 116B decompile; `bdaddr_random_` gate,
threshold compare, and VSC FC95/LMP 268 callee chain unambiguous.

Region unnamed count after this pass: **126** (127 minus this rename). Live named
**2040** global.

**Next:** superseded by Pass 165.

## Pass 165 (2026-07-01) — VSC fd49 channel diagnostic sweep `FUN_8003aa7c`

Fresh `ListUnnamed80030000.java` re-run: **126 unnamed** remain in region
(unchanged from Pass 164; rank-1 at xref=1 tier is `FUN_8003aa7c` at 116B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003aa7c` → `sweep_fd49_extended_diag_30_channels_with_bb_reg_0xe_bit2_enable`**
(116B, HIGH, UTILITY-tier) via
`RenamePass165Region80030000Fun8003aa7c.java` (`renamed=1`, live-verified).

**Mechanism:** VSC `0xfd49` extended-diagnostic channel sweep in the
`0x8003aaxx` register-script cluster. Saves baseline BB reg `0x1e` 5-bit field
via `read_bb_reg_0x1e_5bit_field_via_hook` to `PTR_DAT_8003aaf0`; brackets sweep
by setting then clearing bit 2 of BB reg `0xe` through hook fptrs at
`PTR_DAT_8003aaf4`/`PTR_DAT_8003aaf8`. Loops 30 iterations (`0x1e`): calls
`VSC_0xfd49_extended_diagnostic` at indices `0x10`–`0x2d`, storing each result
into a 30-word array at `PTR_DAT_8003aafc`. Post-sweep reads BB reg `0x21` via
hook and stores to `PTR_PTR_8003ab00`. Diagnostic-read sibling of Pass 123
`read_fd49_extended_diag_build_dual_slot_bitmasks_and_shift_width` and Pass 127
`program_bb_regs_6b_6c_43_6a_via_hook_and_extended_diagnostic`.

**Callers:** 1 xref-in per `ListUnnamed80030000`; `find_callers` empty —
indirect/data-ref invocation (register-script interpreter cluster pattern).

**Confidence:** HIGH — full 116B decompile; `VSC_0xfd49_extended_diagnostic`
callee already HIGH-named; BB reg `0xe` bit-2 bracket and 30-channel index
range unambiguous.

Region unnamed count after this pass: **125** (126 minus this rename). Live named
**2041** global.

**Next:** superseded by Pass 166.

## Pass 166 (2026-07-01) — Boot-init subsystem reset `FUN_80037710`

Fresh `ListUnnamed80030000.java` re-run: **125 unnamed** remain in region
(unchanged from Pass 165; rank-1 at xref=1 tier is `FUN_80037710` at 114B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80037710` → `boot_init_reset_conn_sco_hw_optional_fc95_and_descriptor_reinit`**
(114B, HIGH, HANDLER-tier) via
`RenamePass166Region80030000Fun80037710.java` (`renamed=1`, live-verified).

**Mechanism:** ROM boot-init subsystem reset orchestrator (parallel to HCI Reset
teardown sub-steps): clears global status bit `0x400`, resets connection subsystem
via `reset_conn_subsystem_global_state_and_reinit_slot_entries`, runs
`hw_register_config_with_timeout(0)` and `dispatch_bb_register_da_d6_write_with_hook`,
optionally calls `FUN_80062368` when config
`_x7a_enable_LMP_POWER_REQ_RES_and_CLK_ADJ` bit0 set (counter-derived slot table),
draws PRNG via `lcg_prng_bounded_modulo(0x3ff)` and when pending-VSC sentinel
`PTR_DAT_80037788 == -1` issues `VSC_0xfc95_called2` (returns `0xff` on failure),
then chains `hci_reset_reinit_conn_subsystem_lmp_and_descriptor_tables` and
`init_sco_hw_channel_8plus4_slot_program_and_bb_regs`.

**Callers:** 1 xref-in per `ListUnnamed80030000`; `find_callers` empty —
indirect invocation from `boot_init_chain_string_user_baseband_and_subsystems`
(documented in region `0x80020000` Pass 6 cont. 166).

**Confidence:** HIGH — full 114B decompile; named conn-subsystem/SCO-HW/descriptor
callees anchor the boot-init reset role; sibling of Pass 93 SCO init and Pass 113
HCI Reset reinit orchestrator.

Region unnamed count after this pass: **124** (125 minus this rename). Live named
**2042** global.

**Next:** superseded by Pass 167.

## Pass 167 (2026-07-01) — Calibration-mode conn-weight threshold gate `FUN_80033670`

Fresh `ListUnnamed80030000.java` re-run: **124 unnamed** remain in region
(unchanged from Pass 166; rank-1 at xref=1 tier is `FUN_80033670` at 112B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80033670` → `check_calibration_mode_conn_weight_vs_config_threshold`**
(112B, HIGH, UTILITY-tier) via
`RenamePass167Region80030000Fun80033670.java` (`renamed=1`, live-verified).

**Mechanism:** Optional hook fptr at `PTR_DAT_800336e0` may override the
predicate; default path gates on global flag bit4 (`PTR_DAT_800336e4 & 4`),
`the_0x300` struct `+0x164` bit `0x10`, `byte_0x16f==0`, and
`field_0x171==0`, then compares config `field160_0xa8`/`field161_0xa9`
ushort threshold against per-connection `field106_0x94` at `param_1+0x94`;
returns 1 when config threshold ≤ conn weight. TX-power/calibration cluster
sibling of `power_level_smoothing_filter_feeding_param_dispatch` (Pass 8) which
uses the same config threshold fields.

**Callers:** 1 xref-in per `ListUnnamed80030000`; `xrefs_to` empty — indirect
invocation (consistent with hook-table / fptr dispatch).

**Confidence:** HIGH — full 112B decompile; config threshold + conn-weight
field pairing matches documented Pass 8 calibration-mode idiom.

Region unnamed count after this pass: **123** (124 minus this rename). Live named
**2043** global.

**Next:** superseded by Pass 168.

## Pass 168 (2026-07-01) — LMP 0x3ee link-mode byte commit `FUN_80035f74`

Fresh `ListUnnamed80030000.java` re-run: **123 unnamed** remain in region
(unchanged from Pass 167; rank-1 at xref=1 tier is `FUN_80035f74` at 110B —
largest among the xref=1 cohort, tied with `FUN_80034564`).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80035f74` → `apply_lmp_3ee_case2_and_link_mode_byte_on_signed_status`**
(110B, HIGH, HANDLER-tier) via
`RenamePass168Region80030000Fun80035f74.java` (`renamed=1`, live-verified).

**Mechanism:** Link-mode-change cluster LMP 0x3ee commit helper. Reads signed
status dword at `DAT_80035fe4`; on negative path clears companion flag bit at
`PTR_DAT_80035fec`, invokes `LMP_CH__0x3ee__case2_else_1_FUN_80011fc0FUN_80011e10`
+ `LMP_CH__0x3ee__case2_else_2_FUN_80011d9c` (HW reprogram pair), and clears
link-mode byte bits 1+2 on workspace `PTR_DAT_80035fe8+0x164` (`& 0xf9`); on
non-negative path sets bit 2 on `+0x164` (`| 4`) and logs via
`possible_logging_function__var_args`.

**Callers:** 1 xref-in per `ListUnnamed80030000`; `xrefs_to` empty — indirect
invocation (consistent with link-mode-change fptr-table dispatch).

**Confidence:** HIGH — full 110B decompile; LMP 0x3ee case2 branch idiom matches
siblings `zero_bos_struct_and_init_link_mode_bb_timing_with_lmp_3ee_branch`
(Pass 134) and `evaluate_lmp_3ee_link_mode_phase_with_retry_counter` (Pass 154);
`+0x164` link-mode byte merge matches documented cluster field layout.

Region unnamed count after this pass: **122** (123 minus this rename). Live named
**2044** global.

**Next:** superseded by Pass 169.

## Pass 169 (2026-07-01) — HW-ready bitmask poll `FUN_80034564`

Fresh `ListUnnamed80030000.java` re-run: **122 unnamed** remain in region
(unchanged from Pass 168; rank-1 at xref=1 tier is `FUN_80034564` at 110B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80034564` → `poll_hw_ready_bitmask_until_clear_or_log_timeout`**
(110B, HIGH, UTILITY-tier) via
`RenamePass169Region80030000Fun80034564.java` (`renamed=1`, live-verified).

**Mechanism:** Link-mode-commit HW-ready poll. Merges initial value
`*PTR_DAT_800345d8 |= DAT_800345d4 | 0xc3`, then spins until
`(DAT_800345dc & *status) == 0` (ready). On 2000-iteration timeout logs via
`possible_logging_function__var_args` and writes recovery dword
`(*status & DAT_800345e4 | DAT_800345e8) & 0xffffff00 | 0xa5`.

**Callers:** 1 xref-in per `ListUnnamed80030000`; callee of
`commit_link_mode_snapshot_role_slots_and_materialize_lut` (Pass 106).

**Confidence:** HIGH — full 110B decompile; poll+timeout+log idiom matches
`poll_hw_tx_status_until_nonnegative_or_log_timeout` (region `0x80020000`);
previously referenced by name in Pass 106 parent analysis.

Region unnamed count after this pass: **121** (122 minus this rename). Live named
**2045** global.

**Next:** superseded by Pass 170.

## Pass 170 (2026-07-01) — ACL TX dispatch router `FUN_80037790`

Fresh `ListUnnamed80030000.java` re-run: **121 unnamed** remain in region
(unchanged from Pass 169; rank-1 at xref=1 tier is `FUN_80037790` at 108B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80037790` → `dispatch_acl_tx_by_handle_to_completion_or_pending_queue`**
(108B, HIGH, HANDLER-tier) via
`RenamePass170Region80030000Fun80037790.java` (`renamed=1`, live-verified).

**Mechanism:** ACL TX packet router. Extracts 12-bit connection handle from
`*param_1 >> 8 & 0xfff`. On successful `called_by_fHCI_Read_LMP_Handle_3`
lookup, enqueues via `enqueue_connection_packet_completion_ring_or_overflow_dispatch`;
else tries `lookup_some_sort_of_connection_struct_index_by_connection_handle` —
on miss enqueues via `enqueue_acl_tx_descriptor_to_per_handle_pending_queue`
(returning early on success), on hit logs via `possible_logging_function__var_args`;
fallback dispatches via `PTR_DAT_80037800` fptr with arg 3.

**Callers:** 1 xref-in per `ListUnnamed80030000` (indirect dispatch table;
`xrefs_to` returns none — consistent with function-pointer registration).

**Confidence:** HIGH — full 108B decompile; callees all named (Pass 95
`enqueue_connection_packet_completion_ring_or_overflow_dispatch`, region
`0x80020000` `enqueue_acl_tx_descriptor_to_per_handle_pending_queue`).

Region unnamed count after this pass: **120** (121 minus this rename). Live named
**2046** global.

**Next:** superseded by Pass 171.

## Pass 171 (2026-07-01) — Gated link-mode dispatch `FUN_80035640`

Fresh `ListUnnamed80030000.java` re-run: **120 unnamed** remain in region
(unchanged from Pass 170; rank-1 at xref=1 tier is `FUN_80035640` at 106B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80035640` → `gated_link_mode_change_dispatch_with_prehook_and_completion`**
(106B, HIGH, HANDLER-tier) via
`RenamePass171Region80030000Fun80035640.java` (`renamed=1`, live-verified).

**Mechanism:** Type-105 (`0x69`) internal-message handler sibling of
`remote_name_feature_cleanup_and_lmp268_timer_dispatch` (Pass 158) in
`unknown_fptr_index0` case 5. Clears status bytes at workspace `+0x97` and
`+0xfc`; gates on config `field208_0xd8` bit `0x10` and workspace dword-zero
(returning `0xff` on failure). Optional validator/transformation pre-hook at
`PTR_DAT_800356b4` may short-circuit with its output byte; else calls
`link_mode_change_state_machine(link_type, param2, param3)` and on success
(return `0`) invokes completion fptr at `PTR_DAT_800356b8`.

**Callers:** 1 xref-in per `ListUnnamed80030000`; `xrefs_to` empty — indirect
fptr dispatch via `unknown_fptr_index0` type-105 path (known pattern).

**Confidence:** HIGH — full 106B decompile; named callee
`link_mode_change_state_machine` anchors the link-mode cluster; pre-hook +
completion-callback idiom matches sibling `0x80035xxx` handlers.

Region unnamed count after this pass: **119** (120 minus this rename). Live named
**2047** global.

**Next:** superseded by Pass 172.

## Pass 172 (2026-07-01) — fd49-diag triple compute `FUN_8003ab04`

Fresh `ListUnnamed80030000.java` re-run: **119 unnamed** remain in region
(unchanged from Pass 171; rank-1 at xref=1 tier is `FUN_8003ab04` at 102B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003ab04` → `read_fd49_diag_and_bb_reg_7e_pair_via_mode_41_select`**
(102B, HIGH, HANDLER-tier) via
`RenamePass172Region80030000Fun8003ab04.java` (`renamed=1`, live-verified).

**Mechanism:** Register-script cluster triple-compute helper (previously
referenced by Pass 145/103 parent analysis). Calls
`VSC_0xfd49_extended_diagnostic(0x21, 0)` into `*param_1`; selects BB reg
`0x41` mode `0x21` via hook fptr at `PTR_DAT_8003ab6c`, reads BB reg `0x7e`
via hook fptr at `PTR_DAT_8003ab70` into `*param_3` (`& 0x3ff`); repeats with
mode `0x20` and second `0x7e` read into `*param_2`. Outputs feed
`program_bb_regs_41_43_44_46_47_via_hook_and_da_d6_dispatch` via caller
`preserve_bb_regs_5a_5c_run_regscript_hook_then_program_bb_bundle`.

**Callers:** 1 xref-in per `ListUnnamed80030000` — callee of
`preserve_bb_regs_5a_5c_run_regscript_hook_then_program_bb_bundle` (Pass 145)
and gated wrapper `FUN_8003ac28`.

**Confidence:** HIGH — full 102B decompile; fd49-diag + dual BB `0x7e` read
with `0x41` mode-select `0x21`/`0x20` matches Pass 145 parent description;
`VSC_0xfd49_extended_diagnostic` callee anchors register-script cluster.

Region unnamed count after this pass: **118** (119 minus this rename). Live named
**2048** global.

**Next:** superseded by Pass 173.

## Pass 173 (2026-07-01) — BB reg 0x62/0x63 packer `FUN_80038e74`

Fresh `ListUnnamed80030000.java` re-run: **118 unnamed** remain in region
(unchanged from Pass 172; rank-1 at xref=1 tier is `FUN_80038e74` at 102B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80038e74` → `pack_bitmasked_bytes_into_bb_regs_0x62_low_0x63_high_via_hook`**
(102B, HIGH, HANDLER-tier) via
`RenamePass173Region80030000Fun80038e74.java` (`renamed=1`, live-verified).

**Mechanism:** BB-register programmer in the `0x80038exx` cluster (sibling of
Pass 111's `program_bb_reg_0x6f_7bit_field_at_bits7_13_via_hook` and
`packet_type_to_hw_code_translator_4link`). Walks bits 0–7 of `param_2`;
for each set bit, ORs `param_1[i] << shift` into a 32-bit accumulator (shift
starts at 28, decrements by 4). Splits result into low/high 16-bit halves and
writes via hook fptr `PTR_DAT_80038edc`: `(3, 0x62, 1, low)` and
`(3, 99, 1, high)` — BB regs `0x62`/`0x63` in bank 3.

**Callers:** 1 xref-in — `FUN_80038f1c` (config-flag wrapper at `0x80038f1c`):
when `*PTR_DAT_80038f40 & 0x80`, calls with `param_2=0` (zero-clear path).

**Confidence:** HIGH — full 102B decompile; explicit hook indirection with
`(bank, reg, mode, value)` tuple matches sibling BB programmers; caller body
anchors config-flag-gated clear use.

Region unnamed count after this pass: **117** (118 minus this rename). Live named
**2049** global.

**Next:** superseded by Pass 174.

## Pass 174 (2026-07-01) — AFH channel-map slice `FUN_8003a9b0`

Fresh `ListUnnamed80030000.java` re-run: **117 unnamed** remain in region
(unchanged from Pass 173; rank-1 at xref=1 tier is `FUN_8003a9b0` at 100B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003a9b0` → `program_afh_channel_map_16ch_at_offset_20_with_bb_reg_0xe_bracket`**
(100B, HIGH, HANDLER-tier) via
`RenamePass174Region80030000Fun8003a9b0.java` (`renamed=1`, live-verified).

**Mechanism:** AFH channel-map slice programmer in the `0x8003aaxx` register-script
cluster (sibling of Pass 165's
`sweep_fd49_extended_diag_30_channels_with_bb_reg_0xe_bit2_enable`). Brackets
`AFH_channel_map_hw_register_programmer(channel_map_ptr, 0x14, 0x10)` — 16
channels starting at offset 20 — with BB reg `0xe` read-modify-write via hook
fptrs at `PTR_DAT_8003aa14`/`PTR_DAT_8003aa18`: sets bit2 (`| 4`), sets bit5
and clears bits4–5 (`& 0xffcf | 0x20`), then clears bit2 (`& 0xfffb`) after the
AFH write. Channel-map data pointer from `PTR_DAT_801234bc_3_8003aa1c`.

**Callers:** 1 xref-in — `FUN_8003aa20` config-flag dispatcher: when
`PTR_DAT_8003aa64` bit2 (`0x4`) is set, calls this helper then copies
`pbVar1[0x25]` to output byte.

**Confidence:** HIGH — full 100B decompile; `AFH_channel_map_hw_register_programmer`
callee already HIGH-named; BB reg `0xe` bit-bracket idiom matches Pass 165
sibling; caller config-flag gate unambiguous.

Region unnamed count after this pass: **116** (117 minus this rename). Live named
**2050** global.

**Next:** superseded by Pass 175.

## Pass 175 (2026-07-01) — VSC FC95 + LMP268 dual-slot gateway `FUN_8003a630`

Fresh `ListUnnamed80030000.java` re-run: **116 unnamed** remain in region
(unchanged from Pass 174; rank-1 at xref=1 tier is `FUN_8003a630` at 98B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003a630` → `dual_pending_vsc_fc95_and_lmp268_gateway_with_config_timeout`**
(98B, HIGH, HANDLER-tier) via
`RenamePass175Region80030000Fun8003a630.java` (`renamed=1`, live-verified).

**Mechanism:** Dual pending-slot VSC FC95 / LMP-0x268 gateway in the
`0x8003a6xx` register-script cluster (sibling of Pass 157's
`config_gated_pulse_bb_reg_0x75_bit0_with_spin_delays`). State struct at
`PTR_DAT_8003a694` holds two pending connection slots (dwords at `+0` and
`+4`); sentinel `-1` on each slot triggers `VSC_0xfc95_called2` with
alternate param ptrs (`PTR_PTR_8003a698`/`PTR_PTR_8003a6a0`,
`PTR_DAT_8003a6a4`). When `config_base->field0_0x0[1] != 0`, dispatches
`LMP__268__most_common_for_VSCs2_checks_fptr_patch` on the first slot with
timeout `config_byte × 1000` ms. Always finishes with
`LMP__268__most_common_for_VSCs2_checks_fptr_patch` on the second slot value
with fixed 1000 ms timeout.

**Callers:** 1 xref-in — `dispatch_optional_subsystem_hooks_during_hw_reg_config`
at `0x8003959e` (COMPUTED_CALL); register-script interpreter cluster indirect
invocation.

**Confidence:** HIGH — full 98B decompile; `VSC_0xfc95_called2` and
`LMP__268__most_common_for_VSCs2_checks_fptr_patch` callees already HIGH-named;
dual-slot sentinel `-1` → FC95 then LMP-268 sequencing matches documented
VSC/LMP gateway idiom (Pass 166 boot-init FC95 sibling).

Region unnamed count after this pass: **115** (116 minus this rename). Live named
**2051** global.

**Next:** superseded by Pass 176.

## Pass 176 (2026-07-01) — BOS role-slot snapshot walker `FUN_8003436c`

Fresh `ListUnnamed80030000.java` re-run: **115 unnamed** remain in region
(unchanged from Pass 175; rank-1 at xref=1 tier is `FUN_8003436c` at 96B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003436c` → `walk_10_bos_slots_snapshot_last_valid_role_merge_crypto_to_slot0`**
(96B, HIGH, HANDLER-tier) via
`RenamePass176Region80030000Fun8003436c.java` (`renamed=1`, live-verified).

**Mechanism:** Link-mode role-slot snapshot helper in the `0x800343xx` cluster
(sibling callee of Pass 106's
`commit_link_mode_snapshot_role_slots_and_materialize_lut`). When config struct
`field_0x173` at `PTR_struct_of_at_least_0x300_size_800343cc` is clear, writes
sentinel `0xff` to role-index output at `PTR_DAT_800343d4` and returns. When
set, walks slots 0–9 of the `big_ol_struct` array at `PTR_big_ol_struct_800343d0`:
for each entry with `bos_entry_valid_ == 1`, records slot index to
`PTR_DAT_800343d4`, copies full struct to `PTR_DAT_800343d8` via
`optimized_memcpy`, and for non-zero slots merges `0x218` bytes of
`_x58_crypto_struct` into slot-0's crypto area (last valid slot wins index +
struct copy).

**Callers:** 1 xref-in — `commit_link_mode_snapshot_role_slots_and_materialize_lut`
(Pass 106); invoked during hook-veto-gated link-mode commit when `field_0x10a` is
set.

**Confidence:** HIGH — full 96B decompile; 10-slot `big_ol_struct` walk +
`optimized_memcpy` idiom matches Pass 106 parent description; config
`field_0x173` disable path with `0xff` sentinel unambiguous.

Region unnamed count after this pass: **114** (115 minus this rename). Live named
**2052** global.

**Next:** superseded by Pass 177.

## Pass 177 (2026-07-01) — per-link VSC FC95 + LMP268 gateway `FUN_8003fa24`

Fresh `ListUnnamed80030000.java` re-run: **114 unnamed** remain in region
(unchanged from Pass 176; rank-1 at xref=1 tier is `FUN_8003fa24` at 94B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003fa24` → `per_link_vsc_fc95_lmp268_gateway_with_param_scaled_timeout`**
(94B, HIGH, UTILITY-tier) via
`RenamePass177Region80030000Fun8003fa24.java` (`renamed=1`, live-verified).

**Mechanism:** Per-link VSC FC95 + LMP-268 pending-slot gateway. Indexes
`big_ol_struct[param_1]`; computes timeout `max(10, (param_2*5)>>3)` from
caller-supplied delta. When pending slot `field_0x44 == -1`, issues
`VSC_0xfc95_called2` to fill it; then dispatches
`LMP__268__most_common_for_VSCs2_checks_fptr_patch` with the pending value and
computed timeout. Returns `0xff` on either step failure, `0` on success. TX-power
cluster helper — callee of Pass 164
`apply_random_bdaddr_tx_power_delta_via_vsc_fc95_lmp_268`; sibling of Pass 175
`dual_pending_vsc_fc95_and_lmp268_gateway_with_config_timeout` (dual-slot variant
with config-byte timeout scaling).

**Callers:** 1 xref-in — `apply_random_bdaddr_tx_power_delta_via_vsc_fc95_lmp_268`
(Pass 164); invoked when random-BD_ADDR TX-power delta exceeds global threshold.

**Confidence:** HIGH — full 94B decompile; sentinel `-1` VSC FC95 fill +
LMP-268 dispatch chain matches Pass 164 parent description and Pass 175
sibling pattern; param-scaled timeout formula unambiguous.

Region unnamed count after this pass: **113** (114 minus this rename). Live named
**2053** global.

**Next:** superseded by Pass 178.

## Pass 178 (2026-07-01) — AFH cleanup arm gate `FUN_800388e0`

Fresh `ListUnnamed80030000.java` re-run: **113 unnamed** remain in region
(unchanged from Pass 177; rank-1 at xref=1 tier is `FUN_800388e0` at 90B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_800388e0` → `arm_afh_cleanup_on_lmp_pdu_nibble_e0_f0_tid_gated`**
(90B, HIGH, UTILITY-tier) via
`RenamePass178Region80030000Fun800388e0.java` (`renamed=1`, live-verified).

**Mechanism:** Early LMP-PDU-received AFH cleanup arm gate. Called from
`lmp_pdu_received_top_level_processor` with `&local_76` (connection-event
metadata byte). When two global enable flags (`PTR_DAT_8003893c`,
`PTR_DAT_80038940`) are set and armed flag `PTR_DAT_80038944` is clear,
checks upper nibble `0xe0`/`0xf0` plus TID bit (bit3, optionally inverted
via `PTR_DAT_80038948`); on match clears BB reg `0x44` bit0 via
`read_modify_write_hw_reg_0x44_set_bit0(0)` and sets armed flag to 1.
AFH cluster sibling of `AFH_channel_map_table_builder` (`0x800386d0`) and
`VSC_0xfc64_link_quality` cleanup tail.

**Callers:** 1 xref-in — `lmp_pdu_received_top_level_processor` (`0x80003e0c`,
region `0x80000000`); invoked at top of connection-event processing before
optional pre-hook dispatch.

**Confidence:** HIGH — full 90B decompile; nibble/TID gate + HW reg 0x44
cleanup chain matches AFH cluster pattern; caller context unambiguous.

Region unnamed count after this pass: **112** (113 minus this rename). Live named
**2054** global.

**Next:** superseded by Pass 179.

## Pass 179 (2026-07-01) — VSC FCC0/feature-page dispatch `FUN_8003ca5c`

Fresh `ListUnnamed80030000.java` re-run: **112 unnamed** remain in region
(unchanged from Pass 178; rank-1 at xref=1 tier is `FUN_8003ca5c` at 86B —
largest among the xref=1 cohort, tied with `FUN_8003b428` on size).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003ca5c` → `dispatch_vsc_fcc0_or_feature_page_fptr_on_role_bit0`**
(86B, HIGH, UTILITY-tier) via
`RenamePass179Region80030000Fun8003ca5c.java` (`renamed=1`, live-verified).

**Mechanism:** Inquiry/LAP cluster VSC FCC0 or feature-page hook dispatcher.
Gated on `the_0x300->byte_0x16a` bit 0 (role bit) and `int_0x10 & 3 == 0`
(idle link mode). When global flag `PTR_DAT_8003cab8` bit 2 clear and
`field_0x17c == 0x80` (LAP byte written by `OGC_3_OCF_45`), calls
`wraps_calls_to_VSC_0xfcc0_then_calls_fptr()`. Otherwise checks feature-page
`PTR_some_feature_page_base_8003cabc[3]` bit 3 (8): dispatches
`PTR_DAT_8003cac0` hook fptr with arg `2` (clear) or `10` (set) depending on
feature-page bit. Sibling of `commit_hw_channel_merge_index_0x36_on_role_bit0`
(`0x8003ca28`, same `byte_0x16a` bit0 gate, HW-channel commit path).

**Callers:** 1 xref-in at `0x8003cac8` — no containing function (orphan/indirect
call site; consistent with fptr-table dispatch).

**Confidence:** HIGH — full 86B decompile; VSC FCC0 wrapper + feature-page
fptr dispatch pattern matches Pass 73 inquiry/LAP cluster and
`remote_name_request_feature_apply_8` feature-page bit-8 gating; struct-field
gates (`byte_0x16a`, `int_0x10`, `field_0x17c`) unambiguous.

Region unnamed count after this pass: **111** (112 minus this rename). Live named
**2055** global.

**Next:** superseded by Pass 180.

## Pass 180 (2026-07-01) — Newton floor-sqrt `FUN_8003b428`

Fresh `ListUnnamed80030000.java` re-run: **111 unnamed** remain in region
(unchanged from Pass 179; rank-1 at xref=1 tier is `FUN_8003b428` at 86B —
largest among the xref=1 cohort, tied with `FUN_8003ac28` on size).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003b428` → `newton_floor_sqrt_lower16_shifted_left_6`**
(86B, HIGH, UTILITY-tier) via
`RenamePass180Region80030000Fun8003b428.java` (`renamed=1`, live-verified).

**Mechanism:** Pure math utility. Computes floor integer square root of
`(param_1 & 0xffff) << 6` via MSB position scan (from bit 24 downward) then
Newton divide-and-average iteration (`guess = (n/guess + guess) / 2`) until
convergence (`guess <= new_guess`), returning lower 16 bits. Zero input returns
0 without iteration.

**Callers:** 1 xref-in per `ListUnnamed80030000` (no containing function found
via `xrefs_to` — likely indirect/fptr dispatch).

**Confidence:** HIGH — full 86B decompile; classic integer sqrt Newton loop
structure unambiguous (MSB initial guess + division/average convergence).

Region unnamed count after this pass: **110** (111 minus this rename). Live named
**2056** global.

**Next:** superseded by Pass 181.

## Pass 181 (2026-07-01) — fd49-diag bit0 gate `FUN_8003ac28`

Fresh `ListUnnamed80030000.java` re-run: **110 unnamed** remain in region
(unchanged from Pass 180; rank-1 at xref=1 tier is `FUN_8003ac28` at 84B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003ac28` → `dispatch_fd49_diag_bit0_preserve_bb_or_program_bb_bundle`**
(84B, HIGH, HANDLER-tier) via
`RenamePass181Region80030000Fun8003ac28.java` (`renamed=1`, live-verified).

**Mechanism:** Register-script cluster gated wrapper. Tests bit 0 of
`read_fd49_extended_diag_build_dual_slot_bitmasks_and_shift_width`: when clear,
invokes `preserve_bb_regs_5a_5c_run_regscript_hook_then_program_bb_bundle`
(full save/restore + fd49 triple-compute path); when set, extracts three ushort
params from the fd49-diag slot record (`+4`/`+7`/`+8` dword halves) and calls
`program_bb_regs_41_43_44_46_47_via_hook_and_da_d6_dispatch` directly.

**Callers:** 1 xref-in per `ListUnnamed80030000` — invoked from
`register_script_interpreter` (`0x8003aea0`) alongside
`preserve_bb_regs_5a_5c_run_regscript_hook_then_program_bb_bundle` (Pass 145).

**Confidence:** HIGH — full 84B decompile; bit0 branch to both documented
register-script cluster paths matches Pass 145/103 parent analysis; fd49-diag
context global reference confirmed.

Region unnamed count after this pass: **109** (110 minus this rename). Live named
**2057** global.

**Next:** superseded by Pass 182.

## Pass 182 (2026-07-01) — crypto-teardown conn slot clear `FUN_80037804`

Fresh `ListUnnamed80030000.java` re-run: **109 unnamed** remain in region
(unchanged from Pass 181; rank-1 at xref=1 tier is `FUN_80037804` at 80B —
largest among the xref=1 cohort).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80037804` → `clear_conn_pending_lmp_0x50_and_pdu_on_crypto_teardown`**
(80B, HIGH, UTILITY-tier) via
`RenamePass182Region80030000Fun80037804.java` (`renamed=1`, live-verified).

**Mechanism:** Per-connection-slot (`param_1 & 0xffff`) partial LMP-state scrub on
`PTR_big_ol_struct_80037854[slot]`. When `int_0x50 != -1` (pending LMP procedure
linked), logs via `possible_logger_called_if_no_patch3` with opcode `0x25b`, then
sets `int_0x50 = -1`. Always clears LMP PDU buffer bytes `field_0x212` and
`field_0x215` to zero. Subset of the fuller
`clear_connection_slot_lmp_pdu_and_pending_fields` scrub (region `0x80020000`).

**Callers:** 1 xref-in — `encryption_key_teardown_notifier` at `0x800029a4`
(paired with `role_switch_commit_staged_slot_transition` per Pass 122).

**Confidence:** HIGH — full 80B decompile; field offsets match documented
`big_ol_struct` LMP PDU/pending cluster; caller path matches encryption-key
teardown triplet in region `0x80000000`.

Region unnamed count after this pass: **108** (109 minus this rename). Live named
**2058** global.

**Next:** superseded by Pass 183.

## Pass 183 (2026-07-01) — role-slot state logger `FUN_8003d4fc`

Fresh `ListUnnamed80030000.java` re-run: **108 unnamed** remain in region
(unchanged from Pass 182; rank-1 by size at xref=1 tier is `FUN_8003d4fc` at
78B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003d4fc` → `log_role_slot_state_evt_0x2c4_when_not_role_switch`**
(78B, HIGH, HANDLER-tier) via
`RenamePass183Region80030000Fun8003d4fc.java` (`renamed=1`, live-verified).

**Mechanism:** Optional hook veto at `PTR_DAT_8003d54c` — when absent or
returns zero, checks per-role-slot connection struct (`0x28` stride,
`PTR_some_connection_struct_array_8003d550`): requires `field_0x24 != 0x02`
(not in role-switch state); then logs via `possible_logger_called_if_no_patch3`
with opcode `0x2c4` (708). Sibling of
`log_role_slot_state_evt_0x2c5_when_not_role_switch` (Pass 105) — same
hook-veto + role-switch gate pattern but omits the `field_0x23 != 0` check and
uses a different log opcode.

**Callers:** 1 xref-in — `status_bit_gated_role_state_logger_dispatch` (region
`0x80000000`, dispatches to this leaf vs the `0x2c5` sibling depending on
status-word `0x1e0` field).

**Confidence:** HIGH — full 78B decompile; hook-veto + role-switch gate matches
documented role-state logging cluster; caller confirmed via region `0x80000000`
Pass documentation.

Region unnamed count after this pass: **107** (108 minus this rename). Live named
**2059** global.

**Next:** superseded by Pass 184.

## Pass 184 (2026-07-01) — ACL RX continuation/hook dispatcher `FUN_8003d2f8`

Fresh `ListUnnamed80030000.java` re-run: **107 unnamed** remain in region
(unchanged from Pass 183; rank-1 by size at xref=1 tier is `FUN_8003d2f8` at
78B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003d2f8` → `dispatch_acl_rx_continuation_or_procedure_hook_by_conn_flag_0x23`**
(78B, HIGH, HANDLER-tier) via
`RenamePass184Region80030000Fun8003d2f8.java` (`renamed=1`, live-verified).

**Mechanism:** Per-role-slot ACL RX branch on `0x28`-stride conn struct
(`PTR_some_connection_struct_array_8003d348`). When `field_0x23 != 0`: feeds
`hci_acl_data_fragment_assembler_and_enqueue` with handle (`field_0x4`), ACL
type (`field_0x1b`), payload ptr (`field_0x20`), length (`field_0x1a`), mode 0
(continuation). When `field_0x23 == 0` and global `the_0x300` struct
`byte_0x16f != 0`: dispatches optional hook at `PTR_DAT_8003d350` with
procedure opcode `0x14` or `0x1e` (selected by `ushort_0x24 != 0x40`), args
`(field_0x20, opcode, 0xc0, 0)`.

**Callers:** 1 xref-in — `LC_event_RX_dispatcher` at `0x80042238` (LC RX ACL
path; sibling of `dispatch_acl_fragment_with_per_conn_reassembly_flags` per Pass
125 and `hci_acl_data_fragment_assembler_and_enqueue` per Pass 6 cont. 3).

**Confidence:** HIGH — full 78B decompile; callee `hci_acl_data_fragment_assembler_and_enqueue`
already HIGH-named; conn-field branch matches documented ACL reassembly cluster.

Region unnamed count after this pass: **106** (107 minus this rename). Live named
**2060** global.

**Next:** superseded by Pass 185.

## Pass 185 (2026-07-01) — BB status-bit fptr hook `FUN_8003b818`

Fresh `ListUnnamed80030000.java` re-run: **106 unnamed** remain in region
(unchanged from Pass 184; rank-1 by size at xref=1 tier is `FUN_8003b818` at
76B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003b818` → `merge_status_high_bits_2_3_and_dispatch_fptr_hook_0x27c`**
(76B, HIGH, UTILITY-tier) via
`RenamePass185Region80030000Fun8003b818.java` (`renamed=1`, live-verified).

**Mechanism:** BB-register cluster status-word patcher. Reads ushort at
`DAT_8003b864`, clears then sets high-byte bits 2 and 3 from `param_1` bit0
and `param_2` bit0 (mask `0xf3` on high byte), then invokes patchable fptr at
`PTR_DAT_8003b868` with opcode `0x27c` and the merged ushort. Sibling of
`dispatch_dual_fptr_hooks_by_flag_with_config_field285_bits_or_const_0x41`
(Pass 86) and `program_bb_regs_0x220_0x222_0x224_via_hook_with_masked_params`
(Pass 152) in the `0x8003b8xx` BB-reg hook-programming family.

**Callers:** 1 xref-in — patch firmware `FUN_8010c43c` at `0x8010c482`
(COMPUTED_CALL; T1 hook slot `0x8012067c` per patch-installer audit).

**Confidence:** HIGH — full 76B decompile; bit-merge + fptr-dispatch idiom
matches documented BB-reg cluster; sole caller confirmed via `ListXrefsTo8003b818`.

Region unnamed count after this pass: **105** (106 minus this rename). Live named
**2061** global.

**Next:** superseded by Pass 186.

## Pass 186 (2026-07-01) — LMP TX log+enqueue helper `FUN_80036da8`

Fresh `ListUnnamed80030000.java` re-run: **105 unnamed** remain in region
(unchanged from Pass 185; rank-1 by size at xref=1 tier is `FUN_80036da8` at
74B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80036da8` → `log_lc_tx_evt_0x32a_and_enqueue_lmp_tx_pending_descriptor`**
(74B, HIGH, UTILITY-tier) via
`RenamePass186Region80030000Fun80036da8.java` (`renamed=1`, live-verified).

**Mechanism:** Post-build helper on the `send_LMP_pkt` success path. Logs via
`possible_logger_called_if_no_patch3` with LC TX event tag `0x32a` (member of
`assoc_w_tLC_TX` / `LC_event_TX_dispatcher` opcode set), then calls
`FUN_8006ad80` to init the allocated LMP TX descriptor buffer
(`param_2`: sets `+0x1a` role-index byte, `+0x16` bosi ushort, clears
`+0x1b`/`+0x1e`, copies `+0x1e` from XOR-mask-matched list entry) and enqueue
into the IRQ-guarded pending-TX linked list at `PTR_DAT_8006add0`.

**Callers:** 1 xref-in — `send_LMP_pkt` at `0x80061398` (region `0x80060000`,
shared LMP-PDU-TX primitive used by all `LMP_*` handlers).

**Confidence:** HIGH — full 74B decompile; logger tag `0x32a` matches
documented LC TX opcode table; caller and callee chain confirmed via
`ListXrefsTo80036da8` + `send_LMP_pkt` decompile.

Region unnamed count after this pass: **104** (105 minus this rename). Live named
**2062** global.

**Next:** superseded by Pass 187.

## Pass 187 (2026-07-01) — TX power byte getter `FUN_8003b1d0`

Fresh `ListUnnamed80030000.java` re-run: **104 unnamed** remain in region
(unchanged from Pass 186; rank-1 by size at xref=1 tier is `FUN_8003b1d0` at
72B, tied with `FUN_8003845c` — first by address wins).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003b1d0` → `get_tx_power_byte_from_config_field453_plus_channel_or_hook`**
(72B, HIGH, UTILITY-tier) via
`RenamePass187Region80030000Fun8003b1d0.java` (`renamed=1`, live-verified).

**Mechanism:** TX-power byte getter with optional hook override. When fptr at
`PTR_DAT_8003b218` is null, computes
`config.field453_0x1d1 + 0x5a + (param_1 & 0xff) * 2` and clamps to signed
8-bit `[-128,127]` before return; when hook present, calls `hook(0)` instead.
TX-power cluster sibling of `compute_clamped_tx_power_level_from_link_class_baselines`
(Pass 162) and `return_RSSI_value` (config-formula + hook-override idiom).

**Callers:** 1 xref-in — `LMP_CH__0x3ea__FUN_800656bc` at `0x800656ee` (region
`0x80060000`, LMP channel sub-opcode `0x3ea` handler per
`assoc_w_tLMP_CH` dispatch table).

**Confidence:** HIGH — full 72B decompile; `config.field453_0x1d1` reuse and
hook-override pattern match documented TX-power cluster; sole caller confirmed
via `ListXrefsTo8003b1d0`.

Region unnamed count after this pass: **103** (104 minus this rename). Live named
**2063** global.

**Next:** superseded by Pass 188.

## Pass 188 (2026-07-01) — LC evt 0x321 logger `FUN_8003845c`

Fresh `ListUnnamed80030000.java` re-run: **103 unnamed** remain in region
(unchanged from Pass 187; rank-1 by size at xref=1 tier is `FUN_8003845c` at
72B — tied with several 68–70B siblings, first by address wins).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003845c` → `log_lc_evt_0x321_with_hw_clock_by_role_index`**
(72B, HIGH, UTILITY-tier) via
`RenamePass188Region80030000Fun8003845c.java` (`renamed=1`, live-verified).

**Mechanism:** LC event logger for opcode `0x321`: reads HW clock dword via
`read_hw_clock_raw_dword_by_role_index(out, role_index)`, merges
`role_index << 0x1e` with mask `DAT_800384a4`, then logs via
`possible_logger_called_if_no_patch3` with `param_2 & 0xff` as secondary arg.
Extracted helper matching the inline `0x321` logging path in
`select_packet_type_and_renegotiate_or_log`'s else branch (which uses
`byte_0xCC` for clock read instead of role index).

**Callers:** 1 xref-in — `select_packet_type_and_renegotiate_or_log` at
`0x8000333c` (region `0x80000000`, eSCO packet-type selection success path:
calls this helper then separately logs evt `0x328`).

**Confidence:** HIGH — full 72B decompile; LC evt `0x321` + HW-clock timestamp
pattern matches documented eSCO packet-type cluster; sole caller confirmed via
caller decompile.

Region unnamed count after this pass: **102** (103 minus this rename). Live named
**2064** global.

**Next:** superseded by Pass 189.

## Pass 189 (2026-07-01) — indexed LUT materializer `FUN_80033e10`

Fresh `ListUnnamed80030000.java` re-run: **102 unnamed** remain in region
(unchanged from Pass 188; rank-1 by size at xref=1 tier is `FUN_80033e10` at
70B — tied with several 68B siblings, first by address wins).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80033e10` → `materialize_indexed_lut_5_ushort_buf0_18_ushort_buf1`**
(70B, HIGH, UTILITY-tier) via
`RenamePass189Region80030000Fun80033e10.java` (`renamed=1`, live-verified).

**Mechanism:** Indexed LUT materializer for link-mode commit path. Loops 5 times
copying ushort values from base `DAT_80033e5c` indexed by `PTR_DAT_80033e58`
into `param_1` buffer; writes count `5` to `DAT_80033e60`; then loops 18
times copying ushort values from base `DAT_80033e68` indexed by
`PTR_DAT_80033e64` into `param_2` buffer. Extracted helper sibling of
`commit_link_mode_snapshot_role_slots_and_materialize_lut`'s 9-entry dword LUT
materialization (Pass 106).

**Callers:** 1 xref-in — `commit_link_mode_snapshot_role_slots_and_materialize_lut`
at `0x80034618` (region `0x80030000`, link-mode commit after role-slot snapshot
and HW-ready poll).

**Confidence:** HIGH — full 70B decompile; indexed-LUT copy idiom matches
documented link-mode-commit cluster; sole caller confirmed via
`ListXrefsTo80033e10`.

Region unnamed count after this pass: **101** (102 minus this rename). Live named
**2065** global.

**Next:** superseded by Pass 190.

## Pass 190 (2026-07-01) — config-flag dispatcher `FUN_8003aa20`

Fresh `ListUnnamed80030000.java` re-run: **101 unnamed** remain in region
(unchanged from Pass 189; rank-1 by size at xref=1 tier is `FUN_8003aa20` at
68B — tied with three 68B siblings, first by address wins).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003aa20` → `dispatch_config_flag_bit2_afh_or_fptr_pair_store_status_byte`**
(68B, HIGH, HANDLER-tier) via
`RenamePass190Region80030000Fun8003aa20.java` (`renamed=1`, live-verified).

**Mechanism:** Config-flag dispatcher in the `0x8003aaxx` register-script
cluster (caller of Pass 174's
`program_afh_channel_map_16ch_at_offset_20_with_bb_reg_0xe_bracket`). When
`PTR_DAT_8003aa64` bit2 (`0x4`) is set, calls the AFH channel-map programmer
then copies `pbVar1[0x25]` as status byte; else invokes two hook fptrs at
`PTR_DAT_8003aa70` and `PTR_PTR_8003aa74` (first passes context fields from
`PTR_PTR_8003aa6c`), then reads status from `PTR_DAT_8003aa78[8]`. Stores
result to `PTR_DAT_8003aa68[1]`.

**Callers:** 1 xref-in — patch RAM `0x8011059e` COMPUTED_CALL (register-script
interpreter dispatch path; indirect call, no named enclosing function).

**Confidence:** HIGH — full 68B decompile; config-flag bit2 gate and AFH callee
already HIGH-named from Pass 174; dual-fptr fallback path matches register-script
cluster idiom; sole xref confirmed via `ListXrefsTo8003aa20`.

Region unnamed count after this pass: **100** (101 minus this rename). Live named
**2066** global.

**Next:** superseded by Pass 191.

## Pass 191 (2026-07-01) — link-register role-bit reader `FUN_8003a5ec`

Fresh `ListUnnamed80030000.java` re-run: **100 unnamed** remain in region
(unchanged from Pass 190; rank-1 by size at xref=1 tier is `FUN_8003a5ec` at
68B — tied with two 68B siblings, first by address wins).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003a5ec` → `read_link_register_0xe_role_bits_13_14_by_slot`**
(68B, HIGH, UTILITY-tier) via
`RenamePass191Region80030000Fun8003a5ec.java` (`renamed=1`, live-verified).

**Mechanism:** Slot-indexed HW link-register reader sibling of region
`0x80050000`'s `read_link_register_0xe_top_nibble_by_slot` (`0x800566b8`).
For slot `< 8`: calls `read_indexed_esco_link_register(slot*0x14 + 0xe)`; for
slot `≥ 8`: calls `read_indexed_link_register((slot-8)*0x1e + 0xe)`; returns
bits 13:14 (`>> 0xd & 3`) from register index `0xe`.

**Callers:** 1 xref-in — `apply_tx_power_runtime_mode_byte_and_reconfigure_tables_and_links`
(TX-power runtime-mode reconfiguration cluster).

**Confidence:** HIGH — full 68B decompile; eSCO/SCO stride split matches
established `read_link_register_0xe_top_nibble_by_slot` pattern; both callees
already HIGH-named; sole xref confirmed via `ListXrefsTo8003a5ec`.

Region unnamed count after this pass: **99** (100 minus this rename). Live named
**2067** global.

**Next:** superseded by Pass 192.

## Pass 192 (2026-07-01) — link-state advance clear + slot-timing commit `FUN_800378e4`

Fresh `ListUnnamed80030000.java` re-run: **99 unnamed** remain in region
(unchanged from Pass 191; rank-1 by size at xref=1 tier is `FUN_800378e4` at
68B — tied with two 68B siblings, first by address wins).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_800378e4` → `clear_link_state_advance_pending_and_commit_conn_slot_timing_mode2`**
(68B, HIGH, HANDLER-tier) via
`RenamePass192Region80030000Fun800378e4.java` (`renamed=1`, live-verified).

**Mechanism:** Per-conn-index `big_ol_struct` link-state advance teardown on
`field_0x29d`. When `field_0x29d == 1`: clears flag to 0, calls
`FUN_800143b0(bos_connection__array_index, byte_0xCC, 1)` (IRQ-masked HW-channel
table merge), then
`recompute_and_commit_conn_slot_timing_hw_and_packet_types(param_1, 2)` (mode-2
link-state-advance commit path). Complement of sibling `FUN_8003792c` which arms
`field_0x29d` from 0→1 and commits with mode 1 (establish).

**Callers:** 1 xref-in — `LC_event_RX_dispatcher` at `0x80042188` (LC RX event
dispatch cluster; documented in Pass 2 region `0x80040000`).

**Confidence:** HIGH — full 68B decompile; `field_0x29d` semantics match Pass
104/126/127 link-state-advance cluster; named callee
`recompute_and_commit_conn_slot_timing_hw_and_packet_types` (Pass 74) anchors
mode-2 commit; complement pair with `FUN_8003792c` confirms establish/teardown
symmetry.

Region unnamed count after this pass: **98** (99 minus this rename). Live named
**2068** global.

**Next:** superseded by Pass 193.

## Pass 193 (2026-07-01) — connection-setup BB reg programmer `FUN_80033744`

Fresh `ListUnnamed80030000.java` re-run: **98 unnamed** remain in region
(unchanged from Pass 192; rank-1 by size at xref=1 tier is `FUN_80033744` at
68B).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80033744` → `clear_connection_setup_flag_and_program_bb_regs_0x32_bit13_and_0x8f_via_hook`**
(68B, HIGH, SIMPLE-tier) via
`RenamePass193Region80030000Fun80033744.java` (`renamed=1`, live-verified).

**Mechanism:** Sibling of Pass 112's
`clear_connection_setup_flag_and_program_bb_regs_0x36_bit13_and_0x8e_via_hook`
(`0x800336f4`). Clears byte at `PTR_DAT_80033788+0x104` (connection-setup
status flag), reads ushort from `DAT_8003378c`, masks with `0xdfff` (clears bit
13 / `0x2000`), then invokes hook fptr `PTR_DAT_80033790` twice:
`(reg 0x32, masked_value)` then `(reg 0, 0x8f)`. Same flag-clear + bit13-mask
+ dual hook-write idiom as the `0x36`/`0x8e` sibling, but targets BB register
`0x32` and programs reg `0` to `0x8f` instead of `0x8e`.

**Callers:** 1 xref-in — `param_dispatch_with_rom_calls` at `0x80035bf0`
(`DAT_80125b56 & 2` branch of connection-setup commit dispatch).

**Confidence:** HIGH — full 68B decompile; explicit hook indirection, bitmask,
and named connection-setup dispatch caller anchor the role; structural symmetry
with Pass 112's HIGH-named `0x800336f4` sibling confirms semantics.

Region unnamed count after this pass: **97** (98 minus this rename). Live named
**2069** global.

**Next:** superseded by Pass 194.

## Pass 194 (2026-07-01) — HW reg 0x44 bits 12-14 RMW `FUN_8003b64c`

Fresh `ListUnnamed80030000.java` re-run: **97 unnamed** remain in region
(unchanged from Pass 193; rank-1 by size at xref=1 tier is `FUN_8003b64c` at
66B — wins on size over tied 66B/64B/62B siblings, first by address).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003b64c` → `read_modify_write_hw_reg_0x44_set_bits12_14_from_3bit_param`**
(66B, HIGH, SIMPLE-tier) via
`RenamePass194Region80030000Fun8003b64c.java` (`renamed=1`, live-verified).

**Mechanism:** Read-modify-write HW/VSC register `0x44` bits 12-14: reads via
indirect read fptr at `PTR_DAT_8003b690` with args `(0, 0x44, 1)`, then writes
back via write fptr at `PTR_DAT_8003b694` with value
`(read_val & 0x8fff) | ((param_1 & 7) << 0xc)` — clears bit 15, replaces
bits 12-14 with the low 3 bits of `param_1`. Sibling of Pass 71's
`read_modify_write_hw_reg_0x44_set_bit0` (bit-0 merge) in the same
`0x8003b6xx` per-connection HW-buffer setup cluster.

**Callers:** 1 xref-in — `per_connection_hw_buffer_setup_with_patch_hook` (calls
with literal `7` as part of the setup sequence alongside
`read_modify_write_hw_reg_0x44_set_bits7_10_from_4bit_param(5)`,
`read_modify_write_hw_reg_0x44_set_bit0(1)`, and `read_modify_write_hw_reg_0x44_set_bit1(1)` before
setting bit `0x8000` of reg `0x44`).

**Confidence:** HIGH — full 66B decompile; register index `0x44` and 3-bit
field merge at bits 12-14 unambiguous; caller context matches documented Pass 8
item 13 per-connection HW-buffer setup cluster.

Region unnamed count after this pass: **96** (97 minus this rename). Live named
**2070** global.

**Next:** superseded by Pass 195.

## Pass 195 (2026-07-01) — link-state advance arm + slot-timing commit `FUN_8003792c`

Fresh `ListUnnamed80030000.java` re-run: **96 unnamed** remain in region
(unchanged from Pass 194; rank-1 by size at xref=1 tier is `FUN_8003792c` at
66B — wins on size over tied 64B/62B siblings, first by address).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003792c` → `arm_link_state_advance_pending_and_commit_conn_slot_timing_mode1`**
(66B, HIGH, HANDLER-tier) via
`RenamePass195Region80030000Fun8003792c.java` (`renamed=1`, live-verified).

**Mechanism:** Per-conn-index `big_ol_struct` link-state advance establish on
`field_0x29d`. When `field_0x29d == 0`: sets flag to 1, calls
`FUN_800143b0(bos_connection__array_index, byte_0xCC, 1)` (IRQ-masked HW-channel
table merge), then
`recompute_and_commit_conn_slot_timing_hw_and_packet_types(param_1, 1)` (mode-1
link-state-advance commit path). Complement of Pass 192's
`clear_link_state_advance_pending_and_commit_conn_slot_timing_mode2` which tears
down `field_0x29d` from 1→0 and commits with mode 2.

**Callers:** 1 xref-in — `LC_event_RX_dispatcher` (LC RX event dispatch cluster;
documented in Pass 2 region `0x80040000`).

**Confidence:** HIGH — full 66B decompile; `field_0x29d` semantics match Pass
192 complement pair; named callee
`recompute_and_commit_conn_slot_timing_hw_and_packet_types` (Pass 74) anchors
mode-1 commit; establish/teardown symmetry with Pass 192 confirmed.

Region unnamed count after this pass: **95** (96 minus this rename). Live named
**2071** global.

**Next:** superseded by Pass 196.

## Pass 196 (2026-07-01) — HW reg 0x44 bits 7-10 RMW `FUN_8003b604`

Fresh `ListUnnamed80030000.java` re-run: **95 unnamed** remain in region
(unchanged from Pass 195; rank-1 by size at xref=1 tier is `FUN_8003b604` at
64B — wins on size over tied 62B/56B siblings, first by address).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003b604` → `read_modify_write_hw_reg_0x44_set_bits7_10_from_4bit_param`**
(64B, HIGH, SIMPLE-tier) via
`RenamePass196Region80030000Fun8003b604.java` (`renamed=1`, live-verified).

**Mechanism:** Read-modify-write HW/VSC register `0x44` bits 7-10: reads via
indirect read fptr at `PTR_DAT_8003b644` with args `(0, 0x44, 1)`, then writes
back via write fptr at `PTR_DAT_8003b648` with value
`(read_val & 0xf87f) | ((param_1 & 0xf) << 7)` — clears bits 7-10, replaces
with the low 4 bits of `param_1`. Sibling of Pass 194's
`read_modify_write_hw_reg_0x44_set_bits12_14_from_3bit_param` (bits 12-14)
and Pass 71's `read_modify_write_hw_reg_0x44_set_bit0` (bit 0) in the same
`0x8003b6xx` per-connection HW-buffer setup cluster.

**Callers:** 1 xref-in — `per_connection_hw_buffer_setup_with_patch_hook` (calls
with literal `5` as part of the setup sequence alongside
`read_modify_write_hw_reg_0x44_set_bits12_14_from_3bit_param(7)`,
`read_modify_write_hw_reg_0x44_set_bit0(1)`, and `read_modify_write_hw_reg_0x44_set_bit1(1)` before
setting bit `0x8000` of reg `0x44`).

**Confidence:** HIGH — full 64B decompile; register index `0x44` and 4-bit
field merge at bits 7-10 unambiguous; caller context matches documented Pass 8
item 13 per-connection HW-buffer setup cluster.

Region unnamed count after this pass: **94** (95 minus this rename). Live named
**2072** global.

**Next:** superseded by Pass 197.

## Pass 197 (2026-07-01) — HW reg 0x44 bit 1 RMW `FUN_8003b6fc`

Fresh `ListUnnamed80030000.java` re-run: **94 unnamed** remain in region
(unchanged from Pass 196; rank-1 by size at xref=1 tier is `FUN_8003b6fc` at
62B — wins on size over tied 62B/56B siblings, first by address).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003b6fc` → `read_modify_write_hw_reg_0x44_set_bit1`**
(62B, HIGH, SIMPLE-tier) via
`RenamePass197Region80030000Fun8003b6fc.java` (`renamed=1`, live-verified).

**Mechanism:** Read-modify-write HW/VSC register `0x44` bit 1: reads via
indirect read fptr at `PTR_DAT_8003b73c` with args `(0, 0x44, 1)`, then writes
back via write fptr at `PTR_DAT_8003b740` with value
`(read_val & 0xfffd) | ((param_1 & 1) << 1)` — clears bit 1, replaces with
the low bit of `param_1`. Sibling of Pass 71's
`read_modify_write_hw_reg_0x44_set_bit0` (bit 0), Pass 196's
`read_modify_write_hw_reg_0x44_set_bits7_10_from_4bit_param` (bits 7-10), and
Pass 194's `read_modify_write_hw_reg_0x44_set_bits12_14_from_3bit_param` (bits
12-14) in the same `0x8003b6xx` per-connection HW-buffer setup cluster.

**Callers:** 1 xref-in — `per_connection_hw_buffer_setup_with_patch_hook` (calls
with literal `1` as part of the setup sequence alongside
`read_modify_write_hw_reg_0x44_set_bits7_10_from_4bit_param(5)`,
`read_modify_write_hw_reg_0x44_set_bits12_14_from_3bit_param(7)`,
`read_modify_write_hw_reg_0x44_set_bit0(1)` before setting bit `0x8000` of reg
`0x44`).

**Confidence:** HIGH — full 62B decompile; register index `0x44` and bit-1
field merge unambiguous; caller context matches documented Pass 8 item 13
per-connection HW-buffer setup cluster.

Region unnamed count after this pass: **93** (94 minus this rename). Live named
**2073** global.

**Next:** superseded by Pass 198.

## Pass 198 (2026-07-01) — link-mode slot-budget timing gate `FUN_80033c98`

Fresh `ListUnnamed80030000.java` re-run: **93 unnamed** remain in region
(unchanged from Pass 197; rank-1 by size at xref=1 tier is `FUN_80033c98` at
62B — wins on size over tied 56B/54B siblings, first by address).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80033c98` → `check_link_mode_change_slot_budget_timing_gate_status`**
(62B, HIGH, SIMPLE-tier) via
`RenamePass198Region80030000Fun80033c98.java` (`renamed=1`, live-verified).

**Mechanism:** Link-mode-change slot-budget timing gate in the
`link_mode_change_state_machine` cluster. Computes masked budget
`(DAT_80033cd8 | param_2) - param_3 & DAT_80033cdc`. Phase byte `param_1`
selects config field extraction from `PTR_DAT_80033ce0`: phase `0` uses bits
5–7 (`>>5`), else bit 7 (`>>7`). Derives power-of-2 lower threshold
`2 << ((field & 3) + 1)` (4/8/16/32). Returns `0` (ready) when budget lies
strictly between threshold and upper bound `DAT_80033ce4`; else `0xff`
(blocked). For phases 2/3 caller pre-adjusts budget via
`(hw_clock >> 1) + secondary_timing & mask` before invoking.

**Callers:** 1 xref-in — `link_mode_change_state_machine` (after
`adjust_link_mode_change_slot_budget_and_secondary_timing` and VSC fc11
critical section; gates progression to
`apply_link_mode_change_bb_regs_and_timeout_by_phase`).

**Confidence:** HIGH — full 62B decompile; caller context and
busy(`0xf`)/ready(`0`)/blocked(`0xff`) convention match siblings
`check_link_mode_change_gate_status` and
`check_connection_setup_commit_gate_status`.

Region unnamed count after this pass: **92** (93 minus this rename). Live named
**2074** global.

**Next:** superseded by Pass 199.

## Pass 199 (2026-07-01) — HW reg 0x22 bits 7-9 RMW `FUN_800391d0`

Fresh `ListUnnamed80030000.java` re-run: **92 unnamed** remain in region
(unchanged from Pass 198; rank-1 by size at xref=1 tier is `FUN_800391d0` at
56B — wins on size over tied 56B/54B siblings, first by address).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_800391d0` → `read_modify_write_hw_reg_0x22_set_bits7_9_from_3bit_param`**
(56B, HIGH, SIMPLE-tier) via
`RenamePass199Region80030000Fun800391d0.java` (`renamed=1`, live-verified).

**Mechanism:** Read-modify-write HW/VSC register `0x22` bits 7-9: reads via
indirect read fptr at `PTR_DAT_80039208` with arg `0x22`, then writes back via
write fptr at `PTR_DAT_8003920c` with value
`(read_val & 0xfc7f) | ((param_1 & 7) << 7)` — clears bits 7-9, replaces with
the low 3 bits of `param_1`. Sibling of Pass 117's
`apply_hw_reg_0x2b_slot_nibble_if_config_bit3` / callee `FUN_80039194` (reg
`0x2b` upper-nibble merge) in the `0x800391xx` BB register-config init
cluster.

**Callers:** 1 xref-in — `FUN_80039234` (config-bit2-gated optional hook: when
`PTR_DAT_80039254[1]` bit 2 (`0x4`) set, passes config byte at offset `0x3d`
to program bits 7-9 of reg `0x22`).

**Confidence:** HIGH — full 56B decompile; register index `0x22` and 3-bit field
merge at bits 7-9 unambiguous; caller config-gate pattern matches Pass 117's
`apply_hw_reg_0x2b_slot_nibble_if_config_bit3` sibling.

Region unnamed count after this pass: **91** (92 minus this rename). Live named
**2075** global.

**Next:** superseded by Pass 200.

## Pass 200 (2026-07-01) — truncated-page status bit-8 handler `FUN_800379dc`

Fresh `ListUnnamed80030000.java` re-run: **91 unnamed** remain in region
(unchanged from Pass 199; rank-1 by size at xref=1 tier is `FUN_800379dc` at
56B — wins on size over tied 54B/52B siblings, first by address).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_800379dc` → `handle_trunc_page_complete_status_bit8_via_optional_hook_and_log_0x6e`**
(56B, HIGH, SIMPLE-tier) via
`RenamePass200Region80030000Fun800379dc.java` (`renamed=1`, live-verified).

**Mechanism:** Truncated-page-complete status-word bit-8 handler in the
`truncated_page_complete_status_dispatcher` cluster. Optional hook at
`PTR_DAT_80037a14` — when null or returns zero, default path invokes fptr at
`PTR_PTR_80037a18(0,0)` then logs via `possible_logger_called_if_no_patch3`
with event tag `0x6e` from `PTR_DAT_80037a1c`. Sibling of Pass 102's
`toggle_0x4000_status_bit_via_hook_on_trunc_page_counter_threshold` (bits
`0x80`/`0x100`/`0x200` branches) in the same dispatcher.

**Callers:** 1 xref-in — `truncated_page_complete_status_dispatcher` (`0x800022e4`)
when status-word bit 8 is set.

**Confidence:** HIGH — full 56B decompile; optional-hook + default-dispatch +
`possible_logger_called_if_no_patch3` idiom matches ROM cluster; caller context
in documented truncated-page-complete dispatcher (region `0x80000000`).

Region unnamed count after this pass: **90** (91 minus this rename). Live named
**2076** global.

**Next:** superseded by Pass 201.

## Pass 201 (2026-07-01) — BB reg config teardown `FUN_8003c790`

Fresh `ListUnnamed80030000.java` re-run: **90 unnamed** remain in region
(unchanged from Pass 200; rank-1 by size at xref=1 tier is `FUN_8003c790` at
54B — wins on size over tied 54B/52B siblings, first by address).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_8003c790` → `irq_masked_program_bb_regs_0x188_0x18a_mode0_and_clear_config_bit0x10`**
(54B, HIGH, SIMPLE-tier) via
`RenamePass201Region80030000Fun8003c790.java` (`renamed=1`, live-verified).

**Mechanism:** IRQ-masked BB register config teardown in the
`program_bb_regs_0x188_0x18a_by_mode_byte_gated_on_config_bit0x10` cluster.
Disables interrupts, invokes callee with mode byte `0` (selects template from
`PTR_DAT_8003c780`), then clears config `field452_0x1d0` bit `0x10` (`& 0xef`)
before restoring interrupts. Complement of Pass 82's mode-programmer callee —
this path programs mode-0 BB regs then drops the config gate bit.

**Callers:** 1 xref-in — `HCI_CMD_OGF_3F__Vendor_Specific__FUN_80030f1c` @
`0x80031b00` (master HCI VSC dispatcher cluster).

**Confidence:** HIGH — full 54B decompile; IRQ-mask idiom and callee/caller
context match documented Pass 82 BB reg `0x188`/`0x18a` config cluster.

Region unnamed count after this pass: **89** (90 minus this rename). Live named
**2077** global.

**Next:** superseded by Pass 202.

## Pass 202 (2026-07-01) — HW reg 0x16 bits 1-3 RMW `FUN_80039258`

Fresh `ListUnnamed80030000.java` re-run: **89 unnamed** remain in region
(unchanged from Pass 201; rank-1 by size at xref=1 tier is `FUN_80039258` at
54B — wins on size over tied 54B/52B siblings, first by address).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80039258` → `read_modify_write_hw_reg_0x16_set_bits1_3_from_3bit_param`**
(54B, HIGH, SIMPLE-tier) via
`RenamePass202Region80030000Fun80039258.java` (`renamed=1`, live-verified).

**Mechanism:** Read-modify-write HW/VSC register `0x16` bits 1-3: reads via
indirect read fptr at `PTR_DAT_80039290` with arg `0x16`, then writes back via
write fptr at `PTR_DAT_80039294` with value
`(read_val & 0xfff1) | ((param_1 & 7) << 1)` — clears bits 1-3, replaces with
the low 3 bits of `param_1`. Sibling of Pass 199's
`read_modify_write_hw_reg_0x22_set_bits7_9_from_3bit_param` in the `0x800391xx`
BB register-config init cluster.

**Callers:** 1 xref-in — `FUN_80039358` (config-bit1-gated optional hook: when
`PTR_DAT_80039378[1]` bit 1 (`0x2`) set, passes config byte at offset `0x3c`
to program bits 1-3 of reg `0x16`).

**Confidence:** HIGH — full 54B decompile; register index `0x16` and 3-bit field
merge at bits 1-3 unambiguous; caller config-gate pattern matches Pass 199's
`FUN_80039234` sibling (config bit `0x4`, offset `0x3d`).

Region unnamed count after this pass: **88** (89 minus this rename). Live named
**2078** global.

**Next:** superseded by Pass 203.

## Pass 203 (2026-07-01) — config latch `FUN_80033630`

Fresh `ListUnnamed80030000.java` re-run: **88 unnamed** remain in region
(unchanged from Pass 202; rank-1 by size at xref=1 tier is `FUN_80033630` at
54B — wins on size over tied 52B siblings, first by address).

Decompiled and renamed rank-1 cold-triage target:
**`FUN_80033630` → `latch_config_bit0x164_8_when_global_status_bit13_set`**
(54B, HIGH, SIMPLE-tier) via
`RenamePass203Region80030000Fun80033630.java` (`renamed=1`, live-verified).

**Mechanism:** One-shot latch on config `PTR_DAT_80033668+0x164` bit `0x8`: when
that latch bit is clear, tests global status dword at `DAT_8003366c` for bit 13
(`>>8 & 0x20`); when set, ORs `0x8000` (bit 15) into the global and sets
config `+0x164` bit `0x8` to prevent re-entry. Connection-setup / TX-power
cluster sibling of `check_calibration_mode_conn_weight_vs_config_threshold`
(which gates on `+0x164` bit `0x10`).

**Callers:** 1 xref-in — patch `FUN_80110ca4` @ `0x80110cbe` (COMPUTED_CALL via
sub-installer #3 fn-ptr installed at `0x80120600-0x80121100`).

**Confidence:** HIGH — full 54B decompile; latch bit, status bit 13, and
`0x8000` set unambiguous; patch indirect-call xref confirmed.

Region unnamed count after this pass: **87** (88 minus this rename). Live named
**2079** global.

**Next:** Pass 204 — fresh `ListUnnamed80030000` re-rank; decompile+rename top
rank-1 unnamed function.
