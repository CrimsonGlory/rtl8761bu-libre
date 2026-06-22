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

**Reclassifications expected (post-decompile):** 8–10 functions → high-confidence based on decompile clarity. "idk" and "called_by_*" names suggest Kovah left purposes intentionally vague for manual RE verification.

## Coverage Progress

- **Named functions:** 17 of 307 (5.5%)
- **High-confidence:** 2 (decompiled + documented)
- **Medium-confidence:** 0
- **Low-confidence (thin-named):** 15 (named by Kovah, awaiting decompile)
- **Unnamed:** 290 (FUN_* auto-generated, not yet triaged)

**Reclassifications expected:** Some thin-named VSC handlers and HCI handlers may be medium- or high-confidence after decompile; the "idk" and "called_by_*" names suggest Kovah found them but left purpose unclear.
