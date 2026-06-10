#!/usr/bin/env python3
"""
pad.py — assemble PRAM image and pad to a fixed size with MIPS16e NOP fill

Usage: pad.py <input.elf|input.bin> <target_size> <output.bin> [entry_offset]

entry_offset defaults to 0 (FC20 → 0x8010A000, ROM calls body+0).
Use 0x6880 only with LAYOUT=gap (legacy, known broken on UB500).

ELF input (preferred): copy every allocated executable section into the PRAM
window [LOAD_BASE, LOAD_BASE + target_size).  Linker scatter holes and the
trailing budget stay NOP-filled — only libre-linked code is placed.

Flat binary input (legacy bisect): optional pre-entry NOP gap, then tail NOP.

The version footer (4 bytes, 0x09A95FD1 LE) is placed at exactly
offset (target_size - 4).
"""

from __future__ import annotations

import struct
import sys
from pathlib import Path

MIPS16E_NOP = b"\x00\x65"  # halfword 0x6500 in little-endian memory
VERSION_FOOTER = 0x09A95FD1
LOAD_BASE = 0x8010A000
DEFAULT_ENTRY_OFFSET = 0

# ELF32 constants
EI_MAG0, EI_MAG3 = 0, 3
ELFMAG = b"\x7fELF"
ET_EXEC = 2
EM_MIPS = 8
SHT_PROGBITS = 1
SHF_ALLOC = 0x2
SHF_EXECINSTR = 0x4


def nop_fill(n: int) -> bytes:
    pairs = n // 2
    tail = n % 2
    return MIPS16E_NOP * pairs + (b"\x00" if tail else b"")


def _u16(data: bytes, off: int) -> int:
    return struct.unpack_from("<H", data, off)[0]


def _u32(data: bytes, off: int) -> int:
    return struct.unpack_from("<I", data, off)[0]


def _elf32_sections(blob: bytes) -> tuple[list[dict], bytes]:
    if blob[EI_MAG0 : EI_MAG3 + 1] != ELFMAG:
        raise ValueError("not an ELF32 file")
    if _u16(blob, 0x12) != EM_MIPS:
        raise ValueError("expected EM_MIPS ELF32")
    if _u16(blob, 0x10) != ET_EXEC:
        raise ValueError("expected ET_EXEC ELF32")

    e_shoff = _u32(blob, 0x20)
    e_shentsize = _u16(blob, 0x2E)
    e_shnum = _u16(blob, 0x30)
    e_shstrndx = _u16(blob, 0x32)
    if e_shentsize < 40:
        raise ValueError(f"unexpected section header size {e_shentsize}")

    shstr_off = _u32(blob, e_shoff + e_shstrndx * e_shentsize + 0x10)
    shstr_size = _u32(blob, e_shoff + e_shstrndx * e_shentsize + 0x14)
    shstr = blob[shstr_off : shstr_off + shstr_size]

    sections: list[dict] = []
    for i in range(e_shnum):
        base = e_shoff + i * e_shentsize
        sh_name = _u32(blob, base)
        sh_type = _u32(blob, base + 4)
        sh_flags = _u32(blob, base + 8)
        sh_addr = _u32(blob, base + 0x0C)
        sh_offset = _u32(blob, base + 0x10)
        sh_size = _u32(blob, base + 0x14)
        end = sh_name
        while end < len(shstr) and shstr[end] != 0:
            end += 1
        name = shstr[sh_name:end].decode("ascii", errors="replace")
        sections.append(
            {
                "name": name,
                "type": sh_type,
                "flags": sh_flags,
                "addr": sh_addr,
                "offset": sh_offset,
                "size": sh_size,
            }
        )
    return sections, blob


