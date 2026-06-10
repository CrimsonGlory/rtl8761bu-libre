#!/usr/bin/env python3
"""P4 completeness audit: all 44 FUN_8010a000 DRAM hook installs.

Verifies each hook target is implemented (IMPL) or a ROM SHIM — not STUB_RET
in hook_stubs.S. Cross-checks symbols in patch.elf when present.

Exit 0 = PASS; exit 1 = FAIL.
"""

from __future__ import annotations

import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
HOOK_STUBS = ROOT / "src" / "hook_stubs.S"
PATCH_ELF = ROOT / "build" / "patch.elf"

# Appendix D + mandatory_hooks.md §4 — RAM slot, vendor runtime, libre symbol, kind, source
HOOKS: list[tuple[int, str, str, str, str, str]] = [
    (1, "0x80121318", "0x8010b118", "fn_b118", "IMPL", "t1_hooks.S"),
    (2, "0x80120844", "0x8010b174", "fn_b174", "IMPL", "t2_hooks.S"),
    (3, "0x80120784", "0x8010bba4", "fn_bba4", "IMPL", "lmp_vsc.S"),
    (4, "0x801286c0", "0x8010be20", "fn_be20", "IMPL", "t1_hooks.S"),
    (5, "0x801206cc", "0x8010c1e8", "fn_c1e8", "IMPL", "t1_hooks.S"),
    (6, "0x801206d0", "0x8010c224", "fn_c224", "IMPL", "t1_hooks.S"),
    (7, "0x8012088c", "0x8010b3d8", "fn_b3d8", "IMPL", "t1_hooks.S"),
    (8, "0x80121368", "0x8010b0a4", "fn_b0a4", "IMPL", "t1_hooks.S"),
    (9, "0x8012136c", "0x8010c198", "fn_c198", "IMPL", "t1_hooks.S"),
    (10, "0x80121360", "0x8010d1f4", "fn_d1f4", "IMPL", "t1_hooks.S"),
    (11, "0x80121344", "0x8010c780", "fn_c780", "IMPL", "t1_hooks.S"),
    (12, "0x80125550", "0x8010c63c", "fn_c63c", "IMPL", "t1_hooks.S"),
    (13, "0x8012084c", "0x8010b7f0", "fn_b7f0", "IMPL", "t2_hooks.S"),
    (14, "0x80120c9c", "0x8010e27c", "fn_e27c", "IMPL", "callees.S"),
    (15, "0x80120de8", "0x8010dd1c", "fn_dd1c", "IMPL", "t1_hooks.S"),
    (16, "0x80120f10", "0x8010d890", "fn_d890", "IMPL", "t1_hooks.S"),
    (17, "0x80120dbc", "0x8010d618", "fn_d618", "IMPL", "t1_hooks.S"),
    (18, "0x80120f3c", "0x8010a594", "fn_a594", "IMPL", "patch_entry_pool.S"),
    (19, "0x80120cf8", "0x8010c0f4", "fn_c0f4", "IMPL", "t2_hooks.S"),
    (20, "0x80121414", "0x8010a4ac", "fn_a4ac", "IMPL", "patch_entry_pool.S"),
    (21, "0x801213dc", "0x8010a49c", "fn_a49c", "IMPL", "patch_entry_pool.S"),
    (22, "0x80121348", "0x8010bce0", "fn_bce0", "IMPL", "t2_hooks.S"),
    (23, "0x80120590", "0x8010c49c", "fn_c49c", "IMPL", "t1_hooks.S"),
    (24, "0x8012067c", "0x8010c43c", "fn_c43c", "IMPL", "t1_hooks.S"),
    (25, "0x80120f4c", "0x8010d168", "fn_d168", "IMPL", "t3_hooks.S"),
    (26, "0x801213e8", "0x8010fa34", "fn_fa34", "IMPL", "t3_hooks.S"),
    (27, "0x801213c8", "0x8010f950", "fn_f950", "IMPL", "t3_hooks.S"),
    (28, "0x80121458", "0x8010fb08", "fn_fb08", "IMPL", "t3_hooks.S"),
    (29, "0x80121410", "0x8010abd0", "fn_abd0", "IMPL", "t2_hooks.S"),
    (30, "0x801206fc", "0x8010f884", "fn_f884", "IMPL", "t2_hooks.S"),
    (31, "0x80120a0c", "0x8010f85c", "fn_f85c", "IMPL", "t3_hooks.S"),
    (32, "0x80120cd4", "0x8010a550", "fn_a550", "IMPL", "patch_entry_pool.S"),
    (33, "0x80120cf4", "0x8010c160", "fn_c160", "IMPL", "t2_hooks.S"),
    (34, "0x80120824", "0x8010c178", "fn_c178", "IMPL", "t2_hooks.S"),
    (35, "0x801206dc", "0x8010c088", "fn_c088", "SHIM", "shims.S"),
    (36, "0x80120990", "0x8010b4d0", "fn_b4d0", "IMPL", "t2_hooks.S"),
    (37, "0x80121370", "0x8010a5ac", "fn_a5ac", "IMPL", "patch_entry_pool.S"),
    (38, "0x80120bfc", "0x8010c854", "fn_c854", "IMPL", "t2_hooks.S"),
    (39, "0x80121334", "0x8010c09c", "fn_c09c", "IMPL", "t2_hooks.S"),
    (40, "0x801206c8", "0x8010bc74", "fn_bc74", "IMPL", "lmp_vsc.S"),
    (41, "0x80120cdc", "0x8010ce0c", "fn_ce0c", "IMPL", "t3_hooks.S"),
    (42, "0x80121020", "0x80110ddc", "fn_10ddc", "IMPL", "t4_hooks.S"),
    (43, "0x80121220", "0x8010bda0", "fn_bda0", "IMPL", "t2_hooks.S"),
    (44, "0x8012167c", "0x8010e350", "fn_e350", "IMPL", "t3_hooks.S"),
]

