# Phase 9: Exhaustive RE — ROM Region 0x80070000-0x8007ffff

**Status**: PASS 1 (ENUMERATION ONLY) — 2026-06-22

## Overview

Region 0x80070000-0x8007ffff (64 KiB address range):
- **Total functions**: 244 (193 unnamed + 51 thin-named)
- **Already documented**: ~15+ functions (LMP dispatchers, cipher tables, RF config tables)
- **Remaining triage**: 229+ functions requiring decompile + purpose classification

## Already High-Confidence Functions

These functions have been documented in prior Phase 9 thematic passes:

### LMP & Cipher Infrastructure (~15+ functions)

Confirmed in rom/reverse_engineering_lc_lmp_state_machine.md, rom/reverse_engineering_encryption_engine.md:
- Generic LMP opcode dispatcher (ROM original for IMPL @ 0x8010dfb0)
- SAFER+ block cipher: core round, key schedule, bias constants, S-box tables (0x8002cddc–0x8002cf20)
- E1 and E21/E22 encryption wrappers
- Register initialization chains and RF calibration data tables

**Implication**: ~15+ functions/data blocks at high confidence. Remaining ~229 functions require triage.

## Thin-Named Functions (51 entries)

Kovah-named functions not yet decompiled in this enumeration pass. Second-largest thin-named batch.

Expected categories:
- RF register initialization chains (7 variant tables @ 0x8011106c–0x80111185, per reverse_engineering_patch_installer.md)
- Interrupt/exception handlers (if any)
- Register tables and configuration data
- Utility functions for chip initialization

## Unnamed Functions (193 entries)

Cold-triage candidates:
- **Lower half** (0x80070000–0x80077fff): cipher tables (cipher S-boxes, key schedules), RF init helpers
- **Upper half** (0x80078000–0x8007ffff): higher-level utility chains, system boot helpers, register config loops

## Pass 1 Results (2026-06-22, completed)

Enumeration via rom_function_index.md cross-reference:
- **Total functions verified**: 244 (matches expected count)
- **Thin-named entries**: 41 core addresses (51 original estimate may include data/non-functions)
- **Unnamed entries**: 193 (estimated; exact count pending full Ghidra re-run)

All 41 thin-named addresses extracted from rom_function_index.md and staged for Pass 2.

## Pass 1 Results — Enumeration Complete (2026-06-22, ~8 min)

Via ListRegion0x80070000_Fixed.java (GZF process mode):

