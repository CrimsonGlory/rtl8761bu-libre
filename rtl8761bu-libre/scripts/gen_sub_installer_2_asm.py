#!/usr/bin/env python3
"""Emit sub_installer_2_body.S from vendor [0x764, 0x820) — 188 B."""
from __future__ import annotations

import sys
from pathlib import Path

PATCH1_OFF = 0x3780
SI2_OFF = 0x764
SI2_SIZE = 0x820 - 0x764


def emit(blob: bytes) -> str:
    if len(blob) != SI2_SIZE:
        raise SystemExit(f"si2 size {len(blob)} != {SI2_SIZE}")
    lines = [
        "/* SPDX-License-Identifier: GPL-2.0-or-later",
        " * sub_installer_2 @ file 0x764 — vendor FUN_8011011c compact loop (188 B).",
        " * Regenerate: scripts/gen_sub_installer_2_asm.py",
        " */",
        "\t.globl\tsub_installer_2",
        "\t.type\tsub_installer_2, @function",
        "sub_installer_2:",
    ]
    for i in range(0, len(blob), 16):
        fo = SI2_OFF + i
        chunk = blob[i : i + 16]
        lines.append(f"\t/* 0x{fo:04x} */")
        lines.append("\t.byte\t" + ", ".join(f"0x{b:02x}" for b in chunk))
    lines.append("\t.size\tsub_installer_2, . - sub_installer_2")
    lines.append("")
    return "\n".join(lines)


def main() -> None:
    if len(sys.argv) != 3:
        print(f"Usage: {sys.argv[0]} <vendor_fw.bin> <out.S>", file=sys.stderr)
        sys.exit(1)
    nf = Path(sys.argv[1])
    out = Path(sys.argv[2])
    with nf.open("rb") as f:
        f.seek(PATCH1_OFF + SI2_OFF)
        blob = f.read(SI2_SIZE)
    out.write_text(emit(blob), encoding="utf-8")
    print(f"gen_sub_installer_2_asm: {SI2_SIZE} B → {out}")


if __name__ == "__main__":
    main()
