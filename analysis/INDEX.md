# Analysis Findings Index

All reverse-engineering and design findings for the RTL8761BU libre firmware project.
See `/root/rtl8761bu-libre/CLAUDE.md` for project context and tool stack.

| File | What it covers |
|------|---------------|
| `kovah_function_list.md` | Full list of Kovah's annotated function/label names |
| `reverse_engineering_patch_installer.md` | Master installer `FUN_80103780`: all 50+ fptr installs, 6 sub-installers, phase-by-phase |
| `reverse_engineering_hardware_layer.md` | `FUN_8004f824` slot-budget validator + 7-entry interval table; hook-based HW abstraction architecture |
| `reverse_engineering_lmp_vsc_hook.md` | `FUN_8010bba4` — LMP VSC hook entry point |
| `reverse_engineering_vsc_dispatcher.md` | `FUN_80047c50` — ROM VSC parameter validator and connection record writer |
| `reverse_engineering_conn_record_subsystem.md` | ROM eSCO/SCO connection record pool: allocation, registration, lookup, hardware commit, free |
| `reverse_engineering_rom_regs.md` | ROM HW register r/w protocol (`0x8001136c`/`9c`, MMIO `0xb000a0bc`) |
| `reverse_engineering_lmp_vsc_opcode_map.md` | LMP / HCI VSC opcode dispatch map |
| `reverse_engineering_protocol_dispatch_layer.md` | Protocol dispatch layer analysis (string-assoc + remaining new functions) |
| `reverse_engineering_string_assoc_installer.md` | String-associated function installer (`FUN_8010e27c`) analysis |
| `reverse_engineering_sub_installers.md` | All 6 sub-installers and the patch boot sequence |
| `reverse_engineering_sco_esco_layer.md` | SCO/eSCO layer & slot scheduler — 28 functions across 5 analysis passes |
| `reverse_engineering_config_blob.md` | `rtl8761bu_config.bin` format, BD_ADDR delivery, ROM/patch consumption (Phase 5) |
| `reverse_engineering_patch_entry.md` | `patch_entry` (`FUN_8010a000` region) — libre `init.S`/`bootstrap.S` replacement for vendor prefix |
| `reverse_engineering_libre_patch_architecture.md` | Master design doc for the libre patch architecture (Phase 5 → Phase 6 implementation reference) |
| `reverse_engineering_libre_patch_layout.md` | Libre patch binary layout design spec |
| `reverse_engineering_mandatory_hooks.md` | Mandatory vs. optional ROM hooks — libre design decisions |
| `reverse_engineering_minimum_feature_set.md` | Minimum vs. full feature set classification for libre implementation |
| `reverse_engineering_stub_implementations.md` | Stub implementations for non-critical functions — design spec |
| `reverse_engineering_address_pair_table_omit.md` | Address-pair table @ file `0xA0` — intentional OMIT decision for all libre profiles (P1–P4) |
| `reverse_engineering_late_patch_block_omit.md` | Late-patch block `FUN_80109980`–`80109824` — intentional OMIT decision for all libre profiles (P1–P4) |
| `reverse_engineering_p4_hook_audit.md` | P4 completeness audit — 44 `FUN_8010a000` hook installs, IMPL/SHIM/STUB_RET status |
| `reverse_engineering_phase7_hci_bringup.md` | Phase 7 — basic HCI bring-up verification (working BT controller post-FC20 load) |
| `reverse_engineering_linux_libre_compliance.md` | Linux-libre blob policy compliance audit |

**Keep this index current**: any time a new file is added to `analysis/`, add a row
here in the same turn — filename + one-line summary of its scope/verdict. Do this
immediately, not as a follow-up.
