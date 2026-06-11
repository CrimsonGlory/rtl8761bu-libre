#!/usr/bin/env python3
"""Emit fn_c09c .byte block for t2_hooks.S from vendor patch1 [0x209C, 0x20F4).

Libre fn_c09c was mis-transcribed (missing prologue/pool prefix). Vendor span to
next linker anchor fn_c0f4 @ 0x20F4 is 88 B.

Usage:
  scripts/gen_fn_c09c_asm.py [vendor_fw.bin]   # prints .byte block only
"""
from __future__ import annotations

import sys
from pathlib import Path

PATCH1_OFF = 0x3780
PATCH1_BASE = 0x8010A000
FN_OFF = 0x209C
FN_END = 0x20F4


def emit(blob: bytes) -> str:
    lines = [
        f"\t/* PRAM+0x{FN_OFF:04x} runtime 0x{PATCH1_BASE + FN_OFF:08x} size {len(blob)} (vendor-fixed) */",
    ]
    for i in range(0, len(blob), 16):
        chunk = blob[i : i + 16]
        fo = FN_OFF + i
        hexbytes = ", ".join(f"0x{b:02x}" for b in chunk)
        lines.append(f"\t.byte\t{hexbytes}")
    return "\n".join(lines)


def main() -> None:
    nf = Path(
        sys.argv[1]
        if len(sys.argv) > 1
        else "/root/rtl8761bu-libre/rtl8761bu-non-free/rtl8761bu_fw.bin"
    )
    body = nf.read_bytes()[PATCH1_OFF : PATCH1_OFF + 27808]
    print(emit(body[FN_OFF:FN_END]))


if __name__ == "__main__":
    main()
