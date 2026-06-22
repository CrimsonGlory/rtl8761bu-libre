# Phase 9: Exhaustive RE — ROM Region 0x80050000-0x8005ffff

**Status**: PASS 2 (TRIAGE + BATCH DECOMPILE PREP) — 2026-06-22

## Overview

Region 0x80050000-0x8005ffff (64 KiB address range):
- **Total functions**: 364 (354 unnamed + 10 thin-named)
- **Already documented**: ~30+ functions (eSCO/SCO slot allocation, feature negotiation)
- **Remaining triage**: 324+ unnamed functions requiring decompile + purpose classification

## Already High-Confidence Functions

These functions have been fully documented in prior Phase 9 thematic passes and are anchors for understanding the region's structure:

### eSCO/SCO Slot & Feature Management (~25+ functions)

Confirmed in rom/reverse_engineering_conn_feature_dispatch.md, rom/reverse_engineering_conn_type_dispatch_and_esco.md:
- Connection record allocation and slot validators
- Slot-interval table processors
- Feature-page hash-bucket refcount managers
- Codec-table and AFH-channel allocators
- Link-state timing validators and dispatchers

**Implication**: ~25+ functions in this region already at high confidence. Remaining ~339 functions require triage.

## Thin-Named Functions (10 entries, PASS 2 TRIAGE TARGETS)

All 10 confirmed from `rom_function_index.md` (0x80050000-0x8005ffff range, low/medium confidence):

| Address | Size | Name | Category | Confidence |
|---------|------|------|----------|------------|
| `0x800525b4` | 36B | `send_evt_Meta_buf_at_arg1+0x100` | LE Meta event sender variant | LOW |
| `0x800525d8` | 62B | `send_evt_Meta_buf_at_arg1` | LE Meta event sender | LOW |
| `0x800566f8` | 58B | `VSC_0xfc97_1_FUN_800566f8` | VSC 0xFC97 handler (LE extended) | MEDIUM |
| `0x8005681c` | 84B | `VSC_0xfc73_3_FUN_8005681c` | VSC 0xFC73 handler (AFH/LMP) | LOW |
| `0x80056878` | 84B | `VSC_0xfc73_2_FUN_80056878` | VSC 0xFC73 handler (AFH/LMP) | LOW |
| `0x800568d4` | 94B | `VSC_0xfc73_1_FUN_800568d4` | VSC 0xFC73 handler (AFH/LMP) | LOW |
| `0x8005770c` | 166B | `VSC_0xfc97_2_FUN_8005770c` | VSC 0xFC97 handler (LE extended) | MEDIUM |
| `0x800596c8` | 50B | `get_0x1ac_struct_ptr_by_index` | Config struct accessor | LOW |
| `0x8005a298` | 62B | `get_TX_or_RX_PHY` | PHY getter (LE/BRx) | LOW |
| `0x8005e3b8` | 80B | `c_by_fHCI_Read_Remote_Version_Information_various_0x1ac_manip` | HCI handler (version info) | LOW |

**Pass 2 action**: Decompile all 10 via `BatchDecompileList.java` (arguments: comma-separated addresses above). Rename via `RenameBatch1.java` once purposes confirmed.

## Unnamed Functions (344 entries, after excluding 10 thin-named + 2 already high-confidence)

Cold-triage candidates identified from region structure:
- **Lower half** (0x80050000–0x80057fff): slot allocation, feature dispatch, timer chains
- **Upper half** (0x80058000–0x8005ffff): higher-level state machines, retry logic, link-event handlers

### Top 5 Largest Unnamed (Heuristic from Prior Regions)

Estimated top-5 candidates by function size (pending enumeration confirmation):

| Address | Est. Size | Likely Purpose | Priority |
|---------|-----------|---|----------|
| ~0x80050xxx | ~400–600B | Connection state dispatch/validation | HIGH |
| ~0x80051xxx | ~300–400B | Slot allocation or feature negotiation | HIGH |
| ~0x80053xxx | ~250–350B | Link-layer state handler | HIGH |
| ~0x80054xxx | ~200–300B | VSC dispatcher or protocol handler | MED |
| ~0x80055xxx | ~200–250B | Timer or retry logic | MED |

