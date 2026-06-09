#!/usr/bin/env python3
"""Extract patch1 prefix bytes from vendor rtl8761bu_fw.bin for .incbin overlay."""
import sys

PATCH1_OFF = 0x3780


def main() -> None:
    if len(sys.argv) not in (4, 5):
        print(
            f"Usage: {sys.argv[0]} <vendor_fw.bin> <length_hex> <out.bin> [start_hex]",
            file=sys.stderr,
        )
        sys.exit(1)
    nf_path, length_s, out_path = sys.argv[1:4]
    start = int(sys.argv[4], 0) if len(sys.argv) == 5 else 0
    length = int(length_s, 0)
    with open(nf_path, "rb") as f:
        f.seek(PATCH1_OFF + start)
        blob = f.read(length)
    if len(blob) != length:
        raise SystemExit(f"short read: got {len(blob)} want {length}")
    with open(out_path, "wb") as f:
        f.write(blob)
    print(
        f"extract_vendor_prefix: {length:#x} B from patch1+{start:#x} → {out_path}"
    )


if __name__ == "__main__":
    main()
