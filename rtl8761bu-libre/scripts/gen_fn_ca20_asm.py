#!/usr/bin/env python3
"""Emit fn_ca20 + fn_ca20_gap in protocol_dispatch.S from vendor patch1 bytes."""
from __future__ import annotations

import sys
from pathlib import Path

PATCH1_OFF = 0x3780
FN_OFF = 0x2A20
FN_SIZE = 534
GAP_OFF = 0x2C36
GAP_SIZE = 0x2CB8 - GAP_OFF
PATCH1_BASE = 0x8010A000


def emit_bytes(name: str, blob: bytes, off: int, *, is_fn: bool) -> list[str]:
    kind = "function" if is_fn else "object"
    lines = [
        f"\t.section\t.text.{name}, \"ax\"",
        f"\t.globl\t\t{name}",
        f"\t.type\t\t{name}, @{kind}",
        f"{name}:",
        "",
    ]
    for i in range(0, len(blob), 16):
        chunk = blob[i : i + 16]
        fo = off + i
        lines.append(f"\t/* 0x{fo:04x} / 0x{PATCH1_BASE + fo:08x} */")
        hexbytes = ", ".join(f"0x{b:02x}" for b in chunk)
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
    raw = nf.read_bytes()
    body = raw[PATCH1_OFF + FN_OFF : PATCH1_OFF + FN_OFF + FN_SIZE]
    gap = raw[PATCH1_OFF + GAP_OFF : PATCH1_OFF + GAP_OFF + GAP_SIZE]
    if len(body) != FN_SIZE or len(gap) != GAP_SIZE:
        raise SystemExit(f"short read: body={len(body)} gap={len(gap)}")

    out: list[str] = emit_bytes("fn_ca20", body, FN_OFF, is_fn=True)
    out += emit_bytes("fn_ca20_gap", gap, GAP_OFF, is_fn=False)
    sys.stdout.write("\n".join(out))


if __name__ == "__main__":
    main()
