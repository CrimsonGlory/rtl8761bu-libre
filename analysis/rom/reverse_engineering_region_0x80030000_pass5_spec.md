# Phase 9 Region 0x80030000 PASS 5 Specification

**Date:** 2026-06-24  
**Scope:** Decompile 6 tier-1 priority functions (600–2000B range)  
**Method:** Individual DiagAddr.java calls via GZF process mode  
**Expected outcome:** 5–6 new HIGH-confidence function names

## Tier-1 Functions to Decompile

| Priority | Address | Size | xref_count | Rationale | Expected Category |
|----------|---------|------|-----------|-----------|------------------|
| 1 | `0x8003d7bc` | 1524 B | 1 | LARGEST in tier | eSCO negotiator OR multi-VSC dispatcher OR power stack orchestrator |
| 2 | `0x80033f8c` | 930 B | 4 | HIGHEST XREF | Likely central dispatcher for multiple paths |
| 3 | `0x8003cb80` | 686 B | 4 | High xref, mid-size | Specialized sub-handler (mode negotiator, quality monitor) |
| 4 | `0x8003ec48` | 628 B | 4 | High xref, mid-size | Specialized sub-handler (state machine, connection logic) |
| 5 | `0x80037e28` | 932 B | 1 | Second-largest | Complementary orchestrator or standalone complex procedure |
| 6 | `0x80032540` | 2068 B | ? | Multi-VSC dispatcher | Special priority: verify scope vs. master VSC dispatcher @ 0x80030f1c |

## Analysis Framework

### Per-Function Decompile Checklist

For each function, analyze:

1. **High-level structure:**
   - Entry validation (parameter checks, guard conditions)
   - Call pattern (dispatch, state machine, sequence, fan-out)
   - Exit paths (return values, cleanup, error handling)

2. **Literal pool analysis:**
   - Data pointers (ROM addresses, RAM struct bases)
   - Constant masks, thresholds, lookup tables
   - Configuration register indices or opcodes

3. **Call targets (identify by address):**
   - ROM functions (0x8000xxxx) — use existing ROM function index
   - Patch functions (0x8010xxxx) — cross-ref against firmware hooks
   - Indirect calls via pointers (struct +offset) — trace struct definitions

4. **Struct field patterns:**
   - Field offset detection (loads from param+N, param1+M)
   - Struct size hints (loop bounds, array indices)
   - Known structs (conn_rec @ stride 0x1ac/0x2b8, capability structs)

5. **Opcode values or feature bits:**
   - Dispatch tables (switch cases, opcode ranges)
   - Feature gates (config_base offsets, capability bits, bos+offset masks)
   - Sub-opcodes (nested cases suggesting opcode sub-families)

### Cross-Reference with Pass 3 Findings

Pass 3 identified 10 functions (8 VSC handlers + 2 HCI cancellations). For tier-1 functions:

- **If a tier-1 function calls a Pass-3 VSC handler:** It's likely a dispatcher or orchestrator routing to those handlers
- **If a tier-1 function shares literal-pool offsets with Pass-3 functions:** It's part of the same subsystem (VSC, HCI, AFH, etc.)
- **If a tier-1 function is called by a Pass-3 function:** It's a callee, likely a helper or sub-handler

## Expected Naming Patterns

Based on tier-1 function sizes and xref counts:

| Address | Expected Pattern | Confidence Rationale |
|---------|-----------------|----------------------|
| `0x8003d7bc` | `*_orchestrator` or `*_dispatcher` or `*_manager` | Largest size suggests multi-step procedure or state machine |
| `0x80033f8c` | Dispatcher with multiple call paths | High xref (4) + size (930B) suggests central router |
| `0x8003cb80` | Sub-handler or feature negotiator | Mid-size + high xref suggests targeted feature (eSCO mode, power, AFH) |
| `0x8003ec48` | Utility or state-check function | Mid-size + high xref suggests repeated validation or branching |
| `0x80037e28` | Standalone complex procedure | Second-largest + size suggests independent major feature path |
| `0x80032540` | Multi-VSC dispatcher or router | 2068B is larger than master VSC (4372B) + high semantic value |

## Decompilation Commands

To execute this pass via MCP:

```
mcp__wairz__run_ghidra_headless \
  --binary-path "/root/DarkFirmware_real_i/00_Ghidra-analyzed_files/2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf" \
  --script-name "DiagAddr.java" \
  --script-args "0x8003d7bc" \
  --use-saved-project true
```

Repeat for each address: `0x80033f8c`, `0x8003cb80`, `0x8003ec48`, `0x80037e28`, `0x80032540`.

Alternative (batch script): Use `DecompileRegion80030000Pass5Tier1.java` for all 6 in single run (see /tmp/).

## Expected Output Structure

Each DiagAddr.java run should return:

1. Function name (current)
2. Function size (bytes)
3. Function disassembly (MIPS16e)
4. Decompiler C pseudocode
5. Literal pool references (with resolved addresses)
6. Caller/callee list (xref to/from)
7. Stack frame layout (if complex)

## High-Confidence Name Assignment Criteria

A function earns HIGH-confidence renaming if C decompile shows:

1. **Clear semantic purpose** (e.g., "loops opcode cases", "reads HCI parameter", "negotiates eSCO mode")
2. **Distinctive call patterns** (e.g., "calls 3 different ROM functions in sequence" → procedure)
3. **Recognizable ROM integration** (e.g., "calls send_LMP_pkt", "calls FUN_800115c8 register read", "calls cleanup chain")
4. **No ambiguity in structure** (e.g., not "mystery utility" or "unknown dispatcher")

Example naming decisions:

- `FUN_8003d7bc` → `eSCO_link_negotiator` if decompile shows LMP opcode 0x25 path + conn record writes
- `FUN_80033f8c` → `VSC_opcode_dispatcher_extended` if decompile shows 0xfc##/0xfd## case table
- `FUN_8003cb80` → `power_mode_selector` if decompile shows TX power config branches

## Success Criteria

✓ All 6 functions decompiled (exit code 0 from script runs)  
✓ Each function assigned a HIGH-confidence name based on C evidence  
✓ Names documented in analysis/rom/reverse_engineering_region_0x80030000.md (Pass 5 section)  
✓ RenameBatch2Region80030000Pass5.java created and executed (renames persist in GZF)  
✓ rom_function_index.md updated (confidence flags upgraded, counts recomputed)  
✓ analysis/INDEX.md updated (Pass 5 entry added)

## Next Steps (Pass 6+)

After Pass 5:
- Remaining unnamed: ~280–285 (6 upgraded to HIGH)
- Priority: Continue with next 20–30 largest (301–600B range, ~50 functions in tier)
- Strategy: Self-chain to Pass 5b (continue tier-1 residuals) or promote to Pass 6 (tier-2 medium-range)

