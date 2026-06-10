#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-2.0-or-later
"""
pack.py — wrap a raw patch binary in Realtek EPatch v2 format

Usage:
  pack.py [--chip-id N] [--dual] <patch.bin> <fw_version_hex> <output.bin>

The btrtl kernel driver (drivers/bluetooth/btrtl.c) validates:
  1. First 8 bytes == "Realtech"  (struct rtl_epatch_header.signature)
  2. Extension magic 0x77FD0451 at the last 4 bytes of the file
  3. chip_id in the patch table == rom_version + 1 (2 for RTL8761BU)

struct rtl_epatch_header (14 bytes, packed):
  0x00  8 B   signature "Realtech"
  0x08  4 B   fw_version (LE)     — dmesg prints e.g. 0x09a98a6b
  0x0C  2 B   num_patches
  0x0E  ...   chip_id[], patch_length[], patch_offset[] (LE)
  0x30        patch data (zero-padded header)
"""

import os
import struct
import sys
import argparse

MAGIC    = b'Realtech'
HEADER   = 48
EXT_SIZE = 72

# Non-free patch-0 slot (chip_id=1); UB500 selects patch-1 only.
PATCH0_LEN   = 0x36E0
PATCH0_OFF   = HEADER
PATCH1_OFF   = 0x3780
PATCH_GAP    = PATCH1_OFF - (PATCH0_OFF + PATCH0_LEN)   # 0x70 bytes

CHIP_FAMILY_CONST = 0x00010EFF
EXT_MAGIC         = 0x77FD0451
MIPS16E_NOP       = b'\x00\x65'

def _nf_ref_path() -> str:
    """Bisect-only path; default make all never calls this."""
    return os.environ.get(
        'NF_REF',
        os.path.join(os.path.dirname(__file__), '..', 'rtl8761bu-non-free', 'rtl8761bu_fw.bin'),
    )


def _libre_patch0_stub() -> bytes:
    """chip_id=1 slot filler — UB500 selects patch1 only; not FC20-downloaded."""
    assert PATCH0_LEN % 2 == 0
    return MIPS16E_NOP * (PATCH0_LEN // 2)


def _load_vendor_patch0() -> bytes:
    nf_ref = _nf_ref_path()
    if not os.path.isfile(nf_ref):
        raise FileNotFoundError(
            f"patch0 source missing: {nf_ref!r} "
            f"(set NF_REF to vendor rtl8761bu_fw.bin; --vendor-patch0 is bisect-only)"
        )
    raw = open(nf_ref, 'rb').read()
    return raw[PATCH0_OFF:PATCH0_OFF + PATCH0_LEN]


def _pad_header(meta: bytes) -> bytes:
    hdr  = MAGIC
    hdr += meta
    hdr += b'\x00' * (HEADER - len(hdr))
    assert len(hdr) == HEADER
    return hdr


def _extension() -> bytes:
    ext  = b'\x00' * 64
    ext += struct.pack('<I', CHIP_FAMILY_CONST)
    ext += struct.pack('<I', EXT_MAGIC)
    assert len(ext) == EXT_SIZE
    return ext


def build_epatch_single(patch_data: bytes, chip_id: int, fw_version: int) -> bytes:
    meta  = struct.pack('<I', fw_version)
    meta += struct.pack('<H', 1)
    meta += struct.pack('<H', chip_id)
    meta += struct.pack('<H', len(patch_data))
    meta += struct.pack('<I', HEADER)
    return _pad_header(meta) + patch_data + _extension()


def build_epatch_dual(patch1_data: bytes, fw_version: int, *, vendor_patch0: bool = False) -> bytes:
    """Two-patch layout: libre NOP stub (or vendor) @0x30, gap, patch1 @0x3780."""
    patch0 = _load_vendor_patch0() if vendor_patch0 else _libre_patch0_stub()
    assert len(patch0) == PATCH0_LEN

    meta  = struct.pack('<I', fw_version)
    meta += struct.pack('<H', 2)
    meta += struct.pack('<H', 1)                    # chip_id[0]
    meta += struct.pack('<H', 2)                    # chip_id[1]
    meta += struct.pack('<H', PATCH0_LEN)
    meta += struct.pack('<H', len(patch1_data))
    meta += struct.pack('<I', PATCH0_OFF)
    meta += struct.pack('<I', PATCH1_OFF)

    body  = patch0
    body += b'\x00' * PATCH_GAP
    body += patch1_data
    return _pad_header(meta) + body + _extension()


def main():
    ap = argparse.ArgumentParser(description='Wrap binary in EPatch v2 format')
    ap.add_argument('patch',      help='Input raw patch binary')
    ap.add_argument('fw_version', help='Firmware version (hex, e.g. 0x09a98a6b)')
    ap.add_argument('output',     help='Output .bin file')
    ap.add_argument('--chip-id',  type=int, default=2,
                    help='EPatch chip_id for single-patch mode (default: 2)')
    ap.add_argument('--dual', action='store_true',
                    help='Emit two-patch layout (patch0 stub + patch1 @0x3780)')
    ap.add_argument('--vendor-patch0', action='store_true',
                    help='Copy patch0 from NF_REF (bisect only; not linux-libre)')
    args = ap.parse_args()

    patch_data = open(args.patch, 'rb').read()
    fw_version = int(args.fw_version, 16)

    if args.dual:
        out = build_epatch_dual(patch_data, fw_version, vendor_patch0=args.vendor_patch0)
    else:
        out = build_epatch_single(patch_data, args.chip_id, fw_version)

    open(args.output, 'wb').write(out)

    print(f"pack: fw_version=0x{fw_version:08x}  patch={len(patch_data)} B  "
          f"total={len(out)} B  mode={'dual' if args.dual else 'single'}  "
          f"→ {args.output}")


if __name__ == '__main__':
    main()
