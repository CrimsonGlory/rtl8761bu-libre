#!/usr/bin/env python3
"""Emit fn_e350 + vendor gap sections in t3_hooks.S from patch1 bytes."""
from __future__ import annotations

import sys
from pathlib import Path

PATCH1_OFF = 0x3780
PRE_GAP_OFF = 0x41C2
PRE_GAP_SIZE = 0x4350 - PRE_GAP_OFF
FN_OFF = 0x4350
FN_SIZE = 1174
POST_GAP_OFF = 0x47E6
POST_GAP_SIZE = 0x585C - POST_GAP_OFF
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


def emit_all(pre: bytes, body: bytes, post: bytes) -> str:
    header = [
        "/*",
        " * fn_e350 region — appended by scripts/gen_fn_e350_asm.py",
        " *",
        f" * fn_e350_pre_gap @ PRAM [0x{PRE_GAP_OFF:04x}, 0x{FN_OFF:04x}) ({PRE_GAP_SIZE} B)",
        f" * fn_e350       @ PRAM [0x{FN_OFF:04x}, 0x{POST_GAP_OFF:04x}) ({FN_SIZE} B)",
        f" * fn_e350_post_gap @ PRAM [0x{POST_GAP_OFF:04x}, 0x585C) ({POST_GAP_SIZE} B)",
        " *",
        " * Decompile: FUN_8010e350 — AFH channel quality ranking engine (79 ch).",
        " * Installed @ RAM slot 0x8012167c (hook #44). Tier T3.",
        " */",
        "",
        "\t.set\tmips16",
        "",
    ]
    lines = header
    lines += emit_bytes("fn_e350_pre_gap", pre, PRE_GAP_OFF, is_fn=False)
    lines += emit_bytes("fn_e350", body, FN_OFF, is_fn=True)
    lines += emit_bytes("fn_e350_post_gap", post, POST_GAP_OFF, is_fn=False)
    return "\n".join(lines)


def main() -> None:
    if len(sys.argv) != 3:
        print(f"Usage: {sys.argv[0]} <vendor_fw.bin> <out_fragment.S>", file=sys.stderr)
        sys.exit(1)
    nf = Path(sys.argv[1])
    out = Path(sys.argv[2])
    data = nf.read_bytes()
    base = PATCH1_OFF
    pre = data[base + PRE_GAP_OFF : base + FN_OFF]
    body = data[base + FN_OFF : base + FN_OFF + FN_SIZE]
    post = data[base + POST_GAP_OFF : base + POST_GAP_OFF + POST_GAP_SIZE]
    if len(pre) != PRE_GAP_SIZE or len(body) != FN_SIZE or len(post) != POST_GAP_SIZE:
        raise SystemExit("unexpected slice sizes")
    out.write_text(emit_all(pre, body, post), encoding="utf-8")
    print(
        f"gen_fn_e350_asm: pre={PRE_GAP_SIZE} body={FN_SIZE} post={POST_GAP_SIZE} B → {out}"
    )


if __name__ == "__main__":
    main()
