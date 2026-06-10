#!/usr/bin/env python3
"""Audit rtl8761bu-libre builds against GNU Linux-libre blob policy.

Linux-libre accepts only firmware built from Free source; non-free bytes
embedded at build time (incbin, pack.py patch0, inject_vendor overlays)
block mainline inclusion until removed.

Usage:
  compliance_check.py [--profile NAME] [--nf-ref PATH] [--fw PATH] [--patch PATH]

Defaults read build/.profile, rtl8761bu_fw.bin, and build/patch_injected.bin or
build/patch_padded.bin. NF_REF is optional for libre release audits (--no-nf-ref).
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
NON_LIBRE_PROFILE_PREFIXES = (
    "hybrid-",
    "libre-hybrid-",
    "full-inject-t3-vendor-tail-split-",
    "full-vendor-early-",
    "phase-",
)

# Default `make all` / `make docker` rules — must not list $(NF_REF).
RELEASE_MAKEFILE_RULES = (
    "all:",
    "docker:",
    "rtl8761bu_fw.bin:",
    "build/patch.elf:",
    "build/patch.bin:",
    "build/patch_padded.bin:",
    "minimal:",
    "minimal-null:",
)

# Bisect-only .incbin paths — any other .incbin in src/ fails --release audit.
ALLOWED_INCBIN: dict[str, frozenset[str]] = {
    "src/installer_vendor_early.S": frozenset({"build/vendor_prefix.bin"}),
}

LIBRE_RELEASE_PROFILE = "p2-libre"
# Legacy alias from before Phase 8.3 profile naming.
LIBRE_RELEASE_PROFILE_ALIASES = frozenset({"full", "p2-libre"})


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


def _region_diffs(vendor: bytes, libre: bytes) -> list[tuple[str, int, int, int]]:
    out = []
    for name, lo, hi in REGIONS:
        hi = min(hi, len(vendor), len(libre))
        if lo >= hi:
            continue
        nd = sum(1 for i in range(lo, hi) if vendor[i] != libre[i])
        out.append((name, lo, hi, nd))
    return out


def _audit_makefile_nf_ref(root: Path) -> list[str]:
    """Verify release targets do not depend on NF_REF / non-free tree."""
    mk_path = root / "Makefile"
    if not mk_path.is_file():
        return ["Makefile missing"]
    failures: list[str] = []
    for line in mk_path.read_text(encoding="utf-8").splitlines():
        stripped = line.lstrip()
        if stripped.startswith("#") or not stripped:
            continue
        if not any(stripped.startswith(rule) for rule in RELEASE_MAKEFILE_RULES):
            continue
        if "NF_REF" in line or "rtl8761bu-non-free" in line:
            failures.append(f"release rule must not use NF_REF: {line.strip()}")
    return failures


def _pe1_has_vendor_incbin(root: Path) -> bool:
    pe1 = root / "src" / "patch_entry_code.S"
    if not pe1.is_file():
        return False
    text = pe1.read_text(encoding="utf-8", errors="replace")
    return ".incbin" in text and "vendor_entry_code.bin" in text


def _parse_incbin_targets(text: str) -> list[str]:
    """Return quoted paths from .incbin directives in assembly source."""
    return re.findall(r'\.incbin\s+"([^"]+)"', text)


def _audit_incbin(root: Path) -> list[str]:
    """Fail on new vendor .incbin outside the bisect-only allowlist."""
    failures: list[str] = []
    for path in sorted((root / "src").glob("*.S")):
        rel = path.relative_to(root).as_posix()
        targets = _parse_incbin_targets(path.read_text(encoding="utf-8", errors="replace"))
        if not targets:
            continue
        allowed = ALLOWED_INCBIN.get(rel, frozenset())
        for target in targets:
            if target not in allowed:
                failures.append(f"unexpected .incbin in {rel}: {target!r}")
    return failures


def _audit_release_profile(profile: str) -> list[str]:
    if profile not in LIBRE_RELEASE_PROFILE_ALIASES:
        return [
            f"default release profile is {profile!r}, expected {LIBRE_RELEASE_PROFILE!r}"
        ]
    if _is_non_libre_profile(profile):
        return [f"profile {profile!r} is bisect/inject — not linux-libre release default"]
    return []


def _audit_makefile_pack_release(root: Path) -> list[str]:
    """Default rtl8761bu_fw.bin rule must use single-patch pack (no --dual / vendor patch0)."""
    mk_path = root / "Makefile"
    if not mk_path.is_file():
        return ["Makefile missing"]
    failures: list[str] = []
    in_rule = False
    for line in mk_path.read_text(encoding="utf-8").splitlines():
        stripped = line.lstrip()
        if stripped.startswith("rtl8761bu_fw.bin:"):
            in_rule = True
            if "--dual" in line or "--vendor-patch0" in line:
                failures.append(f"release pack rule must be single-patch: {line.strip()}")
            continue
        if in_rule:
            if stripped and not stripped.startswith("$") and not stripped.startswith("\t"):
                if not stripped.startswith("#"):
                    break
            if "pack.py" in line:
                if "--dual" in line or "--vendor-patch0" in line:
                    failures.append(f"release pack rule must be single-patch: {line.strip()}")
                break
    return failures


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
    ap.add_argument("--nf-ref", type=Path, help="vendor reference firmware (bisect/diff only)")
    ap.add_argument(
        "--no-nf-ref",
        action="store_true",
        help="clean-room audit: skip vendor tree even if present",
    )
    ap.add_argument("--fw", type=Path, help="built rtl8761bu_fw.bin")
    ap.add_argument("--patch", type=Path, help="patch1 body (padded or injected)")
    ap.add_argument("--strict", action="store_true", help="exit 1 if linux-libre FAIL")
    ap.add_argument(
        "--release",
        action="store_true",
        help="CI gate: enforce full profile, no new .incbin, single-patch ship layout",
    )
    args = ap.parse_args()

    profile = args.profile or _read_profile(ROOT / "build" / ".profile")
    default_nf = ROOT.parent / "rtl8761bu-non-free" / "rtl8761bu_fw.bin"
    if args.no_nf_ref:
        nf_ref: Path | None = None
    elif args.nf_ref is not None:
        nf_ref = args.nf_ref
    elif default_nf.is_file():
        nf_ref = default_nf
    else:
        nf_ref = None
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
    print(f"nf_ref      : {nf_ref or '(not used — clean-room release audit)'}")
    print(f"fw          : {fw_path}")
    print(f"patch1 body : {patch_path}")
    print()

    failures: list[str] = []

    # --- Makefile: release must not depend on NF_REF ---
    mk_failures = _audit_makefile_nf_ref(ROOT)
    if mk_failures:
        failures.extend(mk_failures)
        print("Makefile NF_REF gate: FAIL")
        for f in mk_failures:
            print(f"  - {f}")
    else:
        print("Makefile NF_REF gate: OK — default make all/docker does not use NF_REF")
    print()

    if args.release:
        incbin_failures = _audit_incbin(ROOT)
        if incbin_failures:
            failures.extend(incbin_failures)
            print("incbin gate: FAIL")
            for f in incbin_failures:
                print(f"  - {f}")
        else:
            allowed_n = sum(len(v) for v in ALLOWED_INCBIN.values())
            print(
                f"incbin gate: OK — no unexpected .incbin "
                f"({allowed_n} bisect-only path(s) allowlisted)"
            )
        print()

        pack_failures = _audit_makefile_pack_release(ROOT)
        if pack_failures:
            failures.extend(pack_failures)
            print("pack.py release gate: FAIL")
            for f in pack_failures:
                print(f"  - {f}")
        else:
            print("pack.py release gate: OK — make all uses single-patch (no --vendor-patch0)")
        print()

        profile_failures = _audit_release_profile(profile)
        if profile_failures:
            failures.extend(profile_failures)
            print("release profile gate: FAIL")
            for f in profile_failures:
                print(f"  - {f}")
        else:
            print(f"release profile gate: OK — profile={LIBRE_RELEASE_PROFILE}")
        print()

    # --- NF_REF (optional for libre release; required for bisect/inject profiles) ---
    nf_present = nf_ref is not None and nf_ref.is_file()
    if nf_present:
        print(f"NF_REF: present ({nf_ref.stat().st_size} B) — vendor diff enabled")
    elif _is_non_libre_profile(profile):
        failures.append("NF_REF missing — required for inject/bisect profile audit")
        print(f"NF_REF: MISSING (expected at {default_nf})")
    else:
        print("NF_REF: not used — release build does not read rtl8761bu-non-free/")
    print()

    if not fw_path.is_file() or not patch_path.is_file():
        failures.append("built firmware or patch1 body missing — run make first")
        print("ERROR: build artifacts missing; run make inside Docker.")
        print()
        _print_verdict(profile, failures, args.strict)
        return 2

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
    elif nf_present:
        vendor_fw = nf_ref.read_bytes()
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
    else:
        patch0_out = out_fw[PATCH0_OFF : PATCH0_OFF + min(PATCH0_LEN, len(out_fw) - PATCH0_OFF)]
        nop_stub = MIPS16E_NOP * (PATCH0_LEN // 2)
        if patch0_out == nop_stub[: len(patch0_out)]:
            print(f"patch0 @0x30: libre MIPS16e NOP stub (NF_REF not consulted)")
        else:
            failures.append("dual-patch envelope without NF_REF — cannot verify patch0")
            print("patch0 @0x30: dual layout — NF_REF required to classify patch0")
    print()

    pe1_has_incbin = _pe1_has_vendor_incbin(ROOT)
    if pe1_has_incbin:
        failures.append("src/patch_entry_code.S incbins vendor_entry_code.bin (578 B)")

    # --- patch1 vs vendor (optional when NF_REF available) ---
    if nf_present:
        vendor_fw = nf_ref.read_bytes()
        vendor_p1 = vendor_fw[PATCH1_OFF : PATCH1_OFF + PATCH1_LEN]
        total_diff = sum(
            1 for i in range(min(len(vendor_p1), len(libre_p1))) if vendor_p1[i] != libre_p1[i]
        )
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
        elif (
            vendor_match >= PREFIX_CONNECT
            and profile in LIBRE_RELEASE_PROFILE_ALIASES
            and pe1_has_incbin
        ):
            failures.append("default full build embeds vendor PE-1 incbin (578 B)")
    else:
        print(f"patch1 @0x3780: sha256 {hashlib.sha256(libre_p1).hexdigest()} (no vendor diff)")
        print()

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
        print("  4. Keep NF_REF off default make deps (test-nf / inject / hybrid / diff-prefix only)")
    if strict and failures:
        sys.exit(1)


if __name__ == "__main__":
    sys.exit(main())
