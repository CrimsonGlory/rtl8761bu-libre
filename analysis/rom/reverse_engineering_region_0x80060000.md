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

## Enumeration Strategy (Pass 1)

**Step 1**: Run `ListRegion0x80060000.java` (GZF process mode) to enumerate all 332 functions by size/name/type.

**Step 2**: Cross-reference output against:
- rom_function_index.md (confidence flags, Kovah naming survey)
- rom/reverse_engineering_lc_lmp_state_machine.md (LMP procedure cluster)
- rom/reverse_engineering_encryption_engine.md (SAFER+ cipher + LMP encryption handlers)

**Step 3**: Identify next priority batch for decompile:
- 94 thin-named functions (LARGEST batch in any region so far — HIGHEST ROI)
- Categorize by LMP opcode family (COMB_KEY/AU_RAND/SRES = highest-priority encryption cluster)

## Next Steps (Self-Chaining)

If enumeration script completes successfully:

1. **Pass 2 (Batch Triage)**: Scan 94 thin-named: categorize into LMP opcode families; identify top 20 largest
2. **Pass 2 (Decompile Batch)**: Run `BatchDecompileList.java` on top 20 thin-named (encryption family first)
3. **Pass 2 (Rename)**: Use `RenameBatch1.java` to upgrade thin-named → medium confidence
4. **Pass 3+ (Triage Loop)**: Continue region sweep; focus on remaining LMP procedures + extended opcodes
5. **Final**: Update rom_function_index.md + analysis/INDEX.md; mark region [DONE]

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
