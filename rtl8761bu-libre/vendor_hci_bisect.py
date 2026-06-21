#!/usr/bin/env python3
"""
vendor_hci_bisect.py — named overlay sets for p4 HCI hook-body bisect.

Used with inject_vendor.py VENDOR_OVERLAY_SET=<name> (bisect-only, NF_REF).
"""

# (runtime_addr, size_bytes) — file offset = runtime - 0x8010A000
HCI_OVERLAY_SETS: dict[str, list[tuple[int, int]]] = {
    # Only libre hook body that differs from vendor on current p4-libre (87/88 B).
    "c09c": [
        (0x8010C09C, 88),  # fn_c09c @ PRAM+0x209C … fn_c0f4
    ],
    # bos+0x20/+0x24 and HCI dispatch path (already byte-match vendor; control).
    "t1-hci": [
        (0x8010C1E8, 44),
        (0x8010C224, 60),
        (0x8010D618, 422),
        (0x8010D890, 144),
        (0x8010DD1C, 100),
        (0x8010DAA4, 92),
    ],
    # c09c + T1 HCI handlers (full HCI hook surface).
    "all-hci": [],
}

HCI_OVERLAY_SETS["all-hci"] = HCI_OVERLAY_SETS["c09c"] + HCI_OVERLAY_SETS["t1-hci"]

# Connect bisect — file offsets in patch_padded.bin (not runtime).
# Full installer tail is [0xE4C, 0x6C9C) (tail_end = PATCH1_LEN - 4); 0x13D8 is
# fn_b3d8 entry only — overlays stopping there leave libre [0x13D8,0x6C9C) → FC20 cliff.
_TAIL_END = 0x6C9C
_FILE_E4C = 0xE4C

FILE_OVERLAY_SETS: dict[str, list[tuple[int, int]]] = {
    "e4c-10a4": [(_FILE_E4C, 0x10A4 - _FILE_E4C)],   # fn_10ddc + fn_10ddc_gap
    "10a4-1174": [(0x10A4, 0x1174 - 0x10A4)],  # fn_b0a4 + fn_b118
    "1174-13d8": [(0x1174, 0x13D8 - 0x1174)],  # fn_b174 … fn_b3d8 entry (FC20 cliff alone)
    "e4c-1174": [(_FILE_E4C, 0x1174 - _FILE_E4C)],  # slices 1+2
    "e4c-13d8": [(_FILE_E4C, 0x13D8 - _FILE_E4C)],  # stops at fn_b3d8 — FC20 cliff
    "e4c-tail": [(_FILE_E4C, _TAIL_END - _FILE_E4C)],  # ≡ tail-split SPLIT=0xE4C
}
