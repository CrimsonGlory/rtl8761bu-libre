# Ghidra Scripts Index

This table indexes the scripts physically present in `/root/wairz/ghidra/scripts/`
— the ones resolvable by `script_name`. They can be run with:

```python
mcp__wairz__run_ghidra_headless(
    binary_path="2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf",
    script_name="MyScript.java",     # filename already in /opt/ghidra_scripts/
    timeout=300,
)
```

No `processor`, `loader`, `base_addr`, or `setup_script` needed — the GZF already
encodes all that. `script_args` with `0x`-prefixed hex literals work fine for at
least `DiagAddr.java`/`DecompileAddr.java` (verified 2026-06-21 across ~6 tickets)
— the older blanket "hex literals in script_args are broken" claim does not hold
for every script; try it first and only hardcode addresses inside the script if
you actually hit a `NumberFormatException` for the specific script you're using.

**Adding a NEW script (status as of 2026-06-22 — partially fixed, was fully
broken on 2026-06-21):**
- `mcp__wairz__save_ghidra_script` writes to a separate UUID-keyed store, NOT to
  `/root/wairz/ghidra/scripts/`. A script saved this way is **never** reachable via
  `script_name` — neither a brand-new filename nor an overwrite of one of the
  filenames in the table below. Confirmed both ways on 2026-06-21 and re-confirmed
  (overwrite case) on 2026-06-22.
- **What now works (fixed between two checks ~25 min apart on 2026-06-22):** run a
  newly-`save_ghidra_script`'d file via `script_file_id=<uuid>` (the ID
  `save_ghidra_script` returns) instead of `script_name`. This executes correctly
  from a fresh temp dir.
- **What still does NOT work:** overwriting an *existing* filename (one already in
  the table below) via `save_ghidra_script` and expecting `run_ghidra_headless`
  to pick up the new content — `script_name` always runs the original on-disk
  version regardless of what's been saved over it via the UUID store. If an
  existing script needs different logic, save the replacement under a **new**
  filename and run it via `script_file_id`; don't overwrite the old name.
- Net effect: you CAN add new script capability mid-session now (new name +
  `script_file_id`), but a script's `script_name` entry in the table below, once
  it exists in `/opt/ghidra_scripts/`, is effectively immutable through the MCP
  surface.
- **Confirmed working end-to-end 2026-06-22** (Phase 9 region-0x80000000 sweep):
  saved 4 new scripts via `save_ghidra_script` (`ListRegion0x80000_Gaps.java`,
  `Region0x80000StringXrefs.java`, `BatchDecompileList.java`,
  `RenameBatch1.java`), ran each via `script_file_id` against the GZF with
  `use_saved_project=True` — all executed correctly. Also confirmed
  **function renames made via a `script_file_id` script persist** to a
  *separate, later* `run_ghidra_headless` call (different script) against
  the same cached GZF project — i.e. the GZF-process-mode project cache is a
  real persistent Ghidra project, not re-imported fresh each call.
- **Research-script store is a shared, capped pool** (`list_ghidra_research_files`
  showed ~50 entries as of 2026-06-22, including an explicit
  `ProbeCapTest20260622.java` cap probe from earlier in the day) — it holds
  *all* `.py`/`.java` research scripts across the whole project's history, not
  just Ghidra ones tied to this binary. Before saving a new script, consider
  `mcp__wairz__list_ghidra_research_files` to see how close to the cap the
  pool is; there is still no delete/evict tool, so the pool only grows.

| Script | Purpose |
|--------|---------|
| `Mips16eSetup.java` | ISA pre-script (auto-used for raw .bin loads) |
| `ExtractAnnotations.java` | Dumps Kovah's function/label names |
| `DecompileAddr.java` | Decompiles a single function by address |
| `DecompileFunction.java` | Decompiles by name |
| `ListAllFunctions.java` | Lists all known functions |
| `ListMemBlocks.java` | Lists memory blocks (sanity check) |
| `FindXrefsTo.java` | Finds cross-references to `0x801212e4` (`FUN_8004f824` HW write hook RAM slot). Restored to this original content 2026-06-22 after being used twice as a `save_ghidra_script` overwrite-bug probe (briefly held unrelated test content both times, neither of which ever actually ran via `script_name` — see the "Adding a NEW script" note above) |
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

## Reusable research scripts (UUID-keyed, run via `script_file_id`)

Most `save_ghidra_script`'d files in `list_ghidra_research_files` are
one-off/single-ticket scripts (hardcoded addresses, named for the specific
investigation that created them) and aren't worth indexing here. The
following, saved during the 2026-06-22 Phase 9 region sweep, are **generic**
(take addresses via `script_args`) and reusable by any future region-sweep
ticket — look them up by name via `list_ghidra_research_files` to get the
current UUID (UUIDs are not stable across saves of the same filename):

| Filename | Purpose |
|----------|---------|
| `BatchDecompileList.java` | Decompiles a comma-separated list of hex addresses (`script_args[0]`), printing each function's signature, direct callees, and decompiled C. Avoids one-address-per-tool-call overhead — used for all Phase 9 region-sweep batch triage going forward. |
| `RenameBatch1.java` | Renames functions given a `;`-separated list of `0xADDR=newName` pairs (`script_args[0]`). Prints old/new name for each for verification. |
| `ListRegion0x80000_Gaps.java` | Lists every function (named+unnamed) in two hardcoded address ranges with size/name/source-type. Template for "list all functions in range X" — copy and edit the two range constants for a different region. |
| `Region0x80000StringXrefs.java` | Lists every defined string in the `rom` block plus the functions that reference it. Useful for string-driven correlation in *other* regions (this region itself turned out to have almost no in-range strings — see `reverse_engineering_region_0x80000000.md`). |

These 4 scripts pushed the shared research-script pool close to its
informally-observed ~50-entry cap (see the cap-probe note above) — if a
future ticket hits a save failure, these 4 (now superseded by direct reuse)
are reasonable candidates to treat as "already served their one-off purpose"
since their logic is generic enough to recreate cheaply if ever evicted.
