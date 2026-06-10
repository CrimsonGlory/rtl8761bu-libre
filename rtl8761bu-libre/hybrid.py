#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-2.0-or-later
"""
hybrid.py — splice vendor + libre patch1 bodies for FC20 bisect

Usage:
  hybrid.py [--libre-head] <libre_padded.bin> <vendor_fw.bin> <split_hex> <out.bin>

Default (vendor-head):  vendor[:split] + libre[split:]
--libre-head:           libre[:split] + vendor[split:]

Footer (0x09A95FD1) is always re-applied at patch1 end.
"""

import argparse
import struct
import sys

PATCH1_OFF = 0x3780
PATCH1_LEN = 27808
FOOTER = 0x09A95FD1


def main():
    ap = argparse.ArgumentParser(description="Splice patch1 bodies for bisect")
    ap.add_argument("--libre-head", action="store_true",
                    help="libre prefix + vendor tail (find offending libre bytes)")
    ap.add_argument("libre")
    ap.add_argument("vendor")
    ap.add_argument("split")
    ap.add_argument("output")
    args = ap.parse_args()

    libre = open(args.libre, "rb").read()
    vendor = open(args.vendor, "rb").read()
    split = int(args.split, 0)
    out_path = args.output

    if len(libre) != PATCH1_LEN or len(vendor) < PATCH1_OFF + PATCH1_LEN:
        raise SystemExit("unexpected input sizes")

    v_body = vendor[PATCH1_OFF:PATCH1_OFF + PATCH1_LEN]
    if split > PATCH1_LEN - 4:
        raise SystemExit("split too large (footer is last 4 bytes)")

    if args.libre_head:
        body = bytearray(libre[:split] + v_body[split:PATCH1_LEN - 4])
        tag = f"libre[:{split:#x}] + vendor[{split:#x}:]"
    else:
        body = bytearray(v_body[:split] + libre[split:PATCH1_LEN - 4])
        tag = f"vendor[:{split:#x}] + libre[{split:#x}:]"

    body += struct.pack("<I", FOOTER)
    assert len(body) == PATCH1_LEN

    open(out_path, "wb").write(body)
    print(f"hybrid: {tag} → {out_path}")


if __name__ == "__main__":
    main()
