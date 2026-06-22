# Phase 9: Exhaustive RE — ROM Region 0x80050000-0x8005ffff

**Status**: PASS 1 (ENUMERATION ONLY) — 2026-06-22

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

## Thin-Named Functions (10 entries, pending decompile)

Kovah-named functions not yet decompiled in this enumeration pass. These require batch decompile + verification to upgrade to medium/high confidence.

Expected categories:
- Slot scheduler helpers and timing validators
- Feature-page hash-bucket operations
- Link-layer state machine utilities
- Packet-type negotiators and codec drivers

## Unnamed Functions (354 entries)

Cold-triage candidates identified from region structure:
- **Lower half** (0x80050000–0x80057fff): slot allocation, feature dispatch, timer chains
- **Upper half** (0x80058000–0x8005ffff): higher-level state machines, retry logic, link-event handlers

## Enumeration Strategy (Pass 1)

**Step 1**: Run `ListRegion0x80050000.java` (GZF process mode) to enumerate all 364 functions by size/name/type.

**Step 2**: Cross-reference output against:
- rom_function_index.md (confidence flags, existing names)
- rom/reverse_engineering_conn_feature_dispatch.md (feature cluster boundaries)
- rom/reverse_engineering_hardware_layer.md (slot budget + allocation cluster)

**Step 3**: Identify next priority batch for decompile:
- 10 thin-named functions (highest-ROI, already named by Kovah)
- Largest cold-named functions in each sub-region (lowest addresses, slot-alloc path)

## Next Steps (Self-Chaining)

If enumeration script completes successfully:

1. **Pass 2 (Decompile Batch)**: Run `BatchDecompileList.java` on 10 thin-named + top 5 largest unnamed
2. **Pass 2 (Rename)**: Use `RenameBatch1.java` to upgrade thin-named → medium confidence + give names to top unnamed
3. **Pass 3+ (Triage Loop)**: Continue region sweep until all 324+ unnamed reach medium+ confidence
4. **Final**: Update rom_function_index.md + analysis/INDEX.md; mark region [DONE] when all functions are high-confidence or explicitly marked non-real

---

## Tool Notes

- `ListRegion0x80050000.java`: Generic template (reuse of prior region scripts) — no stdout truncation expected for 364 functions
- GZF process mode: Renames made in prior passes (0x80000000, 0x80010000, 0x80020000, 0x80030000, 0x80040000) persist
- Timeline: FAST enumeration pass target = 5–6 minutes (script only, no decompiles)

## Coverage Progress

| Metric | Current | Target |
|--------|---------|--------|
| Region total functions | 364 | 364 |
| Already high-confidence | ~25 (6.9%) | 364 (100%) |
| Thin-named (decompile pending) | 10 (2.7%) | 0 (all high/medium) |
| Unnamed (cold triage) | 354 (97.3%) | 0 (all named) |

---

**NEXT**: Execute `ListRegion0x80050000.java` enumeration script (self-chain to Pass 2 if time permits, else stage for next overnight loop).
