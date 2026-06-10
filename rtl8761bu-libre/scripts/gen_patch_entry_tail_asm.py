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

# fn_abd0 @ PRAM+0xBD0 (316 B) — t2_hooks.S; gap in installer_tail
ABD0_OFF = 0xBD0
ABD0_SIZE = 316
ABD0_END = ABD0_OFF + ABD0_SIZE  # 0xD0C

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
    # fn_b174 @ PRAM+0x1174 — t2_hooks.S (not tail 0xBB0 gap bytes)
    # fn_c1e8 @ PRAM+0x21E8, fn_c224 @ PRAM+0x2224 — t1_hooks.S (not tail 0xBB8)
    # 0x0BC4: tail bytes only — fn_b3d8 is @ PRAM+0x13D8 (t1_hooks.S), not 0x8010abc4
    # 0x0BC8: tail bytes only — fn_b0a4 is @ PRAM+0x10A4 (t1_hooks.S), not 0x8010abc8
    # fn_c198 @ PRAM+0x2198 — t1_hooks.S (not tail 0xBCC)
    # fn_c780 @ PRAM+0x2780 — t1_hooks.S (not tail 0xBD0)
    # fn_c63c @ PRAM+0x263C — t1_hooks.S (not tail 0xBD4)
    # fn_b7f0 @ PRAM+0x17F0 — t2_hooks.S (not tail 0xBD8 gap bytes)
    # fn_dd1c @ PRAM+0x3D1C, fn_d890 @ PRAM+0x3890, fn_d618 @ PRAM+0x3618 — t1_hooks.S
    # (not tail 0xBDC / 0xBE0 / 0xBE4 mislabels)
    # fn_a594 @ PRAM+0x0594, fn_a49c @ PRAM+0x049C — t1_hooks.S (not tail 0xBE8/0xBF4)
    # fn_c09c @ PRAM+0x209C — t2_hooks.S (vendor-fixed before fn_c0f4)
    # fn_c0f4 @ PRAM+0x20F4 — t2_hooks.S (not tail 0xBEC mislabel)
    # fn_c160 @ PRAM+0x2160, fn_c178 @ PRAM+0x2178 — t2_hooks.S (+ gap pools)
    # fn_a4ac @ PRAM+0x04AC — t2_hooks.S pool alias (not tail 0xBF0 mislabel)
    # fn_bce0 @ PRAM+0x1CE0 — t2_hooks.S (not tail 0xBF8 mislabel)
    # fn_abd0 @ PRAM+0xBD0 — t2_hooks.S overlays tail [0xBD0,0xD0C); not 0xC14 mislabel
    # fn_c49c @ PRAM+0x249C, fn_c43c @ PRAM+0x243C — t1_hooks.S (not tail 0xBFC/0xC00)
    # [0xBD0,0xD0C): fn_abd0 — t2_hooks.S (mislabels fn_d168…fn_bb54 prefix omitted)
    # fn_dfb0 @ PRAM+0x3FB0 — protocol_dispatch.S (not tail offset 0xC58)
    # fn_daa4 @ PRAM+0x3AA4 — protocol_dispatch.S (not tail offset 0xC5C)
    # fn_d9f4 @ PRAM+0x39F4 — protocol_dispatch.S (not tail offset 0xC64)
    # fn_da70 @ PRAM+0x3A70 — protocol_dispatch.S (not tail offset 0xC60)
    # fn_ca20 @ PRAM+0x2A20 — protocol_dispatch.S (not tail offset 0xC68)
    0x0D08: ("fn_bba4", "FUN_8010bbb4 region — LMP-related helper"),
    0x0DD8: ("fn_bc74", "FUN_8010bc84 region — bos+0x1c dispatcher"),
    0x0E44: ("fn_c088", "FUN_8010c088 prefix — connection handler (continues @ 0xE4C)"),
}


def _emit_blob_rows(
    lines: list[str],
    blob: bytes,
    blob_start_off: int,
    sym_at: list[int],
    sym_idx: int,
    fo_min: int,
    fo_max: int,
) -> int:
    """Emit 16-byte rows for blob slice; symbols with file offset in [fo_min, fo_max)."""
    row = 16
    for i in range(0, len(blob), row):
        fo = blob_start_off + i
        if fo >= fo_max:
            break
        while sym_idx < len(sym_at) and sym_at[sym_idx] < fo_min:
            sym_idx += 1
        while sym_idx < len(sym_at) and sym_at[sym_idx] <= fo:
            off = sym_at[sym_idx]
            if fo_min <= off < fo_max:
                name, note = SYMBOLS[off]
                rt = TAIL_RT + (off - TAIL_OFF)
                lines.append(f"\t.globl\t\t{name}")
                lines.append(f"\t.type\t\t{name}, @function")
                lines.append(f"{name}:")
                lines.append(f"\t/* 0x{off:04x} / 0x{rt:08x} — {note} */")
            sym_idx += 1

        chunk = blob[i : i + row]
        chunk = chunk[: fo_max - fo]
        if not chunk:
            break
        rt = TAIL_RT + (fo - TAIL_OFF)
        hdr = f"0x{fo:04x} / 0x{rt:08x}"
        lines.append(f"\t/* {hdr} */")
        hexbytes = ", ".join(f"0x{b:02x}" for b in chunk)
        lines.append(f"\t.byte\t{hexbytes}")
        lines.append("")
    return sym_idx


def emit_tail(blob: bytes) -> str:
    if len(blob) != TAIL_SIZE:
        raise SystemExit(f"tail size {len(blob)} != {TAIL_SIZE}")

    pre_end = ABD0_OFF - TAIL_OFF
    post_start = ABD0_END - TAIL_OFF
    pre = blob[:pre_end]
    post = blob[post_start:]

    sym_at = sorted(SYMBOLS.keys())
    lines: list[str] = [
        "/*",
        " * patch_entry_tail.S — vendor installer tail @ file [0x820, 0xE4C)",
        " *",
        " * Split @ [0xBD0,0xD0C) for fn_abd0 (t2_hooks.S). Pre+fn_abd0+post = 1580 B.",
        " * Regenerate: scripts/gen_patch_entry_tail_asm.py",
        " */",
        "",
        "\t.set\tmips16",
        "",
        "\t.section\t.text.installer_tail",
        "",
    ]

    sym_idx = _emit_blob_rows(lines, pre, TAIL_OFF, sym_at, 0, TAIL_OFF, ABD0_OFF)
    lines.append("\t.size\tpatch_entry_tail, . - patch_entry_tail")
    lines.append("")
    lines.append(f"\t/* [0x{ABD0_OFF:04x},0x{ABD0_END:04x}) fn_abd0 @ t2_hooks.S */")
    lines.append("")
    lines.append("\t.section\t.text.installer_tail_cont, \"ax\"")
    lines.append("")
    _emit_blob_rows(
        lines, post, ABD0_END, sym_at, sym_idx, ABD0_END, TAIL_OFF + TAIL_SIZE
    )
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