STUB_RE = re.compile(r"STUB_RET\s+(fn_\w+)")


def read_stub_symbols() -> set[str]:
    text = HOOK_STUBS.read_text()
    return set(STUB_RE.findall(text))


def nm_symbols() -> dict[str, int] | None:
    if not PATCH_ELF.is_file():
        return None
    try:
        out = subprocess.check_output(
            ["mipsel-linux-gnu-nm", "-n", str(PATCH_ELF)], text=True
        )
    except (subprocess.CalledProcessError, FileNotFoundError):
        return None
    syms: dict[str, int] = {}
    for line in out.splitlines():
        parts = line.split()
        if len(parts) >= 3 and parts[1] in ("T", "t"):
            syms[parts[2]] = int(parts[0], 16)
    return syms


def main() -> int:
    stubs = read_stub_symbols()
    nm = nm_symbols()
    failures: list[str] = []
    impl = shim = 0

    print("P4 hook install audit — 44 FUN_8010a000 DRAM slots")
    print("=" * 72)
    for num, slot, runtime, sym, kind, src in HOOKS:
        if sym in stubs:
            failures.append(f"#{num:2d} {sym} is STUB_RET in hook_stubs.S")
        if nm is not None and sym not in nm:
            failures.append(f"#{num:2d} {sym} missing from patch.elf")
        if kind == "SHIM":
            shim += 1
        else:
            impl += 1
        status = "OK" if sym not in stubs else "STUB"
        elf = f"  elf=0x{nm[sym]:05x}" if nm and sym in nm else ""
        print(f"  #{num:2d} {slot} → {sym:12s} [{kind:4s}] {src:22s} {status}{elf}")

    print("-" * 72)
    print(f"Summary: {impl} IMPL + {shim} SHIM = {impl + shim} hooks")
    print(f"hook_stubs.S STUB_RET symbols: {sorted(stubs) or '(none)'}")

    # fn_eac0_callee is sub-inst #6 indirect callee, not a hook install
    hook_syms = {h[3] for h in HOOKS}
    extra_stubs = stubs - hook_syms
    if extra_stubs:
        print(f"Non-hook stubs (allowed): {sorted(extra_stubs)}")

    if failures:
        print("\nFAIL:")
        for f in failures:
            print(f"  - {f}")
        return 1

    print("\nPASS: all 44 hook installs are IMPL or SHIM (zero STUB_RET among hooks)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
