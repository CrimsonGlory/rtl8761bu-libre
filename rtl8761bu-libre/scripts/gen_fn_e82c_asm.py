#!/usr/bin/env python3
"""Verify fn_e82c bytes @ PRAM+0x482c (runtime 0x8010E82C).

FUN_8010e82c (8 B) sits inside fn_e350_post_gap (+0x46). sub-inst #5 installs
0x8010e82d → RAM 0x80120c88. Libre: .set fn_e82c, fn_e350_post_gap + 0x46.

  scripts/gen_fn_e82c_asm.py [vendor_fw.bin]
"""
from __future__ import annotations

import sys
from pathlib import Path

PATCH1_OFF = 0x3780
PATCH1_BASE = 0x8010A000
FN_OFF = 0x482c
FN_SIZE = 8
POST_GAP_OFF = 0x47E6
POST_GAP_REL = FN_OFF - POST_GAP_OFF


def main() -> None:
    nf = Path(
        sys.argv[1]
        if len(sys.argv) > 1
        else "/root/rtl8761bu-libre/rtl8761bu-non-free/rtl8761bu_fw.bin"
    )
    body = nf.read_bytes()[PATCH1_OFF : PATCH1_OFF + 27808]
    blob = body[FN_OFF : FN_OFF + FN_SIZE]
    if len(blob) != FN_SIZE:
        raise SystemExit(f"unexpected slice size {len(blob)}")
    in_gap = body[POST_GAP_OFF + POST_GAP_REL : POST_GAP_OFF + POST_GAP_REL + FN_SIZE]
    if blob != in_gap:
        raise SystemExit("fn_e82c not aligned at fn_e350_post_gap + 0x46")
    hexbytes = ", ".join(f"0x{b:02x}" for b in blob)
    print(f"fn_e82c: {FN_SIZE} B @ PRAM+0x{FN_OFF:x} runtime 0x{PATCH1_BASE + FN_OFF:08x}")
    print(f"  bytes: {hexbytes}")
    print(f"  alias: fn_e350_post_gap + 0x{POST_GAP_REL:x}")


if __name__ == "__main__":
    main()
