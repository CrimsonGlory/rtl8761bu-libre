# Linux-libre Blob Policy Compliance Audit

**Date:** 2026-06-09  
**WIP item:** Phase 7 — Run compliance checks against linux-libre blob policy  
**Tool:** `rtl8761bu-libre/scripts/compliance_check.py` (`make compliance`)

---

## Policy summary

[GNU Linux-libre](https://www.gnu.org/s/linux-libre) ships only **Free** software. For
firmware loaded by the kernel (`request_firmware` / `btrtl` FC20 download), that means:

| Requirement | This project |
|-------------|--------------|
| Shipped `.bin` contains no proprietary bytes | **FAIL** (see below) |
| Build from Free source only | **PARTIAL** — default `make all` still pulls vendor bytes |
| No build-time dependency on non-free inputs for release builds | **FAIL** — `NF_REF` required for PE-1 incbin |
| SPDX / GPL-2.0-or-later on all sources | **PASS** (after 2026-06-09 header sweep) |
| Reproducible output from clean tree | **PASS** — `make all` SHA256 stable in Docker |

Silicon ROM (`0x80000000`) is **not** a linux-libre blob — only the filesystem-loaded
`rtl8761bu_fw.bin` is in scope.

---

## Audit results by build profile

### `make all` (default `full`)

| Artifact | SHA256 (2026-06-09 Docker) |
|----------|----------------------------|
| `rtl8761bu_fw.bin` | `0900fd1dc0aa3ec41793651a2287c0045086a0c468f2f408b90dd0ef0b02ffd7` |

| Region (patch1 file offset) | vs vendor | Notes |
|-----------------------------|-----------|-------|
| `[0x0000, 0x0242)` PE-1 entry | **0 diffs** | **578 B vendor** via `patch_entry_code.S` `.incbin` |
| `[0x0242, 0x05D8)` literal pool | 0 diffs | Libre `patch_entry_pool.S` (transcribed constants) |
| `[0x05D8, 0x0724)` helpers | 0 diffs | Libre `patch_entry_helpers.S` |
| `[0x0724, 0x0764)` si1 + bss | 0 diffs | Libre |
| `[0x0764, 0x0820)` sub_inst_2 | 158/188 diffs | Mostly libre |
| `[0x0820, 0x0E4C)` tail | 1559/1580 diffs | Libre installer tail (in progress) |
| `[0x0E4C, 0x6CA0)` NOP pad | 23153/24148 diffs | Libre MIPS16e NOP fill (not vendor code) |
| **patch0** `@0x30` (14 048 B) | **identical** | **Copied verbatim from `NF_REF`** in `pack.py --dual` |

**Verdict:** Not linux-libre eligible. Non-free surface ≈ **14 048 + 578 = 14 626 B**
minimum in shipped image, plus any pool bytes that are still bitwise-identical to vendor
without a libre transcription trail.

### `make full-inject-t3-pe5-vendor-tail` (hardware PASS profile)

| Artifact | SHA256 |
|----------|--------|
| `rtl8761bu_fw.bin` | `7f051e6480ca7f3abe8d1fd16d21557ee0b4e7108a67cb31cd112aaa1a633bab` |

| Region | vs vendor |
|--------|-----------|
| `[0x0000, 0x0E4C)` prefix | 1404/3660 diffs — libre prefix + T3 hook overlays |
| `[0x0E4C, 0x6C9C)` tail | **0/24144 diffs** — **100 % vendor** (`VENDOR_TAIL_FILL`) |
| patch0 | identical to `NF_REF` |

**95.0 %** of patch1 bytes match vendor. This profile is for **UB500 validation only**;
it must not ship in linux-libre.

### `make minimal` (P0 smoke)

FC20 smoke test only. **26 692 / 27 808** patch1 bytes differ from vendor. Still includes
vendor patch0 via `pack.py --dual`.

---

## Build-time non-free dependencies

| Dependency | Used by | Release impact |
|------------|---------|----------------|
| `NF_REF` / `rtl8761bu-non-free/rtl8761bu_fw.bin` | `build/vendor_entry_code.bin`, `pack.py` patch0, all `inject-*` / `hybrid` / `test-nf` | **Blocks** clean-room build |
| `src/patch_entry_code.S` `.incbin` | Default `make all` | Embeds 578 B vendor machine code |
| `src/installer_vendor_early.S` `.incbin` | `VENDOR_EARLY_PREFIX=*` profiles | Entire early prefix from vendor |
| `inject_vendor.py` + manifest | `full-inject-t*` | Overlays up to ~95 % vendor patch1 |

`make all` **fails** if `NF_REF` is absent (`build/vendor_entry_code.bin` rule).

---

## License headers

All `src/*.S`, `scripts/*.py`, and top-level `*.py` now carry:

```
SPDX-License-Identifier: GPL-2.0-or-later
```

Verified by `make compliance` / `compliance_check.py`.

---

## Reproducibility

Two consecutive `make clean all` runs in Docker (same `NF_REF`) produced identical
`rtl8761bu_fw.bin` SHA256. Reproducibility is **conditional on the same vendor reference
file** for PE-1 incbin and patch0.

---

## Remediation roadmap (linux-libre ship criteria)

Ordered by impact:

1. **patch0** — Ship single-patch mode (`pack.py` without `--dual`) or replace chip_id=1
   body with a libre NOP stub; UB500 uses patch1 only but envelope still carries patch0 today.

2. **PE-1 `[0, 0x242)`** — Finish MIPS16e transcription of `FUN_8010a000` prologue;
   delete `patch_entry_code.S` incbin and `build/vendor_entry_code.bin` Makefile rule.

3. **Installer tail `[0x820, 0xE4C)`** — Complete `patch_entry_tail.S` libre bodies
   (1 559 bytes still differ); drop `inject_vendor` for hook clusters.

4. **Tail `[0xE4C, …)`** — Implement remaining T2/T3 hook functions in source (~24 kB of
   vendor code today in pe5 profile); retire `VENDOR_TAIL_FILL`.

5. **NF_REF** — Restrict to `test-nf` / `diff-prefix` / bisect targets; default `make all`
   must not read non-free tree.

6. **CI** — `make compliance-ci` in Docker (`.github/workflows/compliance.yml`);
   `--release --strict` fails on new `.incbin`, patch0 regression, or non-`full` profile.

---

## Running the audit

```bash
cd rtl8761bu-libre
docker build -t rtl8761bu-libre .
docker run --rm -v "$(pwd)":/work rtl8761bu-libre make compliance-ci

# Optional vendor diff (bisect / regression against NF_REF)
docker run --rm -v "$(pwd)":/work \
  -v /path/to/rtl8761bu-non-free:/nf_ref:ro \
  -e NF_REF=/nf_ref/rtl8761bu_fw.bin \
  rtl8761bu-libre make clean all compliance STRICT=1

# Hardware validation profile (not linux-libre)
docker run --rm -v "$(pwd)":/work \
  -v /path/to/rtl8761bu-non-free:/nf_ref:ro \
  -e NF_REF=/nf_ref/rtl8761bu_fw.bin \
  rtl8761bu-libre make clean full-inject-t3-pe5-vendor-tail \
  'PATCH=build/patch_injected.bin' compliance
```

CI / `make compliance-ci`: **LINUX-LIBRE: PASS** on current libre release tree.

---

## Related docs

| File | Topic |
|------|-------|
| `reverse_engineering_libre_patch_architecture.md` | P0–P4 profiles, non-goals |
| `reverse_engineering_mandatory_hooks.md` | What must be implemented vs stubbed |
| `reverse_engineering_patch_entry.md` | PE-0…PE-5 prefix RE status |
| `reverse_engineering_phase7_hci_bringup.md` | Hardware pass criteria |
