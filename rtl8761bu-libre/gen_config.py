#!/usr/bin/env python3
"""
gen_config.py — generate rtl8761bu_config.bin from source

The RTL8761BU firmware download consists of the patch binary followed
by a small config TLV appended to the last VSC 0x20 chunk.

The config format (Realtek vendor-specific, partially documented via
Kovah's leaked-documentation references in the HardwearioNL 2025 talk):

  Byte 0-1: type marker  0x55AB
  Byte 2-3: config entry type/offset (LE uint16)
  Byte 4-5: value (LE uint16)

The original RTL8761BU config is 6 bytes: 55 AB 23 87 00 00
  type=0x55AB, entry=0x8723 (system-config bitmask register), value=0x0000

0x0000 means "use ROM defaults for all system config bits".

BDADDR can be set by using entry offset 0x0030 with a 6-byte value.
This file generates the default (no override) config.

Usage: gen_config.py <output.bin>
"""

import struct
import sys

# Realtek config format:
#   Bytes 0-1: literal marker  0x55 0xAB  (NOT a LE uint16 — stored as-is)
#   Bytes 2-3: LE uint16 entry offset
#   Bytes 4-5: LE uint16 entry value
CONFIG_MARKER     = b'\x55\xab'  # fixed 2-byte literal
SYSCONFIG_OFFSET  = 0x8723       # system config bitmask register offset
SYSCONFIG_VALUE   = 0x0000       # all bits default (no override)


def gen_default_config() -> bytes:
    """Generate the 6-byte default system config (no BDADDR override)."""
    return (CONFIG_MARKER
            + struct.pack('<H', SYSCONFIG_OFFSET)
            + struct.pack('<H', SYSCONFIG_VALUE))


def main():
    if len(sys.argv) != 2:
        print(f"Usage: {sys.argv[0]} <output.bin>", file=sys.stderr)
        sys.exit(1)

    cfg = gen_default_config()
    assert len(cfg) == 6, f"Config size error: {len(cfg)} != 6"

    open(sys.argv[1], 'wb').write(cfg)
    print(f"gen_config: {cfg.hex()} → {sys.argv[1]}")
    print(f"  type=0x{CONFIG_MARKER:04x}  "
          f"offset=0x{SYSCONFIG_OFFSET:04x}  "
          f"value=0x{SYSCONFIG_VALUE:04x}")


if __name__ == '__main__':
    main()
