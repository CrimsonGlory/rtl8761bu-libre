#!/usr/bin/env python3
"""Emit full fn_b174 body @ PRAM [0x1174, 0x13D8) for t2_hooks.S.

Libre transcription from vendor patch1 @ linked offset (612 B).  Replaces the
truncated 68 B body + pad.py NOP hole [0x11B4, 0x13D8).

Regenerate:
  scripts/gen_fn_b174_asm.py [vendor_fw.bin] > /tmp/fn_b174.S
"""
from __future__ import annotations

import sys
from pathlib import Path

PATCH1_OFF = 0x3780
PATCH1_BASE = 0x8010A000
FN_OFF = 0x1174
FN_END = 0x13D8
FN_SIZE = FN_END - FN_OFF


def main() -> None:
    nf = Path(
        sys.argv[1]
        if len(sys.argv) > 1
        else "/root/rtl8761bu-libre/rtl8761bu-non-free/rtl8761bu_fw.bin"
    )
    body = nf.read_bytes()[PATCH1_OFF : PATCH1_OFF + 27808]
    blob = body[FN_OFF:FN_END]
    if len(blob) != FN_SIZE:
        raise SystemExit(f"unexpected size {len(blob)}")

    lines = [
        "/*",
        " * fn_b174 @ PRAM+0x1174 (runtime 0x8010B174): sec_base+0x14 hook.",
        " * Full 612 B body — transcribed from vendor @ link offset.",
        " * Regenerate: scripts/gen_fn_b174_asm.py",
        " */",
        "",
        "\t.section\t.text.fn_b174, \"ax\"",
        "\t.globl\t\tfn_b174",
        "\t.type\t\tfn_b174, @function",
        "fn_b174:",
        f"\t/* PRAM+0x{FN_OFF:04x} runtime 0x{PATCH1_BASE + FN_OFF:08x} size {FN_SIZE} */",
        "",
    ]
    for i in range(0, len(blob), 16):
        chunk = blob[i : i + 16]
        fo = FN_OFF + i
        hexbytes = ", ".join(f"0x{b:02x}" for b in chunk)
        lines.append(f"\t/* 0x{fo:04x} / 0x{PATCH1_BASE + fo:08x} */")
        lines.append(f"\t.byte\t{hexbytes}")
        lines.append("")
    lines.append("\t.size\tfn_b174, . - fn_b174")
    lines.append("")
    sys.stdout.write("\n".join(lines))


if __name__ == "__main__":
    main()
