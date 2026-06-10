#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-2.0-or-later
"""
vendor_t1_manifest.py — T1 hook bodies to overlay from vendor patch1 (NF_REF).

Each entry: (runtime_addr, size_bytes).  Sizes from GZF / analysis docs.
Bodies are copied into libre patch_padded.bin at (addr - PATCH1_BASE).
Installer must INSTALL_HOOK_ABS to the same runtime_addr (+1 for MIPS16e).
"""

PATCH1_BASE = 0x8010A000

# Linked libre image size before tail NOP (pad.py “entry/tail”); vendor prefix for installer bisect.
VENDOR_INSTALLER_PREFIX_LEN = 0xE4C
PATCH1_FOOTER_LEN = 4

# Appendix D + sub-installer #2 (reverse_engineering_sub_installers.md)
VENDOR_OVERLAY: list[tuple[int, int]] = [
    # sub-installer #2 targets (19)
    (0x801102F0, 0x80110310 - 0x801102F0),
    (0x801100BC, 0x801100D0 - 0x801100BC),
    (0x80110724, 0x80110740 - 0x80110724),
    (0x8011021C, 0x80110240 - 0x8011021C),
    (0x8010FF08, 0x8010FF28 - 0x8010FF08),
    (0x801106BC, 0x801106D8 - 0x801106BC),
    (0x801105E8, 0x80110600 - 0x801105E8),
    (0x8010FF28, 0x8010FF48 - 0x8010FF28),
    (0x801105BC, 0x801105D8 - 0x801105BC),
    (0x8010FED8, 0x8010FEF8 - 0x8010FED8),
    (0x8010FFCC, 0x8010FFE8 - 0x8010FFCC),
    (0x80110310, 0x80110330 - 0x80110310),
    (0x8011006C, 0x80110088 - 0x8011006C),
    (0x80110700, 0x8011071C - 0x80110700),
    (0x8011057C, 0x80110598 - 0x8011057C),
    (0x80110044, 0x80110060 - 0x80110044),
    (0x8010FE84, 0x8010FEA0 - 0x8010FE84),
    (0x80110640, 0x8011065C - 0x80110640),
    (0x80110364, 0x80110380 - 0x80110364),
    # T1 INSTALL_HOOK targets (stubbed in hook_stubs.S)
    (0x8010B118, 82),
    (0x8010BE20, 0x8010BE80 - 0x8010BE20),  # ~96 B cluster
    (0x8010C1E8, 44),
    (0x8010C224, 0x8010C260 - 0x8010C224),
    (0x8010B3D8, 206),
    (0x8010B0A4, 108),
    (0x8010C198, 62),   # FUN_8010c198 — PRAM+0x2198
    (0x8010D1F4, 218),  # FUN_8010d1f4 — PRAM+0x31F4 (Ghidra size)
    (0x8010C780, 34),
    (0x8010C63C, 278),
    (0x8010DD1C, 0x8010DD80 - 0x8010DD1C),
    (0x8010D890, 0x8010D920 - 0x8010D890),
    (0x8010D618, 422),
    (0x8010A594, 14),
    (0x8010C0F4, 0x8010C160 - 0x8010C0F4),
    (0x8010A4AC, 68),
    (0x8010A49C, 10),
    (0x8010BCE0, 0x8010BD40 - 0x8010BCE0),
    (0x8010C49C, 0x8010C500 - 0x8010C49C),
    (0x8010C43C, 0x8010C4A0 - 0x8010C43C),
    (0x8010E27C, 52),  # protocol dispatch installer body
    (0x8010CA20, 0x8010CA80 - 0x8010CA20),
    (0x8010DFB0, 0x8010E020 - 0x8010DFB0),
    (0x8010DAA4, 0x8010DB00 - 0x8010DAA4),
    (0x8010DA70, 20),
    (0x8010D9F4, 20),
    (0x8010D154, 0x8010D1C0 - 0x8010D154),
    # FUN_80110ca4 (178 B) is at runtime 0x80110CA4 — 4 B past the 27,808-byte
    # FC20 patch1 image (ends 0x80110CA0).  Not present in rtl8761bu_fw.bin patch1;
    # sub_installer_3 uses INSTALL_HOOK → linked fn_10ca4 stub until reimplemented.
    (0x8010E82C, 0x8010E880 - 0x8010E82C),
]

