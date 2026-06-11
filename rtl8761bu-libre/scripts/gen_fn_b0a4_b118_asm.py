#!/usr/bin/env python3
"""Emit fn_b0a4 + fn_b118 bodies from vendor @ linked offsets for t1_hooks.S.

fn_b0a4  [0x10A4, 0x1118) 116 B
fn_b118  [0x1118, 0x1174)  92 B

Regenerate:
  scripts/gen_fn_b0a4_b118_asm.py [vendor_fw.bin]
"""
from __future__ import annotations

import sys
from pathlib import Path

PATCH1_OFF = 0x3780
PATCH1_BASE = 0x8010A000

HOOKS = (
    ("fn_b0a4", 0x10A4, 0x1118, "function"),
    ("fn_b118", 0x1118, 0x1174, "function"),
)


def emit(name: str, start: int, blob: bytes, kind: str) -> list[str]:
    lines = [
        f"\t.section\t.text.{name}, \"ax\"",
        f"\t.globl\t\t{name}",
        f"\t.type\t\t{name}, @{kind}",
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
    return lines


def main() -> None:
    nf = Path(
        sys.argv[1]
        if len(sys.argv) > 1
        else "/root/rtl8761bu-libre/rtl8761bu-non-free/rtl8761bu_fw.bin"
    )
    body = nf.read_bytes()[PATCH1_OFF : PATCH1_OFF + 27808]
    out = [
        "/* Generated hook bodies — vendor @ link offset. */",
        "",
        "\t.set\tmips16",
        "",
    ]
    for name, start, end, kind in HOOKS:
        out.extend(emit(name, start, body[start:end], kind))
    sys.stdout.write("\n".join(out))


if __name__ == "__main__":
    main()