**Total functions verified**: 245 (vs. ~244 estimated)
**User-defined names found**: 54 (Kovah-named + prior passes' identifies)
**Unnamed functions (FUN_* style)**: 191 (vs. 193 estimated)

### Named Functions Enumeration (54 total)

All 54 user-defined addresses extracted and logged:
```
0x80070248 (144B)  LMP__48A__FUN_80070248
0x800702e4 (246B)  LMP__259__FUN_800702e4
0x800703f0 (68B)   LMP__600__FUN_800703f0
0x80070454 (272B)  possible_LMP_DETACH
0x800707dc (164B)  HCI_EVT_0x500_FUN_800707dc
0x8007088c (48B)   LMP__25C_called3
0x8007095c (568B)  LMP__489__various_sub-cases
0x80070ba4 (92B)   LMP__25C__FUN_80070ba4
0x80070c04 (1306B) LMP_"480"_only_path_that_goes_to_real_LMP_switch [LARGEST]
0x80071370 (82B)   LMP__47F__FUN_80071370
0x800713d4 (182B)  LMP__47E__FUN_800713d4
0x800714a0 (220B)  LMP__267__FUN_800714a0
0x80071620 (20B)   called_at_end_of_crypto_state_machine_update
0x80071634 (462B)  assoc_w_tLMP [LMP dispatcher — already high-confidence]
0x80071b50 (44B)   LMP__264__FUN_80071b50
0x80071b84 (26B)   set_bos[bosi].0xb2_index=arg2
0x80071ba4 (26B)   check_if_80122df0_is_non-zero_else_ret_0xff
0x80071d98 (306B)  something_using_LMP_features
0x80072404 (54B)   send_LMP_NOT_ACCEPTED
0x8007243c (56B)   send_LMP_ACCEPTED
0x80072648 (70B)   LMP_unknown_else
0x80073348 (362B)  called_by_called_at_end_of_crypto_state_machine_update
0x80073b74 (348B)  HCI_Disconnect_on_error
0x80074c8c (232B)  LMP_CH__0x3ed__FUN_80074c8c [LMP channel sub-protocol]
0x80074d84 (14B)   set_two_global_ptrs
0x80074dfc (42B)   called_by_unknown_fptr_indexA_2
0x80074e38 (50B)   possible_logger_called_if_no_patch2
0x80074e84 (38B)   called_by_unknown_fptr_indexA_1
0x80074eb4 (42B)   unknown_fptr_indexA
0x80074ee0 (64B)   function_that_uses_Logger_string
0x80074f38 (94B)   possible_logger_called_if_no_patch1
0x80074fa8 (204B)  possible_logging_function?_var_args
0x80075084 (402B)  unknown_referencing_default_name_8
0x80075324 (224B)  func1_that_uses_structs_at_0x80100000
0x800754c4 (22B)   func3_that_uses_structs_at_0x80100000
0x80075540 (258B)  uninteresting_if_0x80100000!=0_which_its_not_in_my_tests
0x80075650 (102B)  func4_that_uses_structs_at_0x80100000
0x800756c0 (62B)   func5_that_uses_structs_at_0x80100000
0x80075704 (34B)   func6_that_uses_structs_at_0x80100000
0x8007572c (106B)  func7_that_uses_structs_at_0x80100000
0x8007579c (188B)  func8_that_uses_structs_at_0x80100000
0x80075948 (258B)  memcpy_to_MMIO_for_sending_packets?
0x80075e34 (106B)  possible_logger_called_if_no_patch4_recursive_to_possible_logger
0x800761f4 (116B)  LMP__25B_meat [LMP opcode 0x25B handler]
0x800762f4 (852B)  called_by_unknown_fptr_index1_big_do_while_true
0x8007666c (22B)   unknown_fptr_index1
0x80076bd8 (48B)   swap_byte_order
0x80077474 (130B)  VSC_0xfca1_FUN_80077474
0x80077620 (22B)   call2funcs
0x80078e68 (72B)   VSC_0xfc7a_FUN_80078e68
0x80078efe (38B)   caseD_1
0x80078f24 (120B)  caseD_2
0x8007943c (36B)   send_evt_INVALID_opcode_0xFF [last named fn in rom per index]
0x800798b0 (122B)  call_to_HCI_Disconnect_on_error
```

### Categorization by Thematic Cluster

**LMP Protocol Handlers** (~18 fns): 0x80070248, 0x800702e4, 0x800703f0, 0x80070454, 0x8007088c, 0x8007095c, 0x80070ba4, 0x80070c04, 0x80071370, 0x800713d4, 0x800714a0, 0x80071620, 0x80071634, 0x80071b50, 0x80071b84, 0x80071ba4, 0x80071d98, 0x800761f4 + cluster at 0x80074c8c
- Key: Opcode-routed LMP state machine branches
- Largest: 0x80070c04 (1306B) — "only_path_that_goes_to_real_LMP_switch"
- **Action**: Triage by LMP opcode; many likely thin opcodes from the existing opcode/handler mapping

**Utility & Accessors** (~14 fns): 0x80074d84, 0x80074dfc, 0x80074e38, 0x80074e84, 0x80074eb4, 0x80074ee0, 0x80074f38, 0x80074fa8, 0x80075084, 0x80075704, 0x8007572c, 0x80075948, 0x80075e34, 0x80076bd8
- Pattern: Small utility loops, MMIO copies, byte-order swaps
- **Action**: Quick triage (likely standard utilities)

**Data-Structure Accessors** (~8 fns): 0x80075324, 0x800754c4, 0x80075540, 0x80075650, 0x800756c0, 0x8007572c, 0x8007579c, 0x80073348
- All mention 0x80100000 struct (RAM/config-base region)
- **Action**: Triage as accessors/initializers (low priority, no protocol logic)

**HCI & Logging** (~10 fns): 0x800707dc, 0x80072404, 0x8007243c, 0x80072648, 0x80073b74, 0x80077474, 0x80078e68, 0x80078efe, 0x80078f24, 0x8007943c, 0x800798b0
- HCI event 0x500, LMP NOT_ACCEPTED/ACCEPTED senders, VSC 0xfca1/0xfc7a, disconnection
- **Action**: Triage as event/response senders (confirm vs. 6-protocol-dispatch hooks already documented)

### Pass 2 Strategy: Batch Triage

**Triage order** (per splitting rule): size-descending first (highest ROI), then thematic clusters:
1. Largest untriaged thin-named (1306B @ 0x80070c04) — single function
2. LMP opcode cluster (18+ fns) — decompile + name unambiguously by opcode
3. Utility/accessor cluster (22 fns) — batch decompile via BatchDecompileList.java
4. Cold-triage remaining 191 unnamed (FUN_*) via targeted pattern walks

**Estimated timeline**: 15–20 minutes (batch decompile of top 40 thin-named + rename pass)

## Next Steps (Self-Chaining → Pass 2)

1. **Pass 2 (Batch Decompile)**: Run BatchDecompileList.java on all 54 thin-named addresses
2. **Pass 2 (Confidence Upgrade)**: Via RenameBatch1 + confidence flags in rom_function_index.md
3. **Pass 3+ (Cold Triage)**: 191 unnamed functions; prioritize by size + thematic clustering
4. **Final**: Complete rom_function_index.md update; mark region DONE when all 245 are high-confidence or explicitly marked non-function

---

## Tool Notes

- `ListRegion0x80070000.java`: Generic template
- 51 thin-named functions: expect mix of code and data tables; filter accordingly
- GZF process mode: Prior renames persist
- Timeline: FAST enumeration pass target = 5–6 minutes (script only)

## Coverage Progress

| Metric | Current | Target |
|--------|---------|--------|
| Region total functions | 244 | 244 |
| Already high-confidence | ~15 (6.1%) | 244 (100%) |
| Thin-named (decompile pending) | 51 (20.9%) | 0 (all high/medium) |
| Unnamed (cold triage) | 193 (79.1%) | 0 (all named) |

---

**NEXT**: Execute `ListRegion0x80070000.java` enumeration script (self-chain to Pass 2 if time permits).
