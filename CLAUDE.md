# RTL8761BU Libre Firmware — Project Context for Claude

## Goal

Produce a free/libre replacement for `rtl8761bu_fw.bin` suitable for inclusion in
linux-libre. The non-free binary is a runtime patch for the RTL8761BU Bluetooth 5.1
USB chip (UB500 dongle). The Linux `btrtl` driver loads it at boot via HCI VSC `0xFC20`.

This work continues Xeno Kovah's (Dark Mentor LLC) public reverse-engineering research
presented at Hardwear.io NL 2025.

---

## Reverse-Engineering Tool Stack

All disassembly and decompilation is done through **wairz** (MCP server) using
`mcp__wairz__run_ghidra_headless`.

**IMPORTANT — wairz modifications**: If any limitation in wairz blocks analysis
(missing tool, unsupported mode, wrong behavior), do NOT try to work around it
silently. Instead, tell the user explicitly what change is needed in wairz and
they will implement it. Do not attempt to patch wairz yourself.

### Primary binary

```
2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf
```

This is Kovah's annotated Ghidra project archive (.gzf). It contains **three memory
blocks**:

| Block | Address range | Content |
|-------|--------------|---------|
| `patch` | `0x00000000 – 0x0000adc3` | Patch firmware at file-relative addresses |
| `data`  | `0x80100000 – 0x8013ffff` | Runtime RAM image (patch loaded by ROM) |
| `rom`   | `0x80000000 – 0x8007ffff` | ROM dump with Kovah's annotations |

**Use this GZF as the binary_path**, not the raw `rtl8761bu_fw.bin`.

### Address mapping

When the chip runs, the patch is loaded at `0x80100000`. The patch file itself is
44484 bytes (`0x0000 – 0x0000adc3`). File-relative address to runtime:

```
runtime = 0x80100000 + file_offset
```

ROM functions are at `0x8000xxxx` in both the GZF and on the real chip.

### Ghidra scripts

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

Existing scripts:

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

**IMPORTANT — keep this table current**: any time a new Ghidra script is added
to `/root/wairz/ghidra/scripts/` (via `mcp__wairz__save_ghidra_script` or
written directly), add a row to the table above in the same turn — script name
+ one-line purpose. Do this immediately, not as a follow-up. Undocumented
scripts are invisible to future sessions and the table silently drifts out of
date (as happened with 27 scripts before this rule was added).

To save a new script: use `mcp__wairz__save_ghidra_script`, then reference it by
`script_name`.

---

## Key Runtime Addresses

| Address | Description |
|---------|-------------|
| `0x80103780` | Master patch installer (`thing_that_calls_thing_that_installs_LMP_Patch`) |
| `0x8010bba4` | LMP VSC hook (installed at `bos_base+0xd8`) |
| `0x8004f824` | ROM: slot-budget validator + hardware hook dispatcher |
| `0x801212e4` | **RAM hook slot**: checked by `FUN_8004f824`; patch installs hw-write fn here |
| `0x801212e0` | RAM hook slot: checked by ROM `FUN_80050810` |
| `0x80121200` | Approx. `bos_base` (BT state struct base; `bos_base+0xd8 = 0x801212d8`) |
| `0x8000fd38` | ROM: `copies_config_bdaddr` — reads BD_ADDR from config blob |
| `0x8001136c` | ROM: HW register-read fn (used by FUN_80009980 via thunk/s0) |
| `0x8001139c` | ROM: HW register-write fn (used by FUN_80009980 via s1) |
| `0x80109980` | HW register init (programs 7 baseband regs; reads bos+0x168/0x16a) |
| `0x80109c08` | eSCO packet type negotiator (connection types 0xA000/0xB000/0xE/0xF000) |
| `0x80109de0` | Hamming-weight checksum (popcount of first 10 bytes) |
| `0x8010A160` | MIPS16e handler stubs: 8-byte bteqz/lw/bteqz/addiu pattern, 36 entries |
| `0x801000A0` | Address-pair table (15 × 8-byte entries: data_ptr, fn_ptr) |
| `0x80009990` | ROM: `interesting_string_user_fptr_registration_function` (table processor) |

## Key Struct Offsets (`bos_base` / `puVar6`)

| Offset | Hook installed |
|--------|---------------|
| `+0x1c` | `LAB_8010bc74` |
| `+0xd8` | LMP VSC dispatch → `LAB_8010bba4` |
| `+0xe0` | `FUN_80050810` type-dispatch hook |
| `+0xe4` | `FUN_8004f824` hardware-write hook ← **target of current investigation** |
| `+0x20/0x24` | HCI event handlers |
| `+0x30` | Connection handler → `LAB_8010c088` |
| `+0x50` | `LAB_8010f884` |

---

## Findings — Already Documented

All findings go in `/root/rtl8761bu-libre/analysis/`.

| File | What it covers |
|------|---------------|
| `kovah_function_list.md` | Full list of Kovah's annotated names |
| `reverse_engineering_patch_installer.md` | Master installer `FUN_80103780`: all 50+ fptr installs, 6 sub-installers, phase-by-phase |
| `reverse_engineering_hardware_layer.md` | `FUN_8004f824`, hook at `0x801212e4`, eSCO slot-budget table |
| `reverse_engineering_lmp_vsc_hook.md` | `FUN_8010bba4` — LMP VSC hook entry point |
| `reverse_engineering_vsc_dispatcher.md` | VSC opcode dispatch table |
| `reverse_engineering_conn_record_subsystem.md` | Connection record struct layout |
| `reverse_engineering_rom_regs.md` | ROM HW register r/w protocol (0x8001136c/9c, MMIO 0xb000a0bc) |

---

## Firmware Format (raw `rtl8761bu_fw.bin`)

- 42088 bytes total
- Header: 9-byte magic `Realtechk`, 4-byte fw_version, `num_patches=2`, `0x30` = code start
- Master installer at file offset `0x3780` (runtime `0x80103780`)
- Loaded by Linux `btrtl` driver via HCI VSC `0xFC20`
- Config blob (BD_ADDR + HW config) appended after binary; read by ROM `copies_config_bdaddr`

---

## Non-free reference files

| Path | Notes |
|------|-------|
| `rtl8761bu-non-free/rtl8761bu_fw.bin` | Original Realtek binary (for diff/reference only) |
| `rtl8761bu-libre/rtl8761bu_fw.bin` | Current libre build output |
| `rtl8761bu-libre/build/patch.bin` | Compiled patch blob |

Build system is in `rtl8761bu-libre/` (Makefile + MIPS16e toolchain, Dockerized).

---

## ISA Notes

- MIPS16e (16-bit compressed MIPS). All chip code is MIPS16e.
- PC-relative `lw rx, imm(pc)` loads from literal pool embedded in code.
- 32-bit stores use 4 sequential `sb` instructions (unaligned peripheral workaround).
- Branch delay slots present after `jalr`/`jr`.
