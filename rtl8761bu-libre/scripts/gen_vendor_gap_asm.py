#!/usr/bin/env python3
"""Emit a vendor-byte gap section for rtl8761bu.ld anchored offsets.

Usage:
  gen_vendor_gap_asm.py <name> <start_hex> <end_hex> [vendor_fw.bin]

Regenerate bundled connect gaps:
  scripts/gen_fn_post_10ddc_connect_gaps_asm.py
"""
from __future__ import annotations

import sys
from pathlib import Path

PATCH1_OFF = 0x3780
PATCH1_BASE = 0x8010A000


def emit_section(name: str, start: int, blob: bytes) -> str:
    lines = [
        f"\t.section\t.text.{name}, \"ax\"",
        f"\t.globl\t\t{name}",
        f"\t.type\t\t{name}, @object",
        f"{name}:",
        f"\t/* PRAM+0x{start:04x} runtime 0x{PATCH1_BASE + start:08x} size {len(blob)} */",
        "",
    ]
    for i in range(0, len(blob), 16):
        chunk = blob[i : i + 16]
        fo = start + i
        hexbytes = ", ".join(f"0x{b:02x}" for b in chunk)
        lines.append(f"\t/* 0x{fo:04x} / 0x{PATCH1_BASE + fo:08x} */")
        lines.append(f"\t.byte\t{hexbytes}")
        lines.append("")
    lines.append(f"\t.size\t{name}, . - {name}")
    lines.append("")
    return "\n".join(lines)


def vendor_body(nf: Path) -> bytes:
    return nf.read_bytes()[PATCH1_OFF : PATCH1_OFF + 27808]


def main() -> None:
    if len(sys.argv) < 4:
        raise SystemExit(__doc__)
    name = sys.argv[1]
    start = int(sys.argv[2], 0)
    end = int(sys.argv[3], 0)
    nf = Path(
        sys.argv[4]
        if len(sys.argv) > 4
        else "/root/rtl8761bu-libre/rtl8761bu-non-free/rtl8761bu_fw.bin"
    )
    blob = vendor_body(nf)[start:end]
    if len(blob) != end - start:
        raise SystemExit("bad slice")
    print(emit_section(name, start, blob), end="")


if __name__ == "__main__":
    main()
