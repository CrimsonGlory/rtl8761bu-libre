#!/usr/bin/env python3
"""Emit fn_post_10ddc_connect_gaps.S — vendor pad holes after fn_10ddc.

Connect/FC20 bisect (2026-06-11): libre NOP fills [0x1110,0x13D8) breaks FC20
(libre fn_10ddc + vendor tail) and connect (libre prefix past installer).

Gaps:
  fn_b0a4_tail   [0x1110, 0x1118)   8 B — fn_b0a4 slot tail
  fn_b118_tail   [0x116A, 0x1174)  10 B — fn_b118 pool before fn_b174
  fn_b174_tail   [0x11B4, 0x13D8) 548 B — fn_b174 body continuation (FC20 cliff [0x1310,0x1390) inside)

Regenerate:
  scripts/gen_fn_post_10ddc_connect_gaps_asm.py [vendor_fw.bin] > src/fn_post_10ddc_connect_gaps.S
"""
from __future__ import annotations

import sys
from pathlib import Path

from gen_vendor_gap_asm import emit_section, vendor_body

GAPS = (
    ("fn_b0a4_tail", 0x1110, 0x1118),
    ("fn_b118_tail", 0x116A, 0x1174),
    ("fn_b174_tail", 0x11B4, 0x13D8),
)


def main() -> None:
    nf = Path(
        sys.argv[1]
        if len(sys.argv) > 1
        else "/root/rtl8761bu-libre/rtl8761bu-non-free/rtl8761bu_fw.bin"
    )
    body = vendor_body(nf)
    header = [
        "/*",
        " * fn_post_10ddc_connect_gaps.S — vendor bytes in pad.py NOP holes",
        " * after fn_10ddc / before fn_b3d8 (connect + FC20 bisect 2026-06-11).",
        " * Regenerate: scripts/gen_fn_post_10ddc_connect_gaps_asm.py",
        " */",
        "",
    ]
    out = header
    for name, start, end in GAPS:
        out.append(emit_section(name, start, body[start:end]))
    sys.stdout.write("\n".join(out))


if __name__ == "__main__":
    main()
