#!/usr/bin/env python3
"""Diff libre patch.bin against vendor patch1 prefix [0, LIMIT).

Default LIMIT=0x764 (UB500 connect-critical prefix). Reports per-region stats
documented in analysis/reverse_engineering_patch_entry.md.
"""
from __future__ import annotations

import argparse
import hashlib
import struct
import sys
from pathlib import Path

# file offsets in patch1 body @ 0x8010A000
REGIONS = [
    ("FUN_8010a000 code", 0x0000, 0x0242),
    ("literal pool", 0x0242, 0x05D8),
    ("helper fns (a5d8..)", 0x05D8, 0x0724),
    ("sub_installer_1", 0x0724, 0x0754),
    ("fn_bss_init", 0x0754, 0x0764),
    ("sub_installer_2", 0x0764, 0x0820),
    ("installer tail", 0x0820, 0x0E4C),
]


def load_patch1(fw: bytes, patch_off: int = 0x3780) -> bytes:
    if len(fw) < patch_off + 4:
        raise SystemExit(f"firmware too small for patch1 @ 0x{patch_off:x}")
    return fw[patch_off:]


def diff_runs(a: bytes, b: bytes) -> list[tuple[int, int]]:
    n = min(len(a), len(b))
    idx = [i for i in range(n) if a[i] != b[i]]
    if not idx:
        return []
    runs: list[tuple[int, int]] = []
    s = e = idx[0]
    for i in idx[1:]:
        if i == e + 1:
            e = i
        else:
            runs.append((s, e + 1))
            s = e = i
    runs.append((s, e + 1))
    return runs


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("libre_patch", type=Path, help="build/patch.bin from make all")
    ap.add_argument(
        "--vendor-fw",
        type=Path,
        default=Path("../rtl8761bu-non-free/rtl8761bu_fw.bin"),
        help="non-free rtl8761bu_fw.bin",
    )
    ap.add_argument("--limit", type=lambda x: int(x, 0), default=0x764)
    ap.add_argument("--runs", type=int, default=12, help="max diff runs to print")
    args = ap.parse_args()

    vendor = load_patch1(args.vendor_fw.read_bytes())[: args.limit]
    libre = args.libre_patch.read_bytes()[: args.limit]
    if len(libre) < args.limit:
        print(f"warning: libre patch shorter than limit ({len(libre)} < {args.limit})", file=sys.stderr)

    total = min(len(vendor), len(libre), args.limit)
    runs = diff_runs(vendor, libre)
    ndiff = sum(e - s for s, e in runs)
    print(f"prefix [0, 0x{total:x})  diffs={ndiff}/{total}  match={ndiff == 0}")
    print(f"  vendor sha256: {hashlib.sha256(vendor[:total]).hexdigest()}")
    print(f"  libre  sha256: {hashlib.sha256(libre[:total]).hexdigest()}")
    print()
    print("regions:")
    for name, lo, hi in REGIONS:
        if lo >= total:
            break
        hi = min(hi, total)
        sub = [i for i in range(lo, hi) if vendor[i] != libre[i]]
        print(f"  [{lo:04x}, {hi:04x}) {name}: {len(sub)}/{hi - lo} diffs")
    print()
    print("diff runs:")
    for s, e in runs[: args.runs]:
        sample = min(8, e - s)
        print(
            f"  [{s:04x}, {e:04x}) len={e-s:#x}  "
            f"v={vendor[s:s+sample].hex()}  l={libre[s:s+sample].hex()}"
        )
    if len(runs) > args.runs:
        print(f"  ... {len(runs) - args.runs} more runs")


if __name__ == "__main__":
    main()
