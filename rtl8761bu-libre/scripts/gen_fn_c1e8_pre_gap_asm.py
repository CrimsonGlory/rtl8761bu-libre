#!/usr/bin/env python3
"""Emit src/fn_c1e8_pre_gap.S from vendor patch1 bytes [0x21D6, 0x21E8).

HCI tail bisect (2026-06-10): libre NOP hole between fn_c198 @ 0x2198+0x3E and
fn_c1e8 @ 0x21E8 caused Opcode 0x2036 on p4 single-patch.

Regenerate:
  scripts/gen_fn_c1e8_pre_gap_asm.py [vendor_fw.bin] > src/fn_c1e8_pre_gap.S
"""
from __future__ import annotations

import sys
from pathlib import Path

PATCH1_OFF = 0x3780
PATCH1_BASE = 0x8010A000
GAP_OFF = 0x21D6
GAP_END = 0x21E8
GAP_SIZE = GAP_END - GAP_OFF


def emit(blob: bytes) -> str:
    if len(blob) != GAP_SIZE:
        raise SystemExit(f"gap size {len(blob)} != {GAP_SIZE}")
    lines = [
        "/*",
        f" * fn_c1e8_pre_gap.S — vendor bytes @ PRAM [0x21D6, 0x21E8) ({GAP_SIZE} B).",
        " *",
        " * Literal pool before fn_c1e8; pad.py left NOP zeros here (HCI 0x2036).",
        " * Regenerate: scripts/gen_fn_c1e8_pre_gap_asm.py",
        " */",
        "",
        '\t.section\t.text.fn_c1e8_pre_gap, "ax"',
        "\t.globl\t\tfn_c1e8_pre_gap",
        "\t.type\t\tfn_c1e8_pre_gap, @object",
        "fn_c1e8_pre_gap:",
        "",
    ]
    for i in range(0, len(blob), 16):
        chunk = blob[i : i + 16]
        fo = GAP_OFF + i
        lines.append(f"\t/* 0x{fo:04x} / 0x{PATCH1_BASE + fo:08x} */")
        hexbytes = ", ".join(f"0x{b:02x}" for b in chunk)
        lines.append(f"\t.byte\t{hexbytes}")
        lines.append("")
    lines.append("\t.size\tfn_c1e8_pre_gap, . - fn_c1e8_pre_gap")
    lines.append("")
    return "\n".join(lines)


def main() -> None:
    nf = Path(
        sys.argv[1]
        if len(sys.argv) > 1
        else "/root/rtl8761bu-libre/rtl8761bu-non-free/rtl8761bu_fw.bin"
    )
    body = nf.read_bytes()[PATCH1_OFF : PATCH1_OFF + 27808]
    print(emit(body[GAP_OFF:GAP_END]), end="")


if __name__ == "__main__":
    main()
