# Phase 9 PASS 5 Execution Manifest
**Region:** 0x80030000-0x8003ffff  
**Date Prepared:** 2026-06-24  
**Status:** Ready for MCP execution  
**Prepared by:** Claude Code (claude-code session)

## Summary
Tier-1 decompilation for 6 high-priority functions (600–2000B range). All preparation complete; awaiting MCP tool invocation.

## 6 Target Functions

| Priority | Address | Size | xref | Description | Binary File |
|----------|---------|------|------|-------------|------------|
| 1 | `0x8003d7bc` | 1524 B | 1 | LARGEST orchestrator | GZF USB |
| 2 | `0x80033f8c` | 930 B | 4 | HIGHEST XREF dispatcher | GZF USB |
| 3 | `0x8003cb80` | 686 B | 4 | Mid-size handler | GZF USB |
| 4 | `0x8003ec48` | 628 B | 4 | Utility/state function | GZF USB |
| 5 | `0x80037e28` | 932 B | 1 | Second-largest procedure | GZF USB |
| 6 | `0x80032540` | 2068 B | ? | Multi-VSC dispatcher (special) | GZF USB |

## Execution Parameters

**Binary (GZF File):**
```
Path: /root/DarkFirmware_real_i/00_Ghidra-analyzed_files/2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf
Mode: GZF process mode (use_saved_project=True)
Note: Cached Ghidra project persists function renames across runs
```

**Script:** `DiagAddr.java` (standard Ghidra scripts directory)

**Invocation Template:**
```
Tool: mcp__wairz__run_ghidra_headless
Parameters:
  binary_path: /root/DarkFirmware_real_i/00_Ghidra-analyzed_files/2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf
  script_name: DiagAddr.java
  script_args: [<ADDRESS>]        # Use 0x-prefixed hex, e.g., "0x8003d7bc"
  use_saved_project: True
  timeout: 300 (seconds)
```

**Sequence:**
Execute 6 separate invocations, one for each address:
1. mcp__wairz__run_ghidra_headless(..., script_args=["0x8003d7bc"], ...)
2. mcp__wairz__run_ghidra_headless(..., script_args=["0x80033f8c"], ...)
3. mcp__wairz__run_ghidra_headless(..., script_args=["0x8003cb80"], ...)
4. mcp__wairz__run_ghidra_headless(..., script_args=["0x8003ec48"], ...)
5. mcp__wairz__run_ghidra_headless(..., script_args=["0x80037e28"], ...)
6. mcp__wairz__run_ghidra_headless(..., script_args=["0x80032540"], ...)

## Expected Output (per invocation)

Each DiagAddr.java run will produce ~300-800 lines of output:

```
Function Information
  Address: 0x8003d7bc
  Name: FUN_8003d7bc
  Size: 1524 bytes
  Source: AUTO_GENERATED

MIPS16e Disassembly
  [50-150 lines of assembly with branch analysis]

Decompiled C Pseudocode
  [100-300 lines of Ghidra decompiler C output]

Literal Pool
  [10-30 lines of resolved data/function pointers]

Caller/Callee Analysis
  Callers: [list of functions]
  Callees: [list of functions]
```

## Post-Execution Workflow

### Phase 1: Decompile Output Analysis
1. Capture all 6 outputs to text files
2. For each function:
   - Extract high-level semantic intent from C pseudocode
   - Identify opcode dispatches, state machines, configuration writes
   - Cross-reference caller/callee against Pass 3 findings (VSC handlers, HCI commands)
   - Classify as one of:
     - **VSC Sub-Handler:** Dispatch on 0xfc##/0xfd## opcode range
     - **Dispatcher:** Routes multiple call paths based on params or mode
     - **State Machine:** Multi-case logic on connection/feature state
     - **Negotiator:** Selects mode/parameter from capability flags
     - **Manager:** Allocates/deallocates resources
     - **Utility:** Shared helper function

### Phase 2: Naming Assignment (HIGH-confidence)
Example decision tree:

**0x8003d7bc (1524B, LARGEST)**
- If decompile shows: "large switch on opcode bits" → `VSC_extended_opcode_dispatcher`
- If decompile shows: "multi-step LMP procedure" → `LMP_eSCO_negotiator` or `eSCO_link_manager`
- If decompile shows: "connection record loop + mode selection" → `connection_mode_selector` or `feature_negotiator`