**Note**: Exact addresses require `ListRegion0x80050000.java` enumeration output (pending).

## Enumeration Strategy (Pass 1 → Pass 2)

**Status**: Pass 1 enumeration deferred to upstream supervisor (wairz MCP tool); Pass 2 proceeds with rom_function_index data + heuristic-based preparation.

**Step 1 (COMPLETED)**: Extract thin-named + already-high-confidence counts from rom_function_index.md.

**Step 2 (IN PROGRESS)**: Prepare batch decompile command for 10 thin-named functions.

**Step 3 (PENDING)**: Upon enumeration confirmation, add top-5 largest unnamed addresses to decompile batch.

## Next Steps (Self-Chaining)

**Pass 2 (THIS PASS) — Batch Decompile Prep**:

1. ✓ Identify 10 thin-named functions (done — see table above)
2. ✓ Prepare batch decompile command (done — see prepared batch below)
3. → Await supervisor: Run `BatchDecompileList.java` with 10-address batch
4. → Use `RenameBatch1.java` to confirm names and upgrade confidence
5. **Pass 3 (NEXT)**: Cold-triage remaining 344 unnamed via continued enumeration loops
6. **Final**: Update rom_function_index.md + analysis/INDEX.md; mark region [DONE]

## Pass 2 — Batch Decompile Prep (2026-06-22)

**Prepared batch for execution** (awaiting supervisor/wairz MCP invocation):

### Step 1: Decompile 10 Thin-Named (HIGH-ROI batch)
```
script_file_id: BatchDecompileList (or equivalent)
script_args[0]: 0x800525b4,0x800525d8,0x800566f8,0x8005681c,0x80056878,0x800568d4,0x8005770c,0x800596c8,0x8005a298,0x8005e3b8
binary_path: 2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf
use_saved_project: True
timeout: 120
```
**Expected output**: Full decompilation + callee analysis for each function; saves to GZF project (renames persist).

### Step 2: Rename Functions (upgrade to high confidence)
Once decompiles confirm purposes, execute:
```
script_file_id: RenameBatch1 (or equivalent)
script_args[0]: (See "Rename mapping" section below after decompile)
binary_path: 2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf
use_saved_project: True
```

### Rename Mapping (post-decompile, TBD)
To be filled in after `BatchDecompileList.java` output is reviewed. Likely pattern:
- `send_evt_Meta_buf_at_arg1` → `send_evt_Meta_subevent_or_buffer_wrapper` (high confidence)
- `VSC_0xfc97_*` → `VSC_0xfc97_Set_Extended_Advertising_Parameters_*` or LE-related (medium)
- `VSC_0xfc73_*` → `VSC_0xfc73_AFH_Channel_Assessment_*` (medium)
- `get_0x1ac_struct_ptr_by_index` → `config_struct_array_lookup` (medium)
- `get_TX_or_RX_PHY` → `query_current_PHY_by_conn_index` (high)

---

## Tool Notes

- `ListRegion0x80050000.java`: Generic template (reuse of prior region scripts) — no stdout truncation expected for 364 functions
- `BatchDecompileList.java`: Standard batch decompiler (takes comma-separated hex addresses)
- `RenameBatch1.java`: Standard batch renamer (takes address→name mapping)
- GZF process mode: Renames made in prior passes (0x80000000, 0x80010000, 0x80020000, 0x80030000, 0x80040000) persist
- Timeline: Pass 2 estimated execution = 15–20 minutes (decompile batch only, no region-wide triage yet)

## Coverage Progress

| Metric | Current | Target |
|--------|---------|--------|
| Region total functions | 364 | 364 |
| Already high-confidence | ~27 (7.4%) | 364 (100%) |
| Thin-named pending decompile | 10 (2.7%) | 0 (all high/medium) |
| Unnamed cold triage | 327 (89.8%) | 0 (all named) |

---

**NEXT**: Execute batch decompile (Step 1–2 above); stage remaining 327 unnamed for triage loop (Pass 3+).
