#!/usr/bin/env python3
"""Emit src/patch_entry_pool.S from vendor FUN_8010a000 literal pool bytes.

Pool file range [0x242, 0x5d8) — 918 B. Runtime base 0x8010A242.
Known slot annotations from analysis/reverse_engineering_patch_installer.md Appendix D.
"""
from __future__ import annotations

import sys
from pathlib import Path

POOL_FILE_OFF = 0x242
POOL_SIZE = 0x5D8 - 0x242
POOL_RT = 0x8010A242

# file_offset_in_pool -> comment (Ghidra pool addr = POOL_RT + off)
ANNOTATIONS: dict[int, str] = {
    0x002: "DAT_8010a244 — config word src @ 0x801115f8",
    0x006: "config copy dest high @ 0x80120062",
    0x00A: "config copy dest low @ 0x80120060",
    0x012: "DAT_8010a254 — fn_bss_init @ 0x8010A754",
    0x016: "config_base+0xd8 clear target",
    0x01A: "PTR_PTR_8010a258 — bos_base DRAM slot",
    0x01E: "PTR_FUN_8010bba4 — LMP VSC hook",
    0x022: "hook slot @ 0x801286c0",
    0x026: "LAB_8010be20",
    0x02A: "DAT_8010a278 — sub_installer_1 @ 0x8010A724",
    0x02E: "DAT_8010a27c — sub_installer_2 @ 0x8010A764",
    0x032: "puVar3+0x20 HCI evt slot",
    0x036: "LAB_8010c1e8",
    0x03A: "puVar3+0x24",
    0x03E: "LAB_8010c224",
    0x042: "0x8012088c — FUN_8010b3d8",
    0x046: "FUN_8010b3d8",
    0x04A: "0x80121368 — FUN_8010b0a4",
    0x04E: "FUN_8010b0a4",
    0x052: "0x8012136c",
    0x056: "FUN_8010c198",
    0x05A: "0x80121360",
    0x05E: "FUN_8010d1f4",
    0x062: "0x80121344",
    0x066: "FUN_8010c780",
    0x06A: "0x80125550",
    0x06E: "FUN_8010c63c",
    0x072: "puVar5+0x1c — LAB_8010b7f0",
    0x076: "0x80120c9c — string_assoc installer",
    0x07A: "patch LMP installer fn",
    0x07E: "0x80120de8",
    0x082: "FUN_8010dd1c",
    0x086: "0x80120f10",
    0x08A: "FUN_8010d890",
    0x08E: "0x80120dbc",
    0x092: "FUN_8010d618",
    0x096: "config_base+0xe0 bit-14 clear",
    0x09A: "DAT_8010a2dc — sub_installer_3",
    0x09E: "0x80120f3c",
    0x0A2: "FUN_8010a594",
    0x0A6: "0x80120cf8",
    0x0AA: "FUN_8010c0f4",
    0x0AE: "0x80121414",
    0x0B2: "FUN_8010a4ac",
    0x0B6: "0x801213dc",
    0x0BA: "FUN_8010a49c",
    0x0BE: "0x80121348",
    0x0C2: "FUN_8010bce0",
    0x0C6: "0x80120590",
    0x0CA: "FUN_8010c49c",
    0x0CE: "0x8012067c",
    0x0D2: "FUN_8010c43c",
    0x0D6: "0x80120f4c",
    0x0DA: "FUN_8010d168",
    0x0DE: "0x801213e8",
    0x0E2: "FUN_8010fa34",
    0x0E6: "0x801213c8",
    0x0EA: "FUN_8010f950",
    0x0EE: "0x80121458",
    0x0F2: "FUN_8010fb08",
    0x0F6: "0x80121410",
    0x0FA: "FUN_8010abd0",
    0x0FE: "puVar3+0x50 — LAB_8010f884",
    0x102: "0x80120a0c",
    0x106: "FUN_8010f85c",
    0x10A: "0x80120cd4",
    0x10E: "FUN_8010a550",
    0x112: "0x80120cf4",
    0x116: "FUN_8010c160",
    0x11A: "0x80120824",
    0x11E: "FUN_8010c178",
    0x122: "DAT_8010a364 — sub_installer_4",
    0x126: "DAT_8010a368 — sub_installer_5",
    0x12A: "DAT_8010a36c — sub_installer_6",
    0x12E: "puVar3+0x30 — LAB_8010c088",
    0x132: "secondary+0x30 — LAB_8010b4d0",
    0x136: "secondary struct ptr @ 0x80120960",
    0x13A: "0x80121370",
    0x13E: "FUN_8010a5ac",
    0x142: "0x80120bfc",
    0x146: "FUN_8010c854",
    0x14A: "PTR_DAT_8010a38c — ROM reg-script fn-ptr slot",
    0x14E: "PTR_PTR_8010a390 — reg-cmd array @ 0x80120264",
    0x152: "DAT_8010a394 — FUN_8010e214 silicon rev",
    0x156: "chip-rev save slot",
    0x15A: "PTR_DAT_8010a3a0 — chip-rev table @ 0x80111188",
    0x15E: "expected rev byte ptr",
    0x162: "0x80121334",
    0x166: "FUN_8010c09c",
    0x16A: "puVar3+0x1c — LAB_8010bc74",
    0x16E: "DAT_8010a3b0 — FUN_8010a7b8 TLV applier",
    0x172: "DAT_8010a3b4 — copies_config_bdaddr ROM",
    0x176: "DAT_8010a3b8 — FUN_8010ad38 HW probe",
    0x17A: "DAT_8010a3bc — FUN_8010b04c BB init",
    0x17E: "0x80120cdc",
    0x182: "FUN_8010ce0c",
    0x186: "DAT_8010a3c8 — FUN_8010c278 RF init",
    0x18A: "PTR_DAT_8010a3cc — patch-active @ 0x80120538",
    0x18E: "0x80121020",
    0x192: "FUN_80110ddc",
    0x196: "0x80121220",
    0x19A: "FUN_8010bda0",
    0x19E: "0x8012167c",
    0x1A2: "FUN_8010e350",
    0x1A6: "PTR_config_base_8010a24c — 0x80120070",
    0x1AA: "puVar5 sec_base @ 0x80120830",
}


