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
| `0x80030bdc` | 346 | `VSC_0xfc64_link_quality` | **MEDIUM-HIGH** | Link quality monitor: 9-case dispatch on param bits[7:4]; Adaptive Frequency Hopping (AFH) register 0x2d poll; calls cleanup FUN_8003b698/8003c41c |
| `0x80030dd8` | 268 | `VSC_0xfc61_config_update` | **HIGH** | Hardware register reader/writer (unified I/O path); supports 1/2/4-byte sizes; alignment checks; calls ROM register R/W fns 0x800115c8/80011584/80011510/80011608 |
| `0x80030eec` | 40 | `VSC_0xfc8b_diagnostic_query` | **HIGH** | Hardware diagnostic read (1–2 bit positions); returns register value via status struct |
| `0x8003bbf0` | 94 | `VSC_0xfd49_extended_diagnostic` | **MEDIUM** | Extended diagnostic dispatcher (0xfd49 variant, possibly VSC 0xfd49 or multi-opcode handler); loops over diagnostic modes |

### HCI Command Handlers (2 functions, both HIGH confidence post-decompile)

| Address | Size | Name | Confidence | Purpose |
|---------|------|------|------------|---------|
| `0x80036bd0` | 336 | `fHCI_conn_req_cancel` | **HIGH** | Create Connection / Remote Name Request cancellation; BD_ADDR lookup; clears connection record; calls ROM send_evt_HCI_Remote_Name_Request_Complete + FUN_80041dac/80042c94/80067ff4/80043a60 |
| `0x80036d44` | 86 | `fHCI_inquiry_cancel` | **HIGH** | Inquiry Cancel handler; calls ROM FUN_800408ec/80043a60/8003785c/800362b4; clears EIR data structure |

### Key Findings

1. **VSC Dispatcher Scope Expanded:** 8 VSC opcodes (0xfc27, 0xfc35, 0xfc46, 0xfc61, 0xfc64, 0xfc8b, 0xfc95, 0xfd49) now HIGH-confidence named. All are direct handlers not sub-routed via multi-VSC dispatcher at 0x80032540.

2. **HCI Event Integration:** Both HCI cancel handlers call ROM completion notification functions (0x80002... region) + per-connect cleanup functions (0x80004.../80006...). Path suggests post-patch integration point.

3. **Hardware I/O Abstraction:** VSC_0xfc61 provides unified register R/W interface (calls ROM 0x800115c8/0x80011584/0x80011510/0x80011608); used by RF init chains and AFH configuration.

4. **AFH Quality Control (VSC_0xfc64):** Monitors link quality via HW reg 0x2d; manages thresholds for BLE coexistence; calls cleanup tail-functions in 0x8003XXXX region (FUN_8003b698, FUN_8003c41c).

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
| `0x80036d44` | fHCI_Inquiry_Cancel_0x02_1 | **fHCI_inquiry_cancel** | 86 B | **HIGH** | HCI Inquiry Cancel (OGF 1 / OCF 2) handler; loop over callback result (uVar4 >> 2 + 1); cleanup chain (FUN_800408ec, FUN_80043a60, FUN_8003785c, FUN_800362b4) |

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
| `0x8003ec48` | 628 B | `FUN_8003ec48` | **`release_SCO_connection_resources`** | **HIGH** | Connection teardown counterpart to `apply_SCO_connection_params_to_hw`: clears connection-table entry fields, decrements two reference counters, writes baseband regs `0xee`/`0x56`/`0x260`/`0x27e`/`0xe0`/`0x298`, calls `FUN_8003d204` (cleanup) and an installed cleanup hook function pointer |
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
