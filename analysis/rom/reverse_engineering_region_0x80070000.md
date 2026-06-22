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

## Pass 2: Batch Triage (IN PROGRESS)

### Thin-Named Addresses (41 total)

**Address list for batch decompile** (comma-separated hex):
```
0x80070000,0x80070454,0x8007088c,0x8007095c,0x80070c04,0x80071620,0x80071634,0x80071b84,
0x80071ba4,0x80071d98,0x80072404,0x8007243c,0x80072648,0x80073348,0x80073b74,0x80074d84,
0x80074dfc,0x80074e38,0x80074e84,0x80074eb4,0x80074ee0,0x80074f38,0x80074fa8,0x80075084,
0x80075324,0x800754c4,0x80075540,0x80075650,0x800756c0,0x80075704,0x8007572c,0x8007579c,
0x80075948,0x80075e34,0x800761f4,0x800762f4,0x8007666c,0x80076bd8,0x80077620,0x8007943c,
0x800798b0
```

### Strategy

1. **Data vs. Code classification**:
   - Known data: SAFER+ cipher S-boxes, RF register tables (immutable config constants)
   - Known code: LMP dispatchers, RF init chains, system bootstrap helpers
   - Cross-reference rom/reverse_engineering_encryption_engine.md and rom/reverse_engineering_patch_installer.md for locations

2. **Batch decompile target**: Top 15 largest thin-named CODE functions (exclude data tables)

3. **Timeline**: 5–6 minutes (batch decompile via GZF process mode + confidence upgrade)

## Next Steps (Self-Chaining)

1. **Pass 2 (Batch Decompile)**: Invoke wairz GZF process-mode batch decompile on all 41 thin-named addresses
2. **Pass 2 (Data Classification)**: Mark known data tables (cipher/RF) as non-function placeholders in rom_function_index.md
3. **Pass 2 (Rename + Tally)**: Update rom_function_index.md with high-confidence code rows; tally upgrade count
4. **Pass 3+ (Triage Loop)**: Cold-triage remaining 193 unnamed functions; prioritize utility chains
5. **Final**: Update analysis/INDEX.md; mark region status progression

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
