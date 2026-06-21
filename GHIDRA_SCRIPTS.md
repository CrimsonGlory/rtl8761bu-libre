# Ghidra Scripts Index

Scripts live in wairz at `/root/wairz/ghidra/scripts/`. They can be run with:

```python
mcp__wairz__run_ghidra_headless(
    binary_path="2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf",
    script_name="MyScript.java",     # filename in scripts dir
    timeout=300,
)
```

No `processor`, `loader`, `base_addr`, or `setup_script` needed — the GZF already
encodes all that. Do NOT pass `script_args` with hex literals (`0x...`); hardcode
addresses inside the script instead (Ghidra's `parseOptions` rejects `0x` prefixes).

To save a new script: use `mcp__wairz__save_ghidra_script`, then reference it by
`script_name`.

| Script | Purpose |
|--------|---------|
| `Mips16eSetup.java` | ISA pre-script (auto-used for raw .bin loads) |
| `ExtractAnnotations.java` | Dumps Kovah's function/label names |
| `DecompileAddr.java` | Decompiles a single function by address |
| `DecompileFunction.java` | Decompiles by name |
| `ListAllFunctions.java` | Lists all known functions |
| `ListMemBlocks.java` | Lists memory blocks (sanity check) |
| `FindXrefsTo.java` | Cross-references to a target address |
| `FindStringRefs.java` | Finds references to a string |
| `DiagAddr.java` | Dumps bytes + disasm around an address |
| `GlobalLayout.java` | Shows global data layout |
| `StackLayout.java` | Shows stack frame layout |
| `TaintAnalysis.java` | Taint propagation helper |
| `DecompileMasterInstaller.java` | Decompiles `FUN_80103780` (now superseded by docs) |
| `DecompileInstallerC.java` | C-only decompile of installer + literal pool dump |
| `ScanStoreOffsets.java` | Scans all insns for stores with offsets 0xe4/0xe0/0xd8/0xe8 |
| `DecompileEntryAndTable.java` | Lists late fns, decompiles entry, dumps address-pair table |
| `DecompilePatchEntry.java` | Force-creates + decompiles patch entry region |
| `DecompileLateCode.java` | Decompiles FUN_80009980, 9c08, 9de0 + fn_ptr raw dump |
| `DecompileFnPtrs.java` | Decompiles FUN_80009200, 9550, 96d4, 9824, 9de0 + literal pool |
| `FindTableProcessor.java` | Searches for references to address-pair table 0x801000A0 |
| `AnalyzeBinary.java` | Full single-pass extraction of all data from the binary |
| `AnalyzeHwWriteHook.java` | Analyzes ROM hardware-write hook FUN_80025b68 + codec templates |
| `ReportBinarySize.java` | Reports exact loaded binary size + header bytes |
| `FindGzf.java` | Locates Kovah's .gzf archive on the filesystem |
| `FindXrefsA410.java` | Finds xrefs to FUN_8010a410 |
| `DumpA410Variants.java` | Compares FUN_8010a410 against vendor offset 0x468 |
| `DumpDecompileA410.java` | Dumps + decompiles FUN_8010a410 (86B) |
| `DumpC780Pool.java` | Dumps FUN_8010c780 literal pool + xref targets |
| `DumpC780Vendor.java` | Disassembles FUN_8010c780 at vendor patch1 bytes, follows lw pool |
| `DumpFn10868.java` | Dumps GZF byte-identical body of FUN_80110868 (322B) |
| `DumpFn10ca4.java` | Dumps GZF byte-identical body of FUN_80110ca4 (178B) |
| `DumpFnAa58.java` | Dumps GZF byte-identical body of FUN_8010aa58 (96B) |
| `DumpLiteralPool.java` | Dumps literal pool of master installer FUN_80003780 (0x800039f4–0x80003bc0) |
| `DumpStubsAndDescriptors.java` | GZF process-mode dump across all 3 memory blocks of stubs/descriptors |
| `Decompile10ca4.java` | Decompiles FUN_80110ca4 @ 0x80110CA4 |
| `DecompileCA20CC94.java` | GZF process-mode: full analysis of index-0 dispatch slot FUN_8010cc94 |
| `DecompileDEA2.java` | Decompiles the function containing 0x8010dea2 |
| `DecompileHookFnsBatch.java` | Decompiles 8 hook functions from FUN_8010a84c literal pool (data block) |
| `DecompileMasterAndSubs.java` | Decompiles master installer at Ghidra addr 0x80003780; scans all SW insns w/ offset 0xe4 |
| `DecompileNewHookFns.java` | Decompiles FUN_80110869 + 8 hook-target fns from FUN_8010a84c literal pool |
| `DecompilePostDispatch.java` | GZF process-mode: decompiles 3 post-dispatch fptrs not in the 36-entry BEQZ chain |
| `DecompileRegFns.java` | GZF process-mode: decompiles LMP PDU registration backend FUN_80075ee0 |
| `DecompileRemainingNewFns.java` | GZF process-mode: decompiles remaining fns from FUN_8010a000 + 6 protocol dispatch handlers |
| `DecompileRom9990.java` | GZF process-mode: decompiles ROM FUN_80009990 (table processor) |
| `DecompileStringAssocAndNew.java` | GZF process-mode: decompiles FUN_8010e27c (string-associated patch installer) |
| `DecompileSubInstallers.java` | Decompiles all 6 sub-installers called by the master patch installer |
| `DecodeStubChain.java` | GZF process-mode: analyzes MIPS16e dispatch chain at patch 0xa080-0xa280 + new fn_ptr handlers |
| `DiagHookTable.java` | Diagnoses hook table area 0x801212d0-0x80121320: symbols, raw bytes, code units, xrefs |
| `RomCoverageStats.java` | Computes ROM byte coverage: disassembled/defined vs undefined bytes, named vs anonymous functions |
| `RomNamedFuncAddrs.java` | Lists addresses of all user-named functions within the rom block only |

**Keep this table current**: any time a new Ghidra script is added to
`/root/wairz/ghidra/scripts/` (via `mcp__wairz__save_ghidra_script` or written
directly), add a row to the table above in the same turn — script name + one-line
purpose. Do this immediately, not as a follow-up. Undocumented scripts are
invisible to future sessions and the table silently drifts out of date (as
happened with 27 scripts before this rule was added).
