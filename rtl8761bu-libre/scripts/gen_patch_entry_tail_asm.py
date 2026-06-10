#!/usr/bin/env python3
"""Emit src/patch_entry_tail.S from vendor installer tail [0x820, 0xE4C).

1580 B transcription: patch_entry epilogue, TLV/BD_ADDR callees, sub-installers
#3–#6, entry callees (e27c, HW probe, BB/RF init), hook bodies through fn_c088
prefix. Ghidra DATA block @ 0x8010A820.

Regenerate after vendor reference update:
  scripts/gen_patch_entry_tail_asm.py ../rtl8761bu-non-free/rtl8761bu_fw.bin src/patch_entry_tail.S
"""
from __future__ import annotations

import sys
from pathlib import Path

PATCH1_OFF = 0x3780
TAIL_OFF = 0x820
TAIL_SIZE = 0xE4C - 0x820
TAIL_RT = 0x8010A000 + TAIL_OFF

# file_offset -> (symbol, comment)
SYMBOLS: dict[int, tuple[str, str]] = {
    0x0820: ("patch_entry_tail", "epilogue: hooks #41,44 + RF init + patch-active"),
    0x08AC: ("fn_tlv_applier", "FUN_8010a7b8 — btrtl config TLV applier"),
    0x097C: ("fn_bdaddr_sync", "ROM copies_config_bdaddr @ 0x8000fd38"),
    0x098C: ("sub_installer_3", "FUN_8010a8a0 — conditional hook @ 0x80120f08"),
    0x09C6: ("sub_installer_4", "FUN_8010a8da — DRAM pointer table copy"),
    0x0A30: ("sub_installer_5", "FUN_8010a944 — hook @ 0x80120c88"),
    0x0A4C: ("sub_installer_6", "FUN_8010a960 — indirect call via 0x80120c80"),
    0x0A88: ("fn_e27c", "FUN_8010a99c — connection record init"),
    0x0AEC: ("fn_hw_probe", "FUN_8010aa60 — silicon / HW probe"),
    0x0AF2: ("fn_bb_init", "FUN_8010aa66 — baseband register init"),
    0x0B5E: ("fn_rf_init", "FUN_8010aad2 — RF register table init"),
    # fn_b0a4 @ PRAM+0x10A4, fn_b118 @ PRAM+0x1118, fn_b3d8 @ PRAM+0x13D8 — t1_hooks.S
    0x0BB0: ("fn_b174", "hook body"),
    # 0x0BB4: mislabeled fn_be20 in tail — runtime 0x8010abb4 is fn_b174 cluster
    # fn_c1e8 @ PRAM+0x21E8, fn_c224 @ PRAM+0x2224 — t1_hooks.S (not tail 0xBB8)
    # 0x0BC4: tail bytes only — fn_b3d8 is @ PRAM+0x13D8 (t1_hooks.S), not 0x8010abc4
    # 0x0BC8: tail bytes only — fn_b0a4 is @ PRAM+0x10A4 (t1_hooks.S), not 0x8010abc8
    # fn_c198 @ PRAM+0x2198 — t1_hooks.S (not tail 0xBCC)
    # fn_c780 @ PRAM+0x2780 — t1_hooks.S (not tail 0xBD0)
    # fn_c63c @ PRAM+0x263C — t1_hooks.S (not tail 0xBD4)
    0x0BD8: ("fn_b7f0", "hook body"),
    # fn_dd1c @ PRAM+0x3D1C, fn_d890 @ PRAM+0x3890, fn_d618 @ PRAM+0x3618 — t1_hooks.S
    # (not tail 0xBDC / 0xBE0 / 0xBE4 mislabels)
    # fn_a594 @ PRAM+0x0594, fn_a49c @ PRAM+0x049C — t1_hooks.S (not tail 0xBE8/0xBF4)
    0x0BEC: ("fn_c0f4", "hook body"),
    0x0BF0: ("fn_a4ac", "hook body"),
    0x0BF8: ("fn_bce0", "hook body"),
    # fn_c49c @ PRAM+0x249C, fn_c43c @ PRAM+0x243C — t1_hooks.S (not tail 0xBFC/0xC00)
    0x0C04: ("fn_d168", "hook body"),
    0x0C08: ("fn_fa34", "hook body"),
    0x0C0C: ("fn_f950", "hook body"),
    0x0C10: ("fn_fb08", "hook body"),
    0x0C14: ("fn_abd0", "hook body"),
    0x0C18: ("fn_f884", "hook body"),
    0x0C1C: ("fn_f85c", "hook body"),
    0x0C20: ("fn_a550", "hook body"),
    0x0C24: ("fn_c160", "hook body"),
    0x0C28: ("fn_c178", "hook body"),
    0x0C2C: ("fn_b4d0", "hook body"),
    0x0C30: ("fn_a5ac", "hook body"),
    0x0C34: ("fn_c854", "hook body"),
    0x0C38: ("fn_c09c", "hook body"),
    0x0C3C: ("fn_ce0c", "FUN_8010ce0c — AFH capability mapper"),
    0x0C40: ("fn_10ddc", "hook body"),
    0x0C44: ("fn_bda0", "FUN_8010bda0 — SCO/eSCO validator"),
    0x0C48: ("fn_e350", "FUN_8010e350 — AFH quality engine (prefix)"),
    0x0C4C: ("fn_10ca4", "hook body"),
    0x0C50: ("fn_e82c", "hook body"),
    0x0C54: ("fn_eac0_callee", "hook body"),
    # fn_dfb0 @ PRAM+0x3FB0 — protocol_dispatch.S (not tail offset 0xC58)
    # fn_daa4 @ PRAM+0x3AA4 — protocol_dispatch.S (not tail offset 0xC5C)
    # fn_d9f4 @ PRAM+0x39F4 — protocol_dispatch.S (not tail offset 0xC64)
    # fn_da70 @ PRAM+0x3A70 — protocol_dispatch.S (not tail offset 0xC60)
    # fn_ca20 @ PRAM+0x2A20 — protocol_dispatch.S (not tail offset 0xC68)
    0x0C6C: ("sub2_fn_00", "sub_installer_2 target"),
    0x0C70: ("sub2_fn_01", "sub_installer_2 target"),
    0x0C74: ("sub2_fn_02", "sub_installer_2 target"),
    0x0C78: ("sub2_fn_03", "sub_installer_2 target"),
    0x0C7C: ("sub2_fn_04", "sub_installer_2 target"),
    0x0C80: ("sub2_fn_05", "sub_installer_2 target"),
    0x0C84: ("sub2_fn_06", "sub_installer_2 target"),
    0x0C88: ("sub2_fn_07", "sub_installer_2 target"),
    0x0C8C: ("sub2_fn_08", "sub_installer_2 target"),
    0x0C90: ("sub2_fn_09", "sub_installer_2 target"),
    0x0C94: ("sub2_fn_10", "sub_installer_2 target"),
    0x0C98: ("sub2_fn_11", "sub_installer_2 target"),
    0x0C9C: ("sub2_fn_12", "sub_installer_2 target"),
    0x0CA0: ("sub2_fn_13", "sub_installer_2 target"),
    0x0CA4: ("sub2_fn_14", "sub_installer_2 target"),
    0x0CA8: ("sub2_fn_15", "sub_installer_2 target"),
    0x0CAC: ("sub2_fn_16", "sub_installer_2 target"),
    0x0CB0: ("sub2_fn_17", "sub_installer_2 target"),
    0x0CB4: ("sub2_fn_18", "sub_installer_2 target"),
    0x0CB8: ("fn_bb54", "FUN_8010bb54 — eSCO validator"),
    0x0D08: ("fn_bba4", "FUN_8010bbb4 region — LMP-related helper"),
    0x0DD8: ("fn_bc74", "FUN_8010bc84 region — bos+0x1c dispatcher"),
    0x0E44: ("fn_c088", "FUN_8010c088 prefix — connection handler (continues @ 0xE4C)"),
}


