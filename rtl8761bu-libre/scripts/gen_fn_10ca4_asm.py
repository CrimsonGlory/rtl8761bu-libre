#!/usr/bin/env python3
"""Emit fn_10ca4 section for t4_hooks.S from vendor patch1 bytes.

FUN_80110ca4 (178 B) is transcribed @ PRAM+0x6350 (runtime 0x80110350).
GZF labels the same body @ 0x80110CA4 (4 B past FC20 window). sub-inst #3
INSTALL_HOOK_ABS → 0x80110350. sub2_fn_18 is an inner entry @ +0x14.

Append to t4_hooks.S (after fn_10ddc_gap). Regenerate:
  scripts/gen_fn_10ca4_asm.py [vendor_fw.bin] >> src/t4_hooks.S
"""
from __future__ import annotations

import sys
from pathlib import Path

PATCH1_OFF = 0x3780
PATCH1_BASE = 0x8010A000
FN_OFF = 0x6350
FN_SIZE = 178
SUB2_18_OFF = 0x14


def emit(blob: bytes) -> str:
    lines = [
        "",
        "\t.section\t.text.fn_10ca4, \"ax\"",
        "\t.globl\t\tfn_10ca4",
        "\t.type\t\tfn_10ca4, @function",
        "fn_10ca4:",
        f"\t/* PRAM+0x{FN_OFF:04x} runtime 0x{PATCH1_BASE + FN_OFF:08x} size {len(blob)} */",
        f"\t/* GZF FUN_80110ca4 @ 0x{PATCH1_BASE + 0x6CA4:08x}; sub2_fn_18 entry @ +0x{SUB2_18_OFF:02x} */",
        "",
    ]
    for i in range(0, len(blob), 16):
        chunk = blob[i : i + 16]
        fo = FN_OFF + i
        hexbytes = ", ".join(f"0x{b:02x}" for b in chunk)
        lines.append(f"\t/* 0x{fo:04x} / 0x{PATCH1_BASE + fo:08x} */")
        lines.append(f"\t.byte\t{hexbytes}")
        lines.append("")
    lines.append("\t.size\tfn_10ca4, . - fn_10ca4")
    lines.append("")
    lines.append("\t.globl\t\tsub2_fn_18")
    lines.append(f"\t.set\tsub2_fn_18, fn_10ca4 + 0x{SUB2_18_OFF:02x}")
    lines.append("")
    return "\n".join(lines)


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
    sub2 = body[FN_OFF + SUB2_18_OFF : FN_OFF + SUB2_18_OFF + 28]
    if sub2 != blob[SUB2_18_OFF : SUB2_18_OFF + 28]:
        raise SystemExit("sub2_fn_18 offset mismatch")
    sys.stdout.write(emit(blob))
    print(f"gen_fn_10ca4_asm: {FN_SIZE} B @ 0x{FN_OFF:x}", file=sys.stderr)


if __name__ == "__main__":
    main()
