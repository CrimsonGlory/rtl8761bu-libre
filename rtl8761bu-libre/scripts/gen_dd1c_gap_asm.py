#!/usr/bin/env python3
"""Emit src/fn_dd1c_gap.S from vendor patch1 bytes [0x3D80, 0x3FB0)."""
from __future__ import annotations

import sys
from pathlib import Path

GAP_OFF = 0x3D80
GAP_SIZE = 0x3FB0 - 0x3D80
GAP_RT = 0x8010A000 + GAP_OFF


def emit(blob: bytes) -> str:
    if len(blob) != GAP_SIZE:
        raise SystemExit(f"gap size {len(blob)} != {GAP_SIZE}")
    lines = [
        "/*",
        " * fn_dd1c_gap.S — vendor bytes @ PRAM [0x3D80, 0x3FB0) (560 B).",
        " *",
        " * Literal pools for fn_dd1c tail + fn_dfb0 prefix; includes ptr",
        " * 0x8010a469 → fn_a410 @ PRAM+0x468 (codec reg 0x11C).",
        " * Regenerate: scripts/gen_dd1c_gap_asm.py",
        " */",
        "",
        "\t.section\t.text.fn_dd1c_gap, \"ax\"",
        "\t.globl\t\tfn_dd1c_gap",
        "\t.type\t\tfn_dd1c_gap, @object",
        "fn_dd1c_gap:",
        "",
    ]
    for i in range(0, len(blob), 16):
        chunk = blob[i : i + 16]
        fo = GAP_OFF + i
        lines.append(f"\t/* 0x{fo:04x} / 0x{GAP_RT + i:08x} */")
        hexbytes = ", ".join(f"0x{b:02x}" for b in chunk)
        lines.append(f"\t.byte\t{hexbytes}")
        lines.append("")
    lines.append("\t.size\tfn_dd1c_gap, . - fn_dd1c_gap")
    lines.append("")
    return "\n".join(lines)


def main() -> None:
    if len(sys.argv) != 3:
        print(f"Usage: {sys.argv[0]} <vendor_fw.bin> <out.S>", file=sys.stderr)
        sys.exit(1)
    nf = Path(sys.argv[1])
    out = Path(sys.argv[2])
    with nf.open("rb") as f:
        f.seek(0x3780 + GAP_OFF)
        blob = f.read(GAP_SIZE)
    out.write_text(emit(blob), encoding="utf-8")
    print(f"gen_dd1c_gap_asm: {GAP_SIZE} B → {out}")


if __name__ == "__main__":
    main()
