#!/usr/bin/env python3
"""Emit sub2_hooks.S — sub-installer #2 targets (19 fn bodies @ vendor PRAM offsets).

Regenerate: scripts/gen_sub2_hooks_asm.py [vendor_fw.bin] > src/sub2_hooks.S
"""
from __future__ import annotations

import sys
from pathlib import Path

# First 19 entries in vendor_t1_manifest.VENDOR_OVERLAY (sub-installer #2 targets)
SUB2_TARGETS: list[tuple[int, int]] = [
    (0x801102F0, 0x80110310 - 0x801102F0),
    (0x801100BC, 0x801100D0 - 0x801100BC),
    (0x80110724, 0x80110740 - 0x80110724),
    (0x8011021C, 0x80110240 - 0x8011021C),
    (0x8010FF08, 0x8010FF28 - 0x8010FF08),
    (0x801106BC, 0x801106D8 - 0x801106BC),
    (0x801105E8, 0x80110600 - 0x801105E8),
    (0x8010FF28, 0x8010FF48 - 0x8010FF28),
    (0x801105BC, 0x801105D8 - 0x801105BC),
    (0x8010FED8, 0x8010FEF8 - 0x8010FED8),
    (0x8010FFCC, 0x8010FFE8 - 0x8010FFCC),
    (0x80110310, 0x80110330 - 0x80110310),
    (0x8011006C, 0x80110088 - 0x8011006C),
    (0x80110700, 0x8011071C - 0x80110700),
    (0x8011057C, 0x80110598 - 0x8011057C),
    (0x80110044, 0x80110060 - 0x80110044),
    (0x8010FE84, 0x8010FEA0 - 0x8010FE84),
    (0x80110640, 0x8011065C - 0x80110640),
    (0x80110364, 0x80110380 - 0x80110364),
]

PATCH1_OFF = 0x3780
PATCH1_BASE = 0x8010A000


def emit_fn(name: str, blob: bytes, pram_off: int, runtime: int) -> list[str]:
    sec = f".text.{name}"
    lines = [
        f"\t.section\t{sec}, \"ax\"",
        f"\t.globl\t\t{name}",
        f"\t.type\t\t{name}, @function",
        f"{name}:",
        f"\t/* PRAM+0x{pram_off:04x} runtime 0x{runtime:08x} size {len(blob)} */",
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
    raw = nf.read_bytes()
    header = [
        "/*",
        " * sub2_hooks.S — sub-installer #2 targets (FUN_8011011c pool, 19 fn-ptrs).",
        " *",
        " * Vendor byte-identical bodies @ native PRAM offsets; sub_installer_2 pool",
        " * already points to runtime 0x8010xxxx (+1 MIPS16e). Regenerate:",
        " *   scripts/gen_sub2_hooks_asm.py [vendor_fw.bin] > src/sub2_hooks.S",
        " */",
        "",
        "\t.set\tmips16",
        "",
    ]
    out: list[str] = header
    total = 0
    for idx, (runtime, size) in enumerate(SUB2_TARGETS):
        pram_off = runtime - PATCH1_BASE
        blob = raw[PATCH1_OFF + pram_off : PATCH1_OFF + pram_off + size]
        if len(blob) != size:
            raise SystemExit(f"short read sub2_fn_{idx:02d} @ 0x{pram_off:x}: {len(blob)} != {size}")
        name = f"sub2_fn_{idx:02d}"
        out += emit_fn(name, blob, pram_off, runtime)
        total += size
    sys.stdout.write("\n".join(out))
    print(f"/* gen_sub2_hooks_asm: {len(SUB2_TARGETS)} fns, {total} B */", file=sys.stderr)


if __name__ == "__main__":
    main()
