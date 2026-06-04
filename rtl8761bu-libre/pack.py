#!/usr/bin/env python3
"""
pack.py — wrap a raw patch binary in Realtek EPatch v2 format

Usage: pack.py [--chip-id N] <patch.bin> <fw_version_hex> <output.bin>

The btrtl kernel driver (drivers/bluetooth/btrtl.c) validates:
  1. Magic bytes "Realtech" at offset 0
  2. Extension section magic 0x77FD0451 at the last 4 bytes of the file
  3. chip_id in the patch table matching (rom_version + 1) = 2 for RTL8761BU
  4. fw_version matching the value reported by the chip (0x09a98a6b)

EPatch v2 header layout (all values little-endian):
  0x00  8 B   magic "Realtech"
  0x08  4 B   fw_version
  0x0C  2 B   num_patches         (we always write 1)
  0x0E  2 B   chip_id[0]
  0x10  2 B   patch_length[0]
  0x12  4 B   patch_offset[0]     (= 0x0030, header is 48 bytes)
  0x16  ...   zero padding to 0x0030

Extension section (72 bytes appended after all patches):
  64 B   zeros
   4 B   chip-family constant  0x00010EFF
   4 B   magic                 0x77FD0451   ← driver checks this
"""

import struct
import sys
import argparse

MAGIC    = b'Realtech'
HEADER   = 48          # fixed header size; patch data starts at offset 0x30
EXT_SIZE = 72          # extension section appended after patch data

# Chip-family constant embedded in the extension section.
# Extracted from the original RTL8761BU firmware extension section.
CHIP_FAMILY_CONST = 0x00010EFF
EXT_MAGIC         = 0x77FD0451


def build_epatch(patch_data: bytes, chip_id: int, fw_version: int) -> bytes:
    patch_len    = len(patch_data)
    patch_offset = HEADER            # 0x30

    # ── Header ──────────────────────────────────────────────────────
    hdr  = MAGIC                                    # 8 B  magic
    hdr += struct.pack('<I', fw_version)            # 4 B  fw_version
    hdr += struct.pack('<H', 1)                     # 2 B  num_patches = 1
    hdr += struct.pack('<H', chip_id)               # 2 B  chip_id[0]
    hdr += struct.pack('<H', patch_len)             # 2 B  patch_length[0]
    hdr += struct.pack('<I', patch_offset)          # 4 B  patch_offset[0]
    hdr += b'\x00' * (HEADER - len(hdr))           # zero padding to 48 B
    assert len(hdr) == HEADER

    # ── Extension section ────────────────────────────────────────────
    ext  = b'\x00' * 64
    ext += struct.pack('<I', CHIP_FAMILY_CONST)
    ext += struct.pack('<I', EXT_MAGIC)
    assert len(ext) == EXT_SIZE

    return hdr + patch_data + ext


def main():
    ap = argparse.ArgumentParser(description='Wrap binary in EPatch v2 format')
    ap.add_argument('patch',      help='Input raw patch binary')
    ap.add_argument('fw_version', help='Firmware version (hex, e.g. 0x09a98a6b)')
    ap.add_argument('output',     help='Output .bin file')
    ap.add_argument('--chip-id',  type=int, default=2,
                    help='EPatch chip_id (default: 2 for RTL8761BU)')
    args = ap.parse_args()

    patch_data = open(args.patch, 'rb').read()
    fw_version = int(args.fw_version, 16)

    out = build_epatch(patch_data, args.chip_id, fw_version)
    open(args.output, 'wb').write(out)

    print(f"pack: chip_id={args.chip_id}  fw_version=0x{fw_version:08x}  "
          f"patch={len(patch_data)} B  total={len(out)} B  → {args.output}")


if __name__ == '__main__':
    main()
