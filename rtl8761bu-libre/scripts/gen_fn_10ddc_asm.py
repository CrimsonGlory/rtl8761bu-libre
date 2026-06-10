#!/usr/bin/env python3
"""Emit fn_10ddc + gap in t4_hooks.S from vendor patch1 bytes.

FUN_80110ddc (448 B) is transcribed from native PRAM+0x09F8 but linked at
PRAM+0x0E4C (relocated — native offset overlaps installer tail / sub-inst
bodies).  Vendor bridge bytes [0x100C, 0x10A4) fill the gap before fn_b0a4.

Regenerate: scripts/gen_fn_10ddc_asm.py [vendor_fw.bin] > src/t4_hooks.S
"""
from __future__ import annotations

import sys
from pathlib import Path

PATCH1_OFF = 0x3780
PATCH1_BASE = 0x8010A000
FN_SRC_OFF = 0x09F8
FN_SIZE = 448
FN_DEST_OFF = 0x0E4C
GAP_OFF = 0x100C
GAP_SIZE = 0x10A4 - GAP_OFF


def emit(name: str, blob: bytes, pram_off: int, *, is_fn: bool) -> list[str]:
    kind = "function" if is_fn else "object"
    lines = [
        f"\t.section\t.text.{name}, \"ax\"",
        f"\t.globl\t\t{name}",
        f"\t.type\t\t{name}, @{kind}",
        f"{name}:",
        f"\t/* PRAM+0x{pram_off:04x} runtime 0x{PATCH1_BASE + pram_off:08x} size {len(blob)} */",
        "",
    ]
    for i in range(0, len(blob), 16):
        chunk = blob[i : i + 16]
        fo = pram_off + i
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
    fn_blob = body[FN_SRC_OFF : FN_SRC_OFF + FN_SIZE]
    gap_blob = body[GAP_OFF : GAP_OFF + GAP_SIZE]
    if len(fn_blob) != FN_SIZE or len(gap_blob) != GAP_SIZE:
        raise SystemExit("unexpected slice sizes")

    header = [
        "/*",
        " * t4_hooks.S — T4 hook bodies (P4 parity).",
        " *",
        f" * fn_10ddc @ PRAM+0x{FN_DEST_OFF:04x}: FUN_80110ddc relocated from native 0x{FN_SRC_OFF:04x}",
        " * (native overlaps installer tail / sub-inst bodies; pool → fn_10ddc+1).",
        f" * fn_10ddc_gap @ PRAM+0x{GAP_OFF:04x}: vendor bridge before fn_b0a4.",
        " * Regenerate: scripts/gen_fn_10ddc_asm.py [vendor_fw.bin] > src/t4_hooks.S",
        " */",
        "",
        "\t.set\tmips16",
        "",
    ]
    out = header
    out += emit("fn_10ddc", fn_blob, FN_DEST_OFF, is_fn=True)
    out += emit("fn_10ddc_gap", gap_blob, GAP_OFF, is_fn=False)
    sys.stdout.write("\n".join(out))
    print(
        f"gen_fn_10ddc_asm: fn={FN_SIZE} B @ 0x{FN_DEST_OFF:x}, gap={GAP_SIZE} B",
        file=sys.stderr,
    )


if __name__ == "__main__":
    main()
