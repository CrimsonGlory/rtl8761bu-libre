#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-2.0-or-later
"""
inject_vendor.py — overlay vendor T1 hook bodies into libre patch_padded.bin

Usage:
  inject_vendor.py <libre_padded.bin> <vendor_fw.bin> <output.bin>

Reads vendor_t1_manifest.VENDOR_OVERLAY; copies bytes from vendor patch1
at native offsets.  Requires NF_REF vendor image for reference bodies.
"""

import os
import sys

from vendor_t1_manifest import (
    PATCH1_BASE,
    PATCH1_FOOTER_LEN,
    T2_VENDOR_OVERLAY,
    T3_VENDOR_OVERLAY,
    VENDOR_INSTALLER_PREFIX_LEN,
    VENDOR_OVERLAY,
    VENDOR_RELOC,
)

PATCH1_OFF = 0x3780
PATCH1_LEN = 27808


def _apply_overlay(buf: bytearray, v_body: bytes, runtime: int, size: int, label: str) -> int:
    off = runtime - PATCH1_BASE
    if off < 0 or off + size > PATCH1_LEN:
        raise SystemExit(f"bad overlay {label} {runtime:#x} size {size}")
    buf[off:off + size] = v_body[off:off + size]
    return size


def main():
    if len(sys.argv) != 4:
        print(f"Usage: {sys.argv[0]} <libre_padded.bin> <vendor_fw.bin> <out.bin>",
              file=sys.stderr)
        sys.exit(1)

    libre = bytearray(open(sys.argv[1], 'rb').read())
    vendor = open(sys.argv[2], 'rb').read()
    out = sys.argv[3]

    if len(libre) != PATCH1_LEN:
        raise SystemExit(f"libre padded size {len(libre)} != {PATCH1_LEN}")
    if len(vendor) < PATCH1_OFF + PATCH1_LEN:
        raise SystemExit("vendor fw too small")

    v_body = vendor[PATCH1_OFF:PATCH1_OFF + PATCH1_LEN]
    use_prefix = os.environ.get("VENDOR_INSTALLER_PREFIX", "").lower() in ("1", "yes", "true")
    use_tail = os.environ.get("VENDOR_TAIL_FILL", "").lower() in ("1", "yes", "true")
    prefix_split = 0
    if use_prefix:
        prefix_split = VENDOR_INSTALLER_PREFIX_LEN
    elif os.environ.get("VENDOR_PREFIX_SPLIT"):
        prefix_split = int(os.environ["VENDOR_PREFIX_SPLIT"], 0)

    overlays = VENDOR_OVERLAY + T2_VENDOR_OVERLAY + T3_VENDOR_OVERLAY
    merged = 0
    for runtime, size in overlays:
        merged += _apply_overlay(libre, v_body, runtime, size, "overlay")

    # Full vendor installer owns FUN_80110ddc @ native offset; skip reloc to 0xAE4C.
    relocs = [] if prefix_split >= VENDOR_INSTALLER_PREFIX_LEN else VENDOR_RELOC
    for dest, src, size in relocs:
        src_off = src - PATCH1_BASE
        dest_off = dest - PATCH1_BASE
        if src_off < 0 or src_off + size > PATCH1_LEN:
            raise SystemExit(f"bad reloc src {src:#x} size {size}")
        if dest_off < 0 or dest_off + size > PATCH1_LEN:
            raise SystemExit(f"bad reloc dest {dest:#x} size {size}")
        libre[dest_off:dest_off + size] = v_body[src_off:src_off + size]
        merged += size

    prefix_len = 0
    if prefix_split:
        if prefix_split > VENDOR_INSTALLER_PREFIX_LEN:
            raise SystemExit(f"prefix split {prefix_split:#x} > {VENDOR_INSTALLER_PREFIX_LEN:#x}")
        prefix_len = prefix_split
        libre[0:prefix_len] = v_body[0:prefix_len]
        merged += prefix_len

    tail_len = 0
    if use_tail:
        tail_start = VENDOR_INSTALLER_PREFIX_LEN
        tail_end = PATCH1_LEN - PATCH1_FOOTER_LEN
        if tail_end <= tail_start:
            raise SystemExit("bad tail range")
        tail_len = tail_end - tail_start
        libre[tail_start:tail_end] = v_body[tail_start:tail_end]
        merged += tail_len

    open(out, 'wb').write(libre)
    extras = []
    if prefix_len:
        extras.append(f"prefix {prefix_len} B")
    if tail_len:
        extras.append(f"tail {tail_len} B")
    extra_s = (" + " + " + ".join(extras)) if extras else ""
    print(f"inject_vendor: {len(overlays)} overlays + {len(relocs)} relocs{extra_s}, "
          f"{merged} B from vendor → {out}")


if __name__ == "__main__":
    main()
