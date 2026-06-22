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

## Enumeration Strategy (Pass 1)

**Step 1**: Run `ListRegion0x80070000.java` (GZF process mode) to enumerate all 244 functions by size/name/type.

**Step 2**: Cross-reference output against:
- rom_function_index.md (confidence flags, existing names)
- rom/reverse_engineering_encryption_engine.md (cipher cluster location)
- analysis/reverse_engineering_patch_installer.md Appendix C (RF table locations @ 0x8011106c)

**Step 3**: Identify next priority batch for decompile:
- 51 thin-named functions (large batch, but smaller than 0x80060000)
- Distinguish data tables (no decompile needed) from utility functions

## Next Steps (Self-Chaining)

If enumeration script completes successfully:

1. **Pass 2 (Batch Triage)**: Categorize 51 thin-named: data vs. code; identify top 15 largest code functions
2. **Pass 2 (Decompile Batch)**: Run `BatchDecompileList.java` on top 15 thin-named code functions
3. **Pass 2 (Rename)**: Use `RenameBatch1.java` to upgrade thin-named code → medium confidence; mark data as DATA_* or CIPHER_TABLE_*
4. **Pass 3+ (Triage Loop)**: Continue region sweep; focus on remaining utility chains and register config helpers
5. **Final**: Update rom_function_index.md + analysis/INDEX.md; mark region [DONE]

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
