#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-2.0-or-later
"""Audit rtl8761bu-libre builds against GNU Linux-libre blob policy.

Linux-libre accepts only firmware built from Free source; non-free bytes
embedded at build time (incbin, pack.py patch0, inject_vendor overlays)
block mainline inclusion until removed.

Usage:
  compliance_check.py [--profile NAME] [--nf-ref PATH] [--fw PATH] [--patch PATH]

Defaults read build/.profile, rtl8761bu_fw.bin, build/patch_injected.bin or
build/patch_padded.bin, and ../rtl8761bu-non-free/rtl8761bu_fw.bin.
Exit 0 = audit completed (may still print FAIL verdicts); exit 2 on error.
"""

from __future__ import annotations

import argparse
import hashlib
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PATCH1_OFF = 0x3780
PATCH1_LEN = 0x6CA0
PATCH0_LEN = 0x36E0
PATCH0_OFF = 0x30
SINGLE_FW_SIZE = 0x30 + PATCH1_LEN + 72   # 27,928 B libre ship layout
DUAL_FW_SIZE = 42088
MIPS16E_NOP = b"\x00\x65"
PREFIX_CONNECT = 0x764
VENDOR_INSTALLER_LEN = 0xE4C
TAIL_END = PATCH1_LEN - 4

REGIONS = [
    ("PE-1 entry code", 0x0000, 0x0242),
    ("PE-2 literal pool", 0x0242, 0x05D8),
    ("PE-3 helpers", 0x05D8, 0x0724),
    ("sub_inst_1 + bss", 0x0724, 0x0764),
    ("sub_inst_2", 0x0764, 0x0820),
    ("patch_entry_tail", 0x0820, VENDOR_INSTALLER_LEN),
    ("tail / NOP pad", VENDOR_INSTALLER_LEN, PATCH1_LEN),
]

SPDX_RE = re.compile(r"SPDX-License-Identifier:\s*GPL-2\.0-or-later", re.I)

# Profiles that ship non-free bytes by design (bisect / hardware only).
NON_LIBRE_PROFILES = frozenset(
    p
    for p in (
        "test-nf",
        "full-vendor-patch1",
        "full-inject-t1",
        "full-inject-t2",
        "full-inject-t3",
        "full-inject-t3-vendor-inst",
        "full-inject-t3-vendor-tail",
        "full-inject-t3-pe5-vendor-tail",
    )
    if p
)
NON_LIBRE_PROFILE_PREFIXES = ("hybrid-", "full-inject-t3-vendor-tail-split-", "full-vendor-early-", "phase-")


def _read_profile(path: Path) -> str:
    if not path.is_file():
        return "unknown"
    for line in path.read_text().splitlines():
        if line.startswith("profile="):
            return line.split("=", 1)[1].strip()
    return "unknown"


def _is_non_libre_profile(name: str) -> bool:
    if name in NON_LIBRE_PROFILES:
        return True
    return any(name.startswith(p) for p in NON_LIBRE_PROFILE_PREFIXES)


def _spdx_audit(root: Path) -> tuple[list[Path], list[Path]]:
    patterns = ("src/*.S", "scripts/*.py", "*.py")
    checked: list[Path] = []
    missing: list[Path] = []
    for pat in patterns:
        for path in sorted(root.glob(pat)):
            if path.name == "compliance_check.py":
                continue
            checked.append(path)
            text = path.read_text(encoding="utf-8", errors="replace")
            if not SPDX_RE.search(text):
                missing.append(path)
    return checked, missing


def _region_diffs(vendor: bytes, libre: bytes) -> list[tuple[str, int, int, int]]:
    out = []
    for name, lo, hi in REGIONS:
        hi = min(hi, len(vendor), len(libre))
        if lo >= hi:
            continue
        nd = sum(1 for i in range(lo, hi) if vendor[i] != libre[i])
        out.append((name, lo, hi, nd))
    return out