**0x80033f8c (930B, HIGHEST XREF=4)**
- If high xref + medium size: Likely central dispatcher
- Name: `[subsystem]_[opcode/mode]_dispatcher` or `[subsystem]_router`

**0x8003cb80, 0x8003ec48, 0x80037e28**
- Follow same pattern: C code → semantic purpose → HIGH-confidence name

**0x80032540 (2068B, Multi-VSC)**
- Special case: Already named as "multi-VSC" dispatcher
- Confirm scope vs. 0x80030f1c master dispatcher (4372B)
- Possible scenarios:
  - Alternate entry point for subset of VSC opcodes (vendor-specific range 0xfd##/0xfe##)
  - Fallback dispatcher for vendor extensions not in master table
  - Sub-handler for one major VSC family

### Phase 3: Create Rename Batch Script
File: `RenameBatch2Region80030000Pass5.java` (template in /tmp/DecompileRegion80030000Pass5Tier1.java)

```java
// Example structure:
// Given confirmed names from Phase 2 analysis:
renames = [
  {"oldName": "FUN_8003d7bc", "newName": "eSCO_link_negotiator_or_dispatcher", "confidence": "HIGH"},
  {"oldName": "FUN_80033f8c", "newName": "[confirmed name]", "confidence": "HIGH"},
  ...
]
// Script applies via GZF process mode, persists to cached project
```

### Phase 4: Apply Renames + Verify
1. Execute RenameBatch2Region80030000Pass5.java via MCP
2. Verify persistence:
   - Re-run DiagAddr.java on one sample function (e.g., 0x8003d7bc)
   - Confirm new name appears in output
3. Document rename confirmations

### Phase 5: Update Documentation
1. **analysis/rom/reverse_engineering_region_0x80030000.md**
   - Add "## Pass 5 Status" section
   - Document each renamed function: address, new name, C code evidence, confidence
   - Update coverage metrics (named → named+6, high-confidence → high+6, unnamed → unnamed-6)

2. **analysis/rom/rom_function_index.md**
   - Add 6 new rows for renamed functions (HIGH confidence)
   - Recompute summary counts
   - Update region_0x80030000 row: distribution percentages

3. **analysis/INDEX.md**
   - Update region_0x80030000.md summary line (if coverage crossed a threshold)

## Files Prepared (in this session)

| File | Purpose | Status |
|------|---------|--------|
| `/tmp/DecompileRegion80030000Pass5Tier1.java` | Batch decompile script (all 6 targets) | ✓ Created |
| `/tmp/execute_pass5_decompilation.sh` | Execution documentation + parameter generator | ✓ Created |
| `analysis/rom/reverse_engineering_region_0x80030000_pass5_spec.md` | Detailed Pass 5 specification & framework | ✓ Created |
| `analysis/rom/PASS5_EXECUTION_MANIFEST.md` | This file (execution manifest + workflow) | ✓ Created |
| `/tmp/pass5_execution_plan.txt` | High-level execution plan | ✓ Created |

## Success Criteria

✓ All 6 functions decompiled (MCP tool exit code 0)  
✓ Each function assigned HIGH-confidence name based on C decompile evidence  
✓ RenameBatch2Region80030000Pass5.java created & executed  
✓ Renames verified to persist in GZF project  
✓ Documentation updated (region_0x80030000.md, rom_function_index.md, INDEX.md)  
✓ Coverage progress: 27→33 named functions (8.7%→10.7% of 309)

## Timeline Estimate

- Decompilation (6 × DiagAddr runs): ~30–60 seconds
- Analysis + naming decision (Phase 2): ~15–30 minutes
- Rename batch creation + execution: ~10–15 minutes
- Documentation updates: ~10–20 minutes
- **Total:** ~1–2 hours (mostly human analysis time)

## Blocker Resolution

This Pass 5 is **[BLOCKED]** pending MCP tool execution. All preparation is complete.
To proceed: User or harness invokes mcp__wairz__run_ghidra_headless with parameters documented above.

---

**Next Pass After Pass 5:** PASS 6 (Tier-2 medium-range, 301–600B, ~50 functions)
