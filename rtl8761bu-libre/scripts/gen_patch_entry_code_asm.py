#!/usr/bin/env python3
"""Emit src/patch_entry_code.S from FUN_8010a000 code body bytes.

Code file range [0, 0x242) — 578 B. Runtime base 0x8010A000.
Phase annotations from Ghidra decompile / reverse_engineering_patch_entry.md.
"""
from __future__ import annotations

import sys
from pathlib import Path

PATCH1_OFF = 0x3780
CODE_SIZE = 0x242
CODE_RT = 0x8010A000

# file_offset -> comment (Ghidra addr = CODE_RT + off)
ANNOTATIONS: dict[int, str] = {
    0x000: "prologue: addiu sp,-0x28; sw ra,s1,s0",
    0x00C: "phase 0 — lw config word @ PTR_DAT_8010a244",
    0x01C: "jalr fn_bss_init via DAT_8010a254",
    0x028: "clear config_base+0xd8 (4× sb)",
    0x038: "hook batch #1–4 + pools",
    0x064: "jalr sub_installer_1 (DAT_8010a278)",
    0x06C: "jalr sub_installer_2 (DAT_8010a27c)",
    0x074: "hooks #5–17",
    0x0D4: "clear config_base+0xe0 bit 14",
    0x0E0: "jalr sub_installer_3",
    0x0F8: "hooks #18–34 + sub_installer_4/5/6",
    0x168: "hooks #35–38",
    0x188: "ROM reg script (**PTR_DAT_8010a38c)(PTR_PTR_8010a390,2)",
    0x198: "chip-rev check (DAT_8010a394 / PTR_DAT_8010a3a0)",
    0x1B4: "fn_version_check, fn_tlv_applier, fn_bdaddr_sync",
    0x1D4: "conditional fn_bb_init + final hooks",
    0x228: "*PTR_DAT_8010a3cc = 4 (patch-active)",
    0x234: "epilogue: restore s0/s1/ra; jr ra; addiu sp,0x28",
}


def emit_code(blob: bytes) -> str:
    if len(blob) != CODE_SIZE:
        raise SystemExit(f"code size {len(blob)} != {CODE_SIZE}")

    lines: list[str] = [
        "/*",
        " * patch_entry_code.S — FUN_8010a000 code body @ file [0, 0x242)",
        " *",
        " * PE-1: libre MIPS16e transcription (578 B .byte block, byte-identical",
        " * to vendor). Ghidra DATA block @ 0x8010A000; ROM calls 0x8010A001.",
        " * Literal pool @ +0x242 in patch_entry_pool.S (PC-relative lw targets).",
        " *",
        " * Regenerate: scripts/gen_patch_entry_code_asm.py",
        " * SPDX-License-Identifier: GPL-2.0-or-later",
        " */",
        "",
        "\t.set\tmips16",
        "",
        "\t.section\t.text.entry",
        "\t.globl\t\tpatch_entry",
        "\t.type\t\tpatch_entry, @function",
        "patch_entry:",
        "",
    ]

    row = 16
    for i in range(0, len(blob), row):
        chunk = blob[i : i + row]
        rt = CODE_RT + i
        note = ANNOTATIONS.get(i, "")
        hdr = f"0x{i:04x} / 0x{rt:08x}"
        if note:
            hdr += f" — {note}"
        lines.append(f"\t/* {hdr} */")
        hexbytes = ", ".join(f"0x{b:02x}" for b in chunk)
        lines.append(f"\t.byte\t{hexbytes}")
        lines.append("")

    lines.append("\t.set\tpatch_entry_end, .")
    lines.append("\t.size\tpatch_entry, patch_entry_end - patch_entry")
    lines.append("\t/* Pool @ +0x242 must follow with no align padding (vendor PC-relative lw). */")
    lines.append("\t.balign\t1")
    lines.append("")
    return "\n".join(lines)


def main() -> None:
    if len(sys.argv) != 3:
        print(f"Usage: {sys.argv[0]} <vendor_fw.bin> <out.S>", file=sys.stderr)
        sys.exit(1)
    nf = Path(sys.argv[1])
    out = Path(sys.argv[2])
    with nf.open("rb") as f:
        f.seek(PATCH1_OFF)
        blob = f.read(CODE_SIZE)
    out.write_text(emit_code(blob), encoding="utf-8")
    print(f"gen_patch_entry_code_asm: {CODE_SIZE} B → {out}")


if __name__ == "__main__":
    main()
