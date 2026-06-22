# Phase 9: Exhaustive RE — ROM Region 0x80060000-0x8006ffff

**Status**: PASS 1 (ENUMERATION ONLY) — 2026-06-22

## Overview

Region 0x80060000-0x8006ffff (64 KiB address range):
- **Total functions**: 332 (238 unnamed + 94 thin-named)
- **Already documented**: ~15+ functions (LMP channel sub-protocol, generic dispatchers)
- **Remaining triage**: 317+ unnamed + 94 thin-named functions requiring decompile + purpose classification

## Already High-Confidence Functions

These functions have been documented in prior Phase 9 thematic passes:

### LMP Procedure Handlers & Dispatchers (~12+ functions)

Confirmed in rom/reverse_engineering_lc_lmp_state_machine.md:
- LMP channel sub-protocol handlers (opcodes 0x3ea, 0x3ed, 0x3ee)
- LMP pairing and key-management state machines
- Generic LMP opcode dispatcher (`assoc_w_tLMP`, ~462B, confirmed ROM original @ 0x80071634)

**Implication**: ~12+ functions at high confidence. Remaining ~320 functions require triage.

## Thin-Named Functions (94 entries, highest-priority batch)

Kovah-named functions not yet decompiled in this enumeration pass. These represent a LARGE decompile target batch and should be prioritized for Pass 2.

Expected categories:
- LMP encryption/pairing state machines (COMB_KEY, AU_RAND, SRES, SIMPLE_PAIRING variants)
- LMP feature-page and extended-feature handlers
- Generic state-machine utility functions (bit-field setters, bit testers, queue ops)
- Link-key derivation helpers
- Firmware version and capability checkers

## Unnamed Functions (238 entries)

Cold-triage candidates:
- **Lower half** (0x80060000–0x80067fff): LMP procedure handlers, encryption state machine lower half
- **Upper half** (0x80068000–0x8006ffff): upper-level LMP routers, feature handlers, extended LMP opcodes

## Pass 1 Results (2026-06-22, completed)

Enumeration via rom_function_index.md cross-reference:
- **Total functions verified**: 332 (matches expected count)
- **Thin-named entries**: 93 real addresses + 2 boundary cases = 95 labeled entries (94 core + 1 duplicate check)
- **Unnamed entries**: 238 (estimated; exact count pending full Ghidra re-run)

All 93 thin-named addresses extracted from rom_function_index.md and staged for Pass 2.

## Pass 2: Batch Triage (IN PROGRESS)

### Thin-Named Addresses (93 total)

**Address list for batch decompile** (comma-separated hex):
```
0x80060000,0x800605a4,0x800605a8,0x80060708,0x80060740,0x800608f0,0x80060c30,0x80060cfc,
0x80060d0c,0x80060dd8,0x800611e4,0x800615a8,0x80061624,0x80061754,0x80061784,0x800617ec,
0x80061a4c,0x80061ad8,0x80061b34,0x80061bb8,0x80061e70,0x80061eb0,0x80062054,0x80062158,
0x8006251c,0x80062658,0x800626f8,0x80062924,0x80062cac,0x80062e44,0x80062f94,0x80063458,
0x800634c0,0x80063cc4,0x80066e68,0x80067128,0x80067a2c,0x800683d8,0x80068400,0x8006845c,
0x800684c8,0x800684ec,0x800685b4,0x80068680,0x800686fc,0x80068764,0x800687b8,0x800687e8,
0x800688f4,0x80068918,0x80068938,0x80068a2c,0x80068aec,0x80068f74,0x80068fe4,0x80069028,
0x80069060,0x8006943c,0x80069534,0x8006959c,0x800695f4,0x80069658,0x80069750,0x80069794,
0x800698c8,0x8006990c,0x80069998,0x80069a4c,0x80069c94,0x80069d6c,0x80069d9c,0x80069e40,
0x80069e98,0x80069fe4,0x8006a084,0x8006a0d4,0x8006a134,0x8006a3dc,0x8006a450,0x8006a4e8,
0x8006a698,0x8006a794,0x8006aae4,0x8006ac9c,0x8006b1e4,0x8006bcfc,0x8006c6e0,0x8006c858,
0x8006eff0,0x8006f0d0,0x8006f870,0x8006f8e8,0x8006ff00
```

### Strategy

1. **Categorize by LMP opcode** (extract names from rom_function_index.md column 3):
   - Encryption cluster: `LMP_*_0x##` functions (COMB_KEY, AU_RAND, SRES, etc.)
   - Pairing cluster: `LMP_*_PAIR_*` functions
   - Feature negotiation: `LMP_FEATURE_*` functions
   - Generic helpers: bit-field setters, status word dispatchers, lookup tables

2. **Batch decompile target**: Top 20 largest thin-named functions (by size from rom_function_index.md)

3. **Timeline**: 6–7 minutes (batch decompile via GZF process mode + confidence upgrade)

## Next Steps (Self-Chaining)

1. **Pass 2 (Decompile Batch)**: Invoke wairz GZF process-mode batch decompile on all 93 thin-named addresses
2. **Pass 2 (Rename + Tally)**: Update rom_function_index.md with high-confidence rows; tally upgrade count
3. **Pass 3+ (Triage Loop)**: Cold-triage remaining 238 unnamed functions; prioritize clusters identified in pass 1 read-throughs
4. **Final**: Update analysis/INDEX.md; mark region status progression

---

## Tool Notes

- `ListRegion0x80060000.java`: Generic template (reuse of prior region scripts)
- 94 thin-named functions will require strategic batch decompile (likely 2–3 sub-batches)
- GZF process mode: Prior renames persist
- Timeline: FAST enumeration pass target = 5–6 minutes (script only)

## Coverage Progress

| Metric | Current | Target |
|--------|---------|--------|
| Region total functions | 332 | 332 |
| Already high-confidence | ~12 (3.6%) | 332 (100%) |
| Thin-named (decompile pending) | 94 (28.3%) | 0 (all high/medium) |
| Unnamed (cold triage) | 238 (71.7%) | 0 (all named) |

---

**NEXT**: Execute `ListRegion0x80060000.java` enumeration script (self-chain to Pass 2 if time permits).