def emit_tail(blob: bytes) -> str:
    if len(blob) != TAIL_SIZE:
        raise SystemExit(f"tail size {len(blob)} != {TAIL_SIZE}")

    sym_at = sorted(SYMBOLS.keys())
    lines: list[str] = [
        "/*",
        " * patch_entry_tail.S — vendor installer tail @ file [0x820, 0xE4C)",
        " *",
        " * PE-tail: libre MIPS16e transcription (1580 B .byte block, byte-identical",
        " * to vendor). Replaces macro-based patch_entry_tail + separate hook/callee",
        " * objects that previously mismatched layout in [0x820, 0xE4C).",
        " *",
        " * Regenerate: scripts/gen_patch_entry_tail_asm.py",
        " * SPDX-License-Identifier: GPL-2.0-or-later",
        " */",
        "",
        "\t.set\tmips16",
        "",
        "\t.section\t.text.installer_tail",
        "",
    ]

    sym_idx = 0
    row = 16
    for i in range(0, len(blob), row):
        fo = TAIL_OFF + i
        while sym_idx < len(sym_at) and sym_at[sym_idx] <= fo:
            off = sym_at[sym_idx]
            name, note = SYMBOLS[off]
            rt = TAIL_RT + (off - TAIL_OFF)
            lines.append(f"\t.globl\t\t{name}")
            lines.append(f"\t.type\t\t{name}, @function")
            lines.append(f"{name}:")
            lines.append(f"\t/* 0x{off:04x} / 0x{rt:08x} — {note} */")
            sym_idx += 1

        chunk = blob[i : i + row]
        rt = TAIL_RT + i
        hdr = f"0x{fo:04x} / 0x{rt:08x}"
        lines.append(f"\t/* {hdr} */")
        hexbytes = ", ".join(f"0x{b:02x}" for b in chunk)
        lines.append(f"\t.byte\t{hexbytes}")
        lines.append("")

    lines.append("\t.size\tpatch_entry_tail, . - patch_entry_tail")
    lines.append("")
    return "\n".join(lines)


def main() -> None:
    if len(sys.argv) != 3:
        print(f"Usage: {sys.argv[0]} <vendor_fw.bin> <out.S>", file=sys.stderr)
        sys.exit(1)
    nf = Path(sys.argv[1])
    out = Path(sys.argv[2])
    with nf.open("rb") as f:
        f.seek(PATCH1_OFF + TAIL_OFF)
        blob = f.read(TAIL_SIZE)
    out.write_text(emit_tail(blob), encoding="utf-8")
    print(f"gen_patch_entry_tail_asm: {TAIL_SIZE} B → {out}")


if __name__ == "__main__":
    main()