def _git_clean(root: Path) -> bool | None:
    try:
        r = subprocess.run(
            ["git", "-C", str(root.parent), "status", "--porcelain"],
            capture_output=True,
            text=True,
            check=True,
        )
        return r.stdout.strip() == ""
    except (OSError, subprocess.CalledProcessError):
        return None


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--profile", help="override build/.profile name")
    ap.add_argument("--nf-ref", type=Path, help="vendor reference firmware")
    ap.add_argument("--fw", type=Path, help="built rtl8761bu_fw.bin")
    ap.add_argument("--patch", type=Path, help="patch1 body (padded or injected)")
    ap.add_argument("--strict", action="store_true", help="exit 1 if linux-libre FAIL")
    args = ap.parse_args()

    profile = args.profile or _read_profile(ROOT / "build" / ".profile")
    nf_ref = args.nf_ref or (ROOT.parent / "rtl8761bu-non-free" / "rtl8761bu_fw.bin")
    fw_path = args.fw or (ROOT / "rtl8761bu_fw.bin")
    patch_path = args.patch
    if patch_path is None:
        for candidate in (ROOT / "build" / "patch_injected.bin", ROOT / "build" / "patch_padded.bin"):
            if candidate.is_file():
                patch_path = candidate
                break
        else:
            patch_path = ROOT / "build" / "patch_padded.bin"

    print("=== linux-libre compliance audit ===")
    print(f"profile     : {profile}")
    print(f"nf_ref      : {nf_ref}")
    print(f"fw          : {fw_path}")
    print(f"patch1 body : {patch_path}")
    print()

    failures: list[str] = []

    # --- SPDX ---
    checked, missing = _spdx_audit(ROOT)
    print(f"SPDX GPL-2.0-or-later: {len(checked) - len(missing)}/{len(checked)} files")
    if missing:
        failures.append(f"SPDX missing in {len(missing)} file(s)")
        for p in missing:
            print(f"  MISSING: {p.relative_to(ROOT)}")
    else:
        print("  OK — all tracked sources licensed")
    print()

    # --- NF_REF availability ---
    if not nf_ref.is_file():
        failures.append(f"NF_REF missing: {nf_ref}")
        print(f"NF_REF: MISSING ({nf_ref})")
        print("  make all requires vendor rtl8761bu_fw.bin for PE-1 incbin")
    else:
        print(f"NF_REF: present ({nf_ref.stat().st_size} B)")
    print()

    if not fw_path.is_file() or not patch_path.is_file():
        failures.append("built firmware or patch1 body missing — run make first")
        print("ERROR: build artifacts missing; run make inside Docker.")
        print()
        _print_verdict(profile, failures, args.strict)
        return 2

    if not nf_ref.is_file():
        _print_verdict(profile, failures, args.strict)
        return 2

    vendor_fw = nf_ref.read_bytes()
    vendor_p1 = vendor_fw[PATCH1_OFF : PATCH1_OFF + PATCH1_LEN]
    libre_p1 = patch_path.read_bytes()
    out_fw = fw_path.read_bytes()

    if len(libre_p1) != PATCH1_LEN:
        failures.append(f"patch1 size {len(libre_p1)} != {PATCH1_LEN}")
    if len(out_fw) not in (SINGLE_FW_SIZE, DUAL_FW_SIZE):
        failures.append(
            f"fw size {len(out_fw)} not {SINGLE_FW_SIZE} (single) or {DUAL_FW_SIZE} (dual)"
        )

    # --- envelope / patch0 ---
    if len(out_fw) == SINGLE_FW_SIZE:
        print(f"envelope: single-patch ({SINGLE_FW_SIZE} B) — no patch0 slot")
    else:
        patch0_vendor = vendor_fw[PATCH0_OFF : PATCH0_OFF + PATCH0_LEN]
        patch0_out = out_fw[PATCH0_OFF : PATCH0_OFF + PATCH0_LEN]
        patch0_match = patch0_out == patch0_vendor
        nop_stub = MIPS16E_NOP * (PATCH0_LEN // 2)
        patch0_is_stub = patch0_out == nop_stub
        if patch0_match:
            label = "copied from NF_REF"
            failures.append(f"patch0 is verbatim vendor ({PATCH0_LEN} B non-free in shipped fw)")
        elif patch0_is_stub:
            label = "libre MIPS16e NOP stub"
        else:
            label = "differs from NF_REF (non-vendor)"
        print(f"patch0 @0x30 ({PATCH0_LEN} B): {label}")
    print()

    # --- patch1 vs vendor ---
    total_diff = sum(1 for i in range(min(len(vendor_p1), len(libre_p1))) if vendor_p1[i] != libre_p1[i])
    vendor_match = len(vendor_p1) - total_diff
    pct = 100.0 * vendor_match / PATCH1_LEN
    print(f"patch1 @0x3780: {vendor_match}/{PATCH1_LEN} bytes match vendor ({pct:.1f}%)")
    print(f"  sha256 libre : {hashlib.sha256(libre_p1).hexdigest()}")
    print(f"  sha256 vendor: {hashlib.sha256(vendor_p1).hexdigest()}")
    print("  regions (diffs / size):")
    for name, lo, hi, nd in _region_diffs(vendor_p1, libre_p1):
        print(f"    [{lo:#06x},{hi:#06x}) {name}: {nd}/{hi - lo}")
    print()

    if total_diff == 0:
        failures.append("patch1 is byte-identical to vendor (non-free)")
    elif vendor_match >= PREFIX_CONNECT and profile == "full":
        # [0,0x764) match + incbin PE-1 still counts as vendor-derived
        failures.append("default full build embeds vendor PE-1 incbin (578 B) and requires NF_REF")

    pe1_incbin = ROOT / "src" / "patch_entry_code.S"
    if pe1_incbin.is_file() and ".incbin" in pe1_incbin.read_text():
        if "vendor_entry_code.bin" in pe1_incbin.read_text():
            failures.append("src/patch_entry_code.S incbins vendor_entry_code.bin (578 B)")

    if _is_non_libre_profile(profile):
        print(f"profile '{profile}' is a bisect/inject target — not linux-libre candidate")
        print()

    git_ok = _git_clean(ROOT)
    if git_ok is True:
        print("git tree: clean (reproducible audit)")
    elif git_ok is False:
        print("git tree: dirty — reproducibility not guaranteed")
    else:
        print("git tree: unknown")
    print()

    _print_verdict(profile, failures, args.strict)
    if args.strict and failures:
        return 1
    return 0


def _print_verdict(profile: str, failures: list[str], strict: bool) -> None:
    print("=== verdict ===")
    if _is_non_libre_profile(profile):
        print("SHIP:     NO — inject/bisect profile (hardware validation only)")
    elif not failures:
        print("LINUX-LIBRE: PASS (no blockers detected)")
    else:
        print("LINUX-LIBRE: FAIL")
        for f in failures:
            print(f"  - {f}")
        print()
        print("Remediation (summary):")
        print("  1. patch0: use single-patch ship or libre NOP stub (--dual without --vendor-patch0)")
        print("  2. Transcribe PE-1 [0,0x242) — remove patch_entry_code.S incbin")
        print("  3. Complete libre [0x820,0xE4C) tail + [0xE4C,…) hook bodies (drop inject_vendor)")
        print("  4. Add SPDX to remaining sources; drop NF_REF from default make deps")
    if strict and failures:
        sys.exit(1)


if __name__ == "__main__":
    sys.exit(main())
