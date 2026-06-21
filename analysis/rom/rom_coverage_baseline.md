# ROM Coverage Baseline

Source: `RomCoverageStats.java` + `RomNamedFuncAddrs.java`, GZF process mode,
run 2026-06-21, against `2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf`
(`rom` memory block only, `0x80000000`–`0x8007ffff`).

Purpose: establish what fraction of the 512 KB mask ROM is already
disassembled/defined and named before starting systematic Phase 9 work
(see `work-in-progress.txt` Phase 9). Re-run both scripts periodically as
a progress metric; append new dated entries below rather than overwriting
this baseline.

---

## ROM block size

| Quantity | Value |
|----------|-------|
| Address range | `0x80000000` – `0x8007ffff` |
| Total size | `0x80000` bytes = 524288 bytes (512 KiB) |

## Function coverage

| Metric | Count | % of total functions |
|--------|-------|----------------------|
| Total functions defined | 2738 | 100% |
| User-named functions (not `FUN_*`/`LAB_*` auto-names) | 461 | 16.84% |
| Functions with a Ghidra comment | 0 | 0.00% |

461 of 2738 functions in the ROM block carry one of Kovah's (or our) manual
names; the remaining ~2277 are still auto-generated `FUN_8000xxxx` labels.
Zero functions currently have any plate/pre/end comment attached — all
documentation of *purpose* lives in `analysis/rom/*.md` and
`analysis/kovah_function_list.md`, not in Ghidra comments.

## Byte coverage

| Category | Bytes | % of 512 KiB |
|----------|-------|--------------|
| Function-body bytes (total) | 445333 | 84.94% |
| Function-body bytes inside a user-named function | 89102 | 16.99% |
| Code-unit bytes: instructions | 461046 | — |
| Code-unit bytes: defined data | 38984 | — |
| Code-unit bytes: **defined total** (instructions + data) | 500030 | **95.37%** |
| Code-unit bytes: **undefined** | 24258 | **4.63%** |

Interpretation:

- **95.37% of the ROM is "defined"** in Ghidra's code-unit sense — it has
  been disassembled as an instruction or declared as typed data. Only
  4.63% (≈23.7 KiB) of the address range remains raw undefined bytes.
- **84.94% of the ROM falls inside some function's body** (`total=445333`
  bytes across 2738 functions), but only **16.99%** of all ROM bytes
  (89102 bytes) are inside a function that has been given a real name —
  i.e. confirmed/understood at the "we know what this does" level, as
  opposed to merely disassembled.
- The gap between "defined" (95.37%) and "named" (16.99%) is the real
  measure of remaining Phase 9 work: most of the ROM is already correctly
  split into instructions/functions by Ghidra's auto-analysis, but the
  large majority of those ~2277 anonymous functions have not yet been
  triaged, decompiled, or written up.

## Named function address sample

`RomNamedFuncAddrs.java` enumerates all 461 named-function addresses in the
`rom` block (one `A|0xaddress` line per function). The full list was
captured in the headless run log; representative low/high addresses from
that run:

- Lowest: `0x800009c0`
- Highest (sampled): `0x80079e6c`-range (run continues to the top of the
  populated ROM region; full address list is reproducible by re-running
  `RomNamedFuncAddrs.java` — see `GHIDRA_SCRIPTS.md`)

This list is the practical work queue for the "Consolidate partially-resolved
ROM functions" and "Produce a comprehensive ROM function index" TODOs: cross
reference against `analysis/kovah_function_list.md` to find which named
functions still lack a dedicated write-up in `analysis/rom/`.

## How to re-run

```python
mcp__wairz__run_ghidra_headless(
    binary_path="2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf",
    script_name="RomCoverageStats.java",
    use_saved_project=True,
    timeout=300,
)
mcp__wairz__run_ghidra_headless(
    binary_path="2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf",
    script_name="RomNamedFuncAddrs.java",
    use_saved_project=True,
    timeout=300,
)
```

Both scripts only read the project (no annotations are modified), so they're
safe to re-run at any time as a progress check.

## History

| Date | Total fns | Named fns | Named % | Defined bytes % | Undefined bytes % |
|------|-----------|-----------|---------|------------------|--------------------|
| 2026-06-21 (morning baseline) | 2738 | 461 | 16.84% | 95.37% | 4.63% |
| 2026-06-21 (end of day) | 2739 | 461 | 16.83% | 95.37% | 4.63% |

Same-day re-run, after this session's boot/reset sequence, interrupt vector,
HCI command router, LC/LMP state machine, BLE link layer, encryption engine,
USB transport driver, 33-function consolidation, and function-index work.
Practically **unchanged** from the morning baseline: total function count
moved by +1 (Ghidra's auto-analysis split one more function during this
session's work — likely a side effect of decompiling/cross-referencing
nearby code, not a manual rename), and the named-function count stayed
exactly at 461, so named % moved by only -0.01 (2738→2739 denominator, same
numerator). Byte-coverage figures (defined/undefined/named-bytes) are
bit-for-bit identical to the morning run.

Interpretation: today's Phase 9 work was concentrated on *writing up and
cross-referencing* functions that were already named in the morning baseline
(consolidation, function-index, prose documentation) rather than naming new
previously-anonymous `FUN_*` functions, so the raw named-count metric does
not move even though real documentation progress happened. The function-index
doc (`analysis/rom/rom_function_index.md`) is the more precise record of that
day's qualitative progress (high/medium/low confidence tiers per named
function); this coverage table tracks only the coarse named-vs-anonymous
split.