def emit_pool(blob: bytes) -> str:
    if len(blob) != POOL_SIZE:
        raise SystemExit(f"pool size {len(blob)} != {POOL_SIZE}")

    lines: list[str] = [
        "/*",
        " * patch_entry_pool.S — FUN_8010a000 literal pool @ file [0x242, 0x5d8)",
        " *",
        " * PE-2: libre consolidated literal pool (918 B .byte block). Must use .byte",
        " * not .word — .word raises section alignment and inserts padding after the",
        " * 578 B entry code (breaks PC-relative lw offsets). See Appendix D in",
        " * analysis/reverse_engineering_patch_installer.md for slot semantics.",
        " *",
        " * Regenerate: scripts/gen_patch_entry_pool_asm.py",
        " */",
        "",
        "\t.section\t.text.entry",
        "\t.globl\t\tpatch_entry_pool",
        "\t.type\t\tpatch_entry_pool, @object",
        "patch_entry_pool:",
        "",
    ]

    row = 16
    for i in range(0, len(blob), row):
        chunk = blob[i : i + row]
        rt = POOL_RT + i
        fo = POOL_FILE_OFF + i
        note = ANNOTATIONS.get(i, "")
        if not note and i % 4 == 0:
            note = ANNOTATIONS.get(i, "")
        hdr = f"0x{fo:04x} / 0x{rt:08x}"
        if note:
            hdr += f" — {note}"
        lines.append(f"\t/* {hdr} */")
        hexbytes = ", ".join(f"0x{b:02x}" for b in chunk)
        lines.append(f"\t.byte\t{hexbytes}")
        lines.append("")

    lines.append("\t.size\tpatch_entry_pool, . - patch_entry_pool")
    lines.append("")
    return "\n".join(lines)


def main() -> None:
    if len(sys.argv) != 3:
        print(f"Usage: {sys.argv[0]} <vendor_fw.bin> <out.S>", file=sys.stderr)
        sys.exit(1)
    nf = Path(sys.argv[1])
    out = Path(sys.argv[2])
    with nf.open("rb") as f:
        f.seek(0x3780 + POOL_FILE_OFF)
        blob = f.read(POOL_SIZE)
    out.write_text(emit_pool(blob), encoding="utf-8")
    print(f"gen_patch_entry_pool_asm: {POOL_SIZE} B → {out}")


if __name__ == "__main__":
    main()