def assemble_from_elf(
    elf_path: Path, body_budget: int, load_base: int = LOAD_BASE
) -> tuple[bytearray, int, list[str]]:
    """Lay out PRAM body: NOP background + overlay of executable sections."""
    blob = elf_path.read_bytes()
    sections, blob = _elf32_sections(blob)
    body = bytearray(nop_fill(body_budget))
    code_bytes = 0
    placed: list[str] = []

    for sec in sections:
        if sec["type"] != SHT_PROGBITS:
            continue
        if (sec["flags"] & (SHF_ALLOC | SHF_EXECINSTR)) != (
            SHF_ALLOC | SHF_EXECINSTR
        ):
            continue
        if not sec["name"].startswith(".text"):
            continue
        if sec["size"] == 0:
            continue

        off = sec["addr"] - load_base
        if off < 0 or off >= body_budget:
            continue
        if off + sec["size"] > body_budget:
            raise ValueError(
                f"section {sec['name']} @{sec['addr']:#x} "
                f"({sec['size']} B) exceeds PRAM budget "
                f"({body_budget} B @ {load_base:#x})"
            )

        chunk = blob[sec["offset"] : sec["offset"] + sec["size"]]
        if len(chunk) != sec["size"]:
            raise ValueError(f"short read for section {sec['name']}")
        body[off : off + sec["size"]] = chunk
        code_bytes += sec["size"]
        placed.append(f"{sec['name']}@{off:#x}+{sec['size']:#x}")

    return body, code_bytes, placed


def assemble_from_flat(
    raw: bytes, body_budget: int, entry_off: int
) -> tuple[bytearray, int]:
    """Legacy flat-binary path (bisect / LAYOUT=gap)."""
    if len(raw) > body_budget:
        raise ValueError(
            f"Input ({len(raw)} B) exceeds body budget ({body_budget} B)"
        )

    if entry_off == 0:
        prefix = b""
        entry_and_tail = raw
    elif len(raw) <= entry_off:
        prefix = raw
        entry_and_tail = b""
    else:
        prefix = raw[:entry_off]
        entry_and_tail = raw[entry_off:]

    if len(prefix) > entry_off:
        raise ValueError(
            f".text ({len(prefix)} B) overflows pre-entry budget ({entry_off} B)"
        )
    prefix_padded = prefix + nop_fill(entry_off - len(prefix))
    body = prefix_padded + entry_and_tail
    if len(body) > body_budget:
        raise ValueError(
            f"Patched body ({len(body)} B) exceeds budget ({body_budget} B)"
        )
    tail_nop = nop_fill(body_budget - len(body))
    return bytearray(body + tail_nop), len(raw)


def main() -> None:
    if len(sys.argv) not in (4, 5):
        print(
            f"Usage: {sys.argv[0]} <input.elf|input.bin> <target_size> "
            f"<output.bin> [entry_offset]",
            file=sys.stderr,
        )
        sys.exit(1)

    in_path = Path(sys.argv[1])
    target_sz = int(sys.argv[2])
    out_path = Path(sys.argv[3])
    entry_off = int(sys.argv[4], 0) if len(sys.argv) == 5 else DEFAULT_ENTRY_OFFSET

    footer_bytes = 4
    body_budget = target_sz - footer_bytes

    blob = in_path.read_bytes()
    if blob[:4] == ELFMAG:
        body, code_bytes, placed = assemble_from_elf(in_path, body_budget)
        hole_bytes = body_budget - code_bytes
        mode = "elf"
        detail = f"{len(placed)} sections  code {code_bytes} B  holes/tail NOP {hole_bytes} B"
    else:
        body, code_bytes = assemble_from_flat(blob, body_budget, entry_off)
        hole_bytes = body_budget - code_bytes
        mode = "flat"
        detail = (
            f"entry_off=0x{entry_off:x}  raw {code_bytes} B  "
            f"holes/tail NOP {hole_bytes} B"
        )

    body += struct.pack("<I", VERSION_FOOTER)
    assert len(body) == target_sz

    out_path.write_bytes(body)

    print(
        f"pad: mode={mode}  {detail}  + footer 4 B  = {len(body)} B"
    )
    if mode == "elf" and len(placed) > 6:
        print(f"     sections: {', '.join(placed[:6])} … +{len(placed) - 6} more")


if __name__ == "__main__":
    main()
