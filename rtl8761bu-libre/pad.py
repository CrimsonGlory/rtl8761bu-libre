#!/usr/bin/env python3
"""
pad.py — pad a MIPS16e binary to a fixed size

Usage: pad.py <input.bin> <target_size> <output.bin>

Fills the space between the compiled code and the version footer with
MIPS16e NOP instructions (0x6500, stored as bytes 00 65 in LE memory).
Kovah confirmed 0x6500 is the MIPS16e padding NOP used by Realtek.

The version footer (4 bytes, 0x09A95FD1 LE) is placed at exactly
offset (target_size - 4).  The btrtl driver does not check this value,
but the chip's ROM appears to read it during startup.
"""

import struct
import sys

MIPS16E_NOP    = b'\x00\x65'   # halfword 0x6500 in little-endian memory
VERSION_FOOTER = 0x09A95FD1    # from original patch1, last 4 bytes


def main():
    if len(sys.argv) != 4:
        print(f"Usage: {sys.argv[0]} <input.bin> <target_size> <output.bin>",
              file=sys.stderr)
        sys.exit(1)

    in_path   = sys.argv[1]
    target_sz = int(sys.argv[2])
    out_path  = sys.argv[3]

    code = open(in_path, 'rb').read()

    footer_bytes = 4
    code_budget  = target_sz - footer_bytes

    if len(code) > code_budget:
        raise ValueError(
            f"Code ({len(code)} B) exceeds budget "
            f"({code_budget} B = {target_sz} - {footer_bytes} footer).\n"
            f"Increase PATCH_SIZE in the Makefile or reduce code size."
        )

    # Align fill to 2-byte boundary (MIPS16e halfword boundary)
    gap   = code_budget - len(code)
    pairs = gap // 2
    tail  = gap % 2

    nop_fill = MIPS16E_NOP * pairs + (b'\x00' if tail else b'')

    buf = bytearray(code + nop_fill)
    assert len(buf) == code_budget, f"Padding bug: {len(buf)} != {code_budget}"

    # Append version footer
    buf += struct.pack('<I', VERSION_FOOTER)
    assert len(buf) == target_sz

    open(out_path, 'wb').write(buf)

    pct_free = (len(nop_fill) / target_sz) * 100
    print(f"pad: {len(code):>6} B code  "
          f"+ {len(nop_fill):>6} B NOPs ({pct_free:.1f}% free)  "
          f"+ {footer_bytes} B footer  "
          f"= {len(buf)} B")


if __name__ == '__main__':
    main()
