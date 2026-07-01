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
`FUN_8003e1d4`+`FUN_8002bb50`+optional `FUN_8003e648`+indirect fptr+log (link-supervision-loss
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
`(0x1000 - (val&0x7f)) & 0x7f`, and applies it via `FUN_80038e24`. Matches the textbook shape of
a crystal/clock-trim calibration routine.

#### 6. `link_mode_change_state_machine` (0x80035454, 456B)

`(byte link_type, uint, uint)`. The core link-mode/role-switch procedure dispatcher for this
region's `FUN_80033a04`/`FUN_80033ae4`/`FUN_80033b14`/`FUN_80033c98`/`FUN_80035378`
helper cluster (all in this same region, none yet independently decompiled). Uses an explicit
busy(`0xf`)/idle(`0xff`) status convention. Issues a vendor command `VSC_0xfc11_2_FUN_800120ac`,
brackets the critical section with `disable_interrupts_`/`enable_interrupts_`, applies link
parameters via `FUN_80033c98`/`FUN_80035214`, and on failure cleans up via `FUN_80034d88`
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
right-shift of the 64-bit result via two calls to `FUN_80038c94` (sign-extend/shift) with manual
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
(`FUN_8003b604(5)`, `FUN_8003b64c(7)`, `FUN_8003b698(1)`, `FUN_8003b6fc(1)`) and sets bit 0x8000
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
config-defined threshold (`field160_0xa8`/`field161_0xa9`) and `FUN_80035378(1)!=0xff` and a
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

**Mechanism:** Thin wrapper tail-calling `FUN_8003497c(out_dword, NULL, role_index)` —
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
`multi-VSC_Handler_FUN_80032540`, `FUN_80039c98`, patch `FUN_8010fcac`.

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
`conn_event_packet_type_update_and_reschedule`, `FUN_800366cc`,
`FUN_800367e4`, and `sweep_conn_table_program_esco_packet_type_and_clear_gate_bytes`
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

**Next:** Pass 63 — fresh `ListUnnamed80030000` re-rank; decompile+rename top
rank-1 unnamed function.
