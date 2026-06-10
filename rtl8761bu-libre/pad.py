#!/usr/bin/env python3
"""
pad.py — pad a MIPS16e binary to a fixed size

Usage: pad.py <input.bin> <target_size> <output.bin> [entry_offset]

entry_offset defaults to 0 (FC20 → 0x8010A000, ROM calls body+0).
Use 0x6880 only with LAYOUT=gap (legacy, known broken on UB500).

The input is a flat binary from objcopy (load base 0x8010A000).
Optional gaps before patch_entry are filled with MIPS16e NOP (0x6500).
Trailing space before the version footer is also NOP-filled.

The version footer (4 bytes, 0x09A95FD1 LE) is placed at exactly
offset (target_size - 4).
"""

import struct
import sys

MIPS16E_NOP    = b'\x00\x65'   # halfword 0x6500 in little-endian memory
VERSION_FOOTER = 0x09A95FD1
DEFAULT_ENTRY_OFFSET = 0


def nop_fill(n: int) -> bytes:
    pairs = n // 2
    tail  = n % 2
    return MIPS16E_NOP * pairs + (b'\x00' if tail else b'')


def main():
    if len(sys.argv) not in (4, 5):
        print(f"Usage: {sys.argv[0]} <input.bin> <target_size> <output.bin> "
              f"[entry_offset]", file=sys.stderr)
        sys.exit(1)

    in_path   = sys.argv[1]
    target_sz = int(sys.argv[2])
    out_path  = sys.argv[3]
    entry_off = int(sys.argv[4], 0) if len(sys.argv) == 5 else DEFAULT_ENTRY_OFFSET

    raw = open(in_path, 'rb').read()
    footer_bytes = 4
    body_budget  = target_sz - footer_bytes

    if len(raw) > body_budget:
        raise ValueError(
            f"Input ({len(raw)} B) exceeds body budget ({body_budget} B)"
        )

    # objcopy lays out [.text @0][gap][.text.entry @entry_off] (or entry @0).
    if entry_off == 0:
        prefix = b''
        entry_and_tail = raw
    elif len(raw) <= entry_off:
        prefix = raw
        entry_and_tail = b''
    else:
        prefix = raw[:entry_off]
        entry_and_tail = raw[entry_off:]

    if len(prefix) > entry_off:
        raise ValueError(
            f".text ({len(prefix)} B) overflows pre-entry budget "
            f"({entry_off} B)"
        )
    prefix_padded = prefix + nop_fill(entry_off - len(prefix))

    body = prefix_padded + entry_and_tail
    if len(body) > body_budget:
        raise ValueError(
            f"Patched body ({len(body)} B) exceeds budget ({body_budget} B)"
        )

    tail_nop = nop_fill(body_budget - len(body))
    buf = bytearray(body + tail_nop)
    buf += struct.pack('<I', VERSION_FOOTER)
    assert len(buf) == target_sz

    open(out_path, 'wb').write(buf)

    print(f"pad: entry_off=0x{entry_off:x}  prefix {len(prefix):>5} B "
          f"+ NOP gap {entry_off - len(prefix):>5} B  "
          f"+ entry/tail {len(entry_and_tail):>5} B  "
          f"+ tail NOP {len(tail_nop):>5} B  + footer 4 B  = {len(buf)} B")


if __name__ == '__main__':
    main()
