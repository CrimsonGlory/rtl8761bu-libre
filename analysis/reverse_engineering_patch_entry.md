# Reverse Engineering: `patch_entry` (`FUN_8010a000` region)

**Goal**: Replace vendor `.incbin` prefix `[0, 0x764)` in `full-inject-t3-vendor-tail` with
libre `init.S` + `bootstrap.S` that emits **identical** patch1 bytes.

**Sources**: non-free `rtl8761bu_fw.bin` patch1 @ file `0x3780`, GZF data block
`FUN_8010a000` decompile (wairz `DecompileAddr.java 0x8010a000`, 2026-06-09).

---

## Connect-critical prefix layout

UB500 bisect: only `[0, 0x764)` must match vendor for connect (with libre si2+ and
vendor tail). File offsets are patch-body relative (runtime = `0x8010A000 + off`).

| File range | Size | Ghidra / role |
|------------|------|----------------|
| `[0x0000, 0x0242)` | 578 B | **`FUN_8010a000`** — master installer code |
| `[0x0242, 0x05D8)` | 918 B | PC-relative **literal pool** for `FUN_8010a000` |
| `[0x05D8, 0x0724)` | 332 B | **Helper functions** (`FUN_8010a5d8`, `FUN_8010a6ec`, …) — executable, not padding |
| `[0x0724, 0x0754)` | 48 B | **`sub_installer_1`** (`FUN_8010af40`) |
| `[0x0754, 0x0764)` | 16 B | **`fn_bss_init`** (`FUN_8010a6c8`) |

**Total**: `0x764` = 1892 bytes.

### Libre vs vendor (2026-06-09 `make all`)

| Metric | Value |
|--------|-------|
| `patch_entry` symbol size (libre) | `0x61e` (1566 B) |
| `FUN_8010a000` code only (vendor) | `0x242` (578 B) |
| Prefix diffs `[0, 0x764)` | **1808 / 1892** |
| `patch_entry` diffs `[0, 0x61e)` | **1493 / 1566** |
| Code body diffs `[0, 0x242)` | **570 / 578** |
| Pool diffs `[0x242, 0x61e)` | **923** |

Libre `nm` layout (wrong):

```
0x8010a000  patch_entry     0x61e
0x8010a700  fn_bss_init     0x1c   ← vendor @ 0x754
0x8010a71c  sub_installer_1 0x30   ← vendor @ 0x724
```

Libre **omits** `[0x05D8, 0x0724)` helper code entirely and places early bootstrap
~0xE0 bytes too low.

---

## `FUN_8010a000` — semantic map (Ghidra)

Runtime `0x8010A000`, **578 bytes** of code; pool follows at `0x8010A242`.

**Prologue** (differs from libre `init.S`):

```asm
addiu sp, -0x28
sw    ra, 0x24(sp)
sw    s1, 0x20(sp)
sw    s0, 0x1c(sp)
```

Libre uses `addiu sp,-8` / `sw ra,4(sp)` only — frame size and saved regs differ.

**Phase order** (decompile):

1. Load 32-bit config word from `PTR_DAT_8010a244`; split to two `sh` stores
2. `jalr` → **`fn_bss_init`** via pool (`DAT_8010a254`) — **before** hook installs
3. Clear LSBit of `config_base+0xd8` (4× `sb` unaligned store)
4. First hook batch + `jalr` sub_installer_1/2 via pools (`DAT_8010a278`, `DAT_8010a27c`)
5. More DRAM hook installs (see Appendix D in `reverse_engineering_patch_installer.md`)
6. Clear bit 14 of `config_base+0xe0`
7. `sub_installer_3` … `sub_installer_6` via pools
8. More hooks; `bos_base+0x30`, `+0x50`, etc.
9. ROM register script: `(**PTR_DAT_8010a38c)(PTR_PTR_8010a390, 2)`
10. Chip-rev check (`DAT_8010a394`, table `PTR_DAT_8010a3a0`)
11. `fn_version_check`, `fn_tlv_applier`, `fn_bdaddr_sync`, conditional `fn_bb_init`
12. Final hooks; `*PTR_DAT_8010a3cc = 4` (patch-active flag)
13. Epilogue: restore `s0/s1/ra`, `jr ra`, `addiu sp,0x28`

Libre `init.S` adds **extra** prologue stores to `0x80120f0c` / `0x80120c80` (phase-zero
fix) that vendor `FUN_8010a000` does not perform — shifts all PC-relative offsets.

---

## Literal pool (`[0x0242, 0x05D8)`)

All `lw rx, imm(pc)` in `FUN_8010a000` target this region. Ghidra names pools
`PTR_DAT_8010a244`, `PTR_PTR_8010a258`, `PTR_FUN_8010bba4_1_8010a270`, etc.

Libre `LOAD_ADDR` / `INSTALL_HOOK_ABS` macros emit **per-use** mini-pools with
`b` skips (~12 B overhead each), so:

- Same semantics, **different** code size and pool placement
- Cannot match vendor bytes without either vendor machine-code transcription or
  a new macro set that emits compact MIPS16e matching Realtek's layout

---

## Helper region (`[0x05D8, 0x0724)`)

Not dead space. Example @ `0x8010A61e` (file `0x61e`):

- Ghidra: `li a0, 0x1fe` inside `FUN_8010a5d8` / related cluster
- Raw: `8ce80dea2cea40cb...`

`sub_installer_1` @ file `0x724` begins `ff 6a 4c e9` (`FUN_8010af40`).

This region must be present at the correct offset for any call/jump targets from
the main installer pool.

---

## Incremental libre plan

| Phase | Target range | Work |
|-------|--------------|------|
| **PE-0** | Linker layout | Anchor `fn_bss_init` @ `0x754`, `sub_installer_1` @ `0x724`, helpers @ `0x5D8` |
| **PE-1** | `[0, 0x0242)` | Transcribe `FUN_8010a000` prologue + phased logic to match Ghidra disasm |
| **PE-2** | `[0x0242, 0x05D8)` | Emit consolidated literal pool (single `.word` block, fixed order) |
| **PE-3** | `[0x05D8, 0x0724)` | RE helper functions; interim: vendor `.byte` blob with labels |
| **PE-4** | `[0x0724, 0x0764)` | Already have vendor machine code in `bootstrap.S` |
| **PE-5** | Hardware | `make all` + inject tail; byte-diff `0` on `[0,764)` then drop `VENDOR_EARLY_PREFIX` |

**Tooling**: `make diff-prefix` → `scripts/diff_vendor_prefix.py build/patch.bin`.

**Reference extract**: `scripts/extract_vendor_prefix.py NF_REF 0x764 build/vendor_prefix.bin`
(can sub-slice per region for PE-3 interim blobs).

---

## Key literal pool entries (first calls)

| Pool symbol (Ghidra) | Typical target |
|----------------------|----------------|
| `DAT_8010a254` | `fn_bss_init` @ `0x8010A754` |
| `DAT_8010a278` | `sub_installer_1` @ `0x8010A724` |
| `DAT_8010a27c` | `sub_installer_2` @ `0x8010A764` |
| `PTR_PTR_8010a258` | `bos_base` DRAM slot |
| `PTR_config_base_8010a24c` | config struct pointer |
| `PTR_DAT_8010a3cc` | patch-active flag (`0x80120538` in libre) |

---

## Related docs

- `reverse_engineering_patch_installer.md` — Appendix D hook map, `FUN_80103780` (parallel installer in **patch** block @ `0x3780`, not runtime entry)
- `reverse_engineering_libre_patch_layout.md` — PRAM section plan
- `work-in-progress.txt` — `[NEXT]` incremental `patch_entry` RE