# T2 INSTALL_HOOK targets still stubbed after full-inject-t1 (Ghidra / analysis sizes).
T2_VENDOR_OVERLAY: list[tuple[int, int]] = [
    (0x8010B174, 64),    # FUN_8010b174 — conn struct +0x14
    (0x8010B7F0, 772),   # FUN_8010b7f0 — LMP eSCO processor
    (0x8010ABD0, 316),   # FUN_8010abd0
    (0x8010D168, 140),   # FUN_8010d168
    (0x8010FA34, 184),   # FUN_8010fa34
    (0x8010F950, 174),   # FUN_8010f950
    (0x8010FB08, 292),   # FUN_8010fb08
    (0x8010F884, 204),   # LAB_8010f884 — bos+0x50
    (0x8010F85C, 40),    # FUN_8010f85c
    (0x8010A550, 54),    # FUN_8010a550
    (0x8010C160, 18),    # FUN_8010c160
    (0x8010C178, 24),    # FUN_8010c178
    (0x8010C088, 16),    # LAB_8010c088 — bos+0x30 (vendor SHIM body)
    (0x8010B4D0, 76),    # LAB_8010b4d0
    (0x8010A5AC, 36),    # FUN_8010a5ac
    (0x8010C854, 460),   # FUN_8010c854
    (0x8010C09C, 76),    # FUN_8010c09c
    (0x8010CE0C, 728),   # FUN_8010ce0c
    (0x8010BDA0, 114),   # FUN_8010bda0
    (0x8010E350, 1174),  # FUN_8010e350
    (0x80110350, 178),   # FUN_80110ca4 body (sub-installer #3)
]

# T3 — LMP gateway + bos+0x1c dispatcher (last libre hooks after full-inject-t2).
T3_VENDOR_OVERLAY: list[tuple[int, int]] = [
    (0x8010BBA4, 176),   # FUN_8010bba4 (+ pool); FUN_8010bb54 callee inside
    (0x8010BC74, 80),    # FUN_8010bc74
]

# Relocate bodies that cannot sit at native offset (overlap libre installer).
# (dest_runtime, src_runtime, size)
VENDOR_RELOC: list[tuple[int, int, int]] = [
    # FUN_80110ddc (448 B) — native 0x8010A9F8 is inside linked installer
    (0x8010AE4C, 0x8010A9F8, 448),
]

# (dram_slot, handler_runtime) — INSTALL_HOOK_ABS in init.S
HOOK_INSTALL_ABS: list[tuple[int, int]] = [
    (0x80121318, 0x8010B118),
    (0x801286C0, 0x8010BE20),
    (0x801206CC, 0x8010C1E8),
    (0x801206D0, 0x8010C224),
    (0x8012088C, 0x8010B3D8),
    (0x80121368, 0x8010B0A4),
    (0x8012136C, 0x8010C198),
    (0x80121360, 0x8010D1F4),
    (0x80121344, 0x8010C780),
    (0x80125550, 0x8010C63C),
    (0x80120DE8, 0x8010DD1C),
    (0x80120F10, 0x8010D890),
    (0x80120DBC, 0x8010D618),
    (0x80120F3C, 0x8010A594),
    (0x80120CF8, 0x8010C0F4),
    (0x80121414, 0x8010A4AC),
    (0x801213DC, 0x8010A49C),
    (0x80121348, 0x8010BCE0),
    (0x80120590, 0x8010C49C),
    (0x8012067C, 0x8010C43C),
    (0x80120C9C, 0x8010E27C),
]

# sub_installer_2 literal pool: (slot, handler) — even runtime addrs
SUB2_INSTALL: list[tuple[int, int]] = [
    (0x80121100, 0x801102F0),
    (0x801210F4, 0x801100BC),
    (0x801205F8, 0x80110724),
    (0x801205B0, 0x8011021C),
    (0x801205A0, 0x8010FF08),
    (0x801205AC, 0x801106BC),
    (0x8012063C, 0x801105E8),
    (0x80120644, 0x8010FF28),
    (0x80120628, 0x801105BC),
    (0x80120634, 0x8010FED8),
    (0x80120624, 0x8010FFCC),
    (0x80120608, 0x80110310),
    (0x80120648, 0x8011006C),
    (0x801205D0, 0x80110700),
    (0x801205D8, 0x8011057C),
    (0x801205E0, 0x80110044),
    (0x801205C4, 0x8010FE84),
    (0x801205FC, 0x80110640),
    (0x80120600, 0x80110364),
]
