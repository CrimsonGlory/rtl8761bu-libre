#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-2.0-or-later
"""
gen_config.py — generate rtl8761bu_config.bin from source

The RTL8761BU firmware download consists of the patch binary followed
by a small config TLV appended to the last VSC 0x20 chunk.

Format matches Linux drivers/bluetooth/btrtl.h:

  struct rtl_vendor_config {
      __le32 signature;   // 0x8723ab55  → bytes 55 AB 23 87
      __le16 total_len;   // sum of (4 + entry.len) for all entries
      struct rtl_vendor_config_entry entry[];
  };
  struct rtl_vendor_config_entry {
      __le16 offset;      // byte offset into chip config_base
      __u8   len;
      __u8   data[];
  };

Default (no overrides): 6 bytes — signature + total_len=0.
  55 AB 23 87 00 00

BD_ADDR override: entry at offset 0x0030, len 6, address bytes LSB-first.

Usage:
  gen_config.py <output.bin>
  gen_config.py --bdaddr AA:BB:CC:DD:EE:FF <output.bin>
"""

import argparse
import struct
import sys

RTL_CONFIG_MAGIC = 0x8723AB55
BDADDR_OFFSET = 0x0030


def _entry_wire(offset: int, data: bytes) -> bytes:
    if not 0 <= offset <= 0xFFFF:
        raise ValueError(f"offset out of range: {offset:#x}")
    if not 1 <= len(data) <= 255:
        raise ValueError(f"entry length must be 1..255, got {len(data)}")
    return struct.pack('<HB', offset, len(data)) + data


def gen_default_config() -> bytes:
    """6-byte header: magic + total_len=0 (ROM defaults, no patches)."""
    return struct.pack('<IH', RTL_CONFIG_MAGIC, 0)


def gen_bdaddr_config(mac: str) -> bytes:
    """
    Build config with one BD_ADDR entry at config_base+0x30.

    mac: colon-separated hex, MSB-first (e.g. AA:BB:CC:DD:EE:FF).
    Wire order is LSB-first per Realtek convention.
    """
    parts = mac.replace('-', ':').split(':')
    if len(parts) != 6:
        raise ValueError(f"expected 6 octets, got {len(parts)} in {mac!r}")
    try:
        octets = [int(p, 16) for p in parts]
    except ValueError as e:
        raise ValueError(f"invalid MAC {mac!r}") from e
    if any(o < 0 or o > 0xFF for o in octets):
        raise ValueError(f"octet out of range in {mac!r}")

    entry = _entry_wire(BDADDR_OFFSET, bytes(reversed(octets)))
    total_len = len(entry)
    return struct.pack('<IH', RTL_CONFIG_MAGIC, total_len) + entry


def main():
    ap = argparse.ArgumentParser(description='Generate rtl8761bu_config.bin')
    ap.add_argument('output', help='Output .bin path')
    ap.add_argument('--bdaddr', metavar='MAC',
                    help='Override public BD_ADDR (AA:BB:CC:DD:EE:FF)')
    args = ap.parse_args()

    if args.bdaddr:
        cfg = gen_bdaddr_config(args.bdaddr)
    else:
        cfg = gen_default_config()

    open(args.output, 'wb').write(cfg)
    print(f"gen_config: {cfg.hex()} → {args.output}")
    if args.bdaddr:
        print(f"  BD_ADDR override @ config_base+0x{BDADDR_OFFSET:04x}")
    else:
        print(f"  magic=0x{RTL_CONFIG_MAGIC:08x}  total_len=0 (ROM defaults)")


if __name__ == '__main__':
    main()
