# ROM Function Index

`analysis/rom/` equivalent of `kovah_function_list.md`, scoped to the `rom`
memory block only (`0x80000000`–`0x8007ffff`). Every function in the block
is accounted for, split into two parts:

- **Named functions** (461) — full per-function table: address, size, name,
  one-line purpose, confidence/coverage flag.
- **Unnamed functions** (2278, auto-named `FUN_8000xxxx` by Ghidra) — compact
  coverage summary (count, address-range distribution, size stats), not a
  per-function table. Giving each of these a real purpose is the rest of
  Phase 9's ongoing, best-effort work (see `work-in-progress.txt`).

This doc **supersedes the ad-hoc "new ROM fns" callouts** scattered across
Phases 1–8 — those are now consolidated into the table below, with each
function's confidence flag reflecting whatever Phase 1–8 doc (if any)
covers it.

Source: `RomNamedFuncAddrs.java` + `ExtractAnnotations.java` + `ListAllFunctions.java`,
GZF process mode, run 2026-06-21, against
`2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf` (`rom` block only).

---

## Summary

| Metric | Count |
|--------|-------|
| Total functions in `rom` block | 2739 (2738 effective — `0x8000046c` reclassified 2026-06-22 pass 2 as a non-function/padding artifact, not a real Ghidra function; not yet re-run through `RomCoverageStats.java` to confirm the analyzer-level count drops, noted here as a known pending discrepancy) |
| Named functions (this doc's table) | 624 (461 baseline + 12 pass-1 + 19 pass-2 + 74 pass-3 + 27 pass-4 + 31 pass-5, 2026-06-22 region-0x80000000 sweep) |
| Unnamed (`FUN_*`) functions (summarized below) | 2114 (2278 baseline − 12 pass-1 − 19 pass-2 − 74 pass-3 − 27 pass-4 − 31 pass-5 − 1 reclassified non-function) |
| Named-function confidence: **high** (decompiled + written up in a dedicated `rom/*.md`) | 221 (190 after pass 4 + 31 pass-5 new) |
| Named-function confidence: **medium** (named, one-line purpose only, not decompiled) | 105 |
| Named-function confidence: **low** (named by Kovah, purpose unclear) | 299 |

**2026-06-22 update (pass 1)**: Phase 9's region-by-region exhaustive sweep
(`work-in-progress.txt`, "PHASE 9 CONTINUED") resolved its first 12
functions this pass, all in ROM region `0x80000000`-`0x8000ffff`. See
`rom/reverse_engineering_region_0x80000000.md` for full detail; the 12 new
rows are appended at the end of the named-function table below (kept
separate from the original 2026-06-21 alphabetical-by-address run for easy
diffing — a future re-run of `RomNamedFuncAddrs.java` will naturally merge
them back into address order).

**2026-06-22 update (pass 2, same-day continuation)**: resolved 19 more
functions in the same region (`0x80000000`-`0x8000ffff`), starting from
pass 1's own "decompiled but not yet confidently named" list per the
ticket's splitting-rule instructions, plus reclassified `0x8000046c` (was
tentatively named `usb_event_status_handler_dup` in pass 1) as **not a
real function** — it is 20 bytes of zero-filled alignment padding that
Ghidra's auto-analyzer mis-split into a stub; see
`rom/reverse_engineering_region_0x80000000.md`'s pass-2 section for the
raw-byte evidence. The 19 new rows are appended after the pass-1 rows
below.

**2026-06-22 update (pass 3, same-day continuation)**: resolved 74 more
functions in the same region (`0x80000000`-`0x8000ffff`), this time
cold-triaging the previously-untouched "gap B" stretch
(`~0x8000b068`-`0x8000ffff`) per the ticket's explicit instruction, plus
closing out 2 small clusters pass 2 had flagged but not finished (the
encryption-teardown/role-confirmation triplet and the `0x8000a4ac` pool-init
cluster). All 74 are **high confidence** (decompiled + written up) — see
`rom/reverse_engineering_region_0x80000000.md`'s pass-3 section for the
per-cluster evidence and full per-function table. The 74 new rows are
appended after the pass-2 rows below. 105 of the region's original 220
gap functions are now resolved (104 real + 1 reclassified non-function);
129 remain — see that doc's "Remaining scope" section for the specific
untouched sub-ranges flagged for the next pass.

**2026-06-22 update (pass 4, same-day continuation)**: resolved 27 more
functions in the same region (`0x80000000`-`0x8000ffff`) — the
`0x80001648`/`0x80001c4c` packet-type/role-switch supercluster (14
functions, including the long-flagged cluster heads
`apply_codec_type_and_role_switch_hook_dispatch` and
`role_switch_completion_or_abort_handler`) and the `0x8000c09c`-`0x8000c77c`
stretch plus immediate neighbors (13 functions, including
`afh_or_rf_divider_reconfig_orchestrator` and
`vsc_0xfc56_payload_apply_and_rf_reconfig`). This pass also reconciled a
tally-drift bug in passes 1-3's own running totals (root cause: 17 of the
claimed-105 "resolved" functions were leaf helpers OUTSIDE this region's
220-function scope, living in `0x80010000`+ regions or the excluded
interrupt-vector sub-range; separately the thin-named-open bucket was
undercounted by 2). Reconciled, verified-by-direct-address-membership
total: **115 of 220 in-scope gap functions resolved**, **102 remain** (86
still-unnamed + 16 genuinely-open thin-named; 2 more thin-named are already
"high confidence" via other docs and don't need further work). See
`rom/reverse_engineering_region_0x80000000.md`'s "Tally reconciliation
(pass 4)" and "Resolved functions — pass 4" sections for full detail. The
27 new rows are appended after the pass-3 rows below.

**2026-06-22 update (pass 5, same-day continuation)**: resolved 31 more
functions in the same region (`0x80000000`-`0x8000ffff`), cold-triaging the
large `0x80002488`-`0x80008f04` stretch flagged since pass 3 as the
highest-value untouched sub-range. Found this stretch was largely more
siblings of the packet-type/role-switch supercluster (8 functions,
including `status_word_multiflag_link_event_dispatcher`, the master
multi-flag dispatcher the `0x80001648`/`0x80001c4c` cluster funnels
alongside) and a connection-teardown/link-loss-cleanup cluster (4
functions, including `conn_teardown_and_link_loss_cleanup_handler`), plus a
batch-sweep-dispatcher quartet and the tiny `0x80008a7c`-`0x80008cd8`
feature-bit-registration cluster (8 functions). See
`rom/reverse_engineering_region_0x80000000.md`'s "Resolved functions — pass
5" section for full per-cluster evidence. Reconciled total: **146 of 220
in-scope gap functions resolved**, **71 remain** (55 still-unnamed + 16
genuinely-open thin-named; 2 more thin-named already "high confidence" via
other docs). The 31 new rows are appended after the pass-4 rows below.

Note: the 2026-06-21 `rom_coverage_baseline.md` run recorded `total=2738`;
this run recorded `total=2739` (461 named unchanged). The 1-function drift
is most likely an artifact of Ghidra's auto-analysis re-running incidental
function-boundary detection between headless invocations (e.g. a 1-byte
alignment stub at a data/code boundary), not a real change to the named
subset. Not investigated further — doesn't affect this doc's scope.

**Important caveat on "confidence" vs. "named"**: being in the 461-named set
and having a dedicated write-up are *not* the same thing. Cross-referencing
found **147 additional ROM addresses** that are discussed/decompiled in
Phase 1–8 `rom/*.md` docs (e.g. the hardware-layer slot-budget validator
`FUN_8004f824`, the VSC dispatcher `FUN_80047c50`, the register-script VM
`FUN_8003aea0`, the baseband-reg-helper family `FUN_80011510`+) but were
**never given a real Ghidra symbol name** — they're referenced by raw
address in prose, and remain `FUN_8000xxxx` in Ghidra. Those 147 functions
are **not** in the table below (out of scope — this doc's named-function
table is exactly the 461-function set from `RomNamedFuncAddrs.java`), but
they ARE part of the "well-understood" subset of the unnamed-function bucket
summarized in the second half of this doc. A future cleanup pass could
promote these 147 to real names in Ghidra; tracked informally, not a
separate TODO line item.

---

## Named functions (461)

Confidence legend:
- **high (decompiled+documented)** — appears in a dedicated `analysis/rom/*.md`
  write-up from Phases 1–9 (decompiled and explained in detail). The
  "Purpose" column links to the doc(s) by short name.
- **medium (named, one-line purpose only, not decompiled)** — either (a)
  discussed in passing in a root-level cross-cutting doc
  (`analysis/reverse_engineering_*.md`, e.g. the opcode map or hook-audit
  docs) without a dedicated decompile, or (b) present in
  `analysis/kovah_function_list.md`'s curated table with no further write-up.
  The function's own descriptive name is the only "purpose" documentation.
- **low (named by Kovah, purpose unclear)** — carries a real Ghidra
  symbol name (so it doesn't show up as `FUN_*`), but the name itself
  signals uncertainty (`unknown_*`, `possible_*`, trailing `?`, etc.), or
  the function has no doc cross-reference at all and its name is too terse
  to confirm purpose with confidence.

Purpose text is derived from the function's own name (Kovah's names are
themselves intended as a one-line purpose description); where a dedicated
ROM doc exists, that doc is linked instead of/in addition to the bare name.

| Address | Size | Name | Purpose | Confidence |
|---------|------|------|---------|------------|
| `0x800009c0` | 150 | `unknown_referencing_default_name_1` | unknown referencing default name 1 | low (named by Kovah, purpose unclear) |
| `0x80003d10` | 2044 | `func2_that_uses_structs_at_0x80100000` | func2 that uses structs at 0x80100000 | medium (named, one-line purpose only, not decompiled) |
| `0x80008d18` | 380 | `log_many_2_0x72_0x121-0x14e` | log many 2 0x72 0x121-0x14e | medium (named, one-line purpose only, not decompiled) |
| `0x80009104` | 28 | `disable_interrupts_(clear_LSBit_of_CP0_Status_Register)` | disable interrupts (clear LSBit of CP0 Status Register) — see `boot_reset_sequence`, `interrupt_vectors`, `usb_transport_hci_driver` | high (decompiled+documented) |
| `0x80009120` | 16 | `enable_interrupts_(set_CP0_Status_to_arg)` | enable interrupts (set CP0 Status to arg) — see `boot_reset_sequence`, `interrupt_vectors`, `usb_transport_hci_driver` | high (decompiled+documented) |
| `0x80009148` | 4 | `VSC_0xfc11_3_in_while_loop_FUN_80009148` | VSC 0xfc11 3 in while loop FUN 80009148 — see `interrupt_vectors` | high (decompiled+documented) |
| `0x800092e4` | 152 | `memcmp` | memcmp | medium (named, one-line purpose only, not decompiled) |
| `0x800093f8` | 20 | `wrap_set_two_global_ptrs` | wrap set two global ptrs | medium (named, one-line purpose only, not decompiled) |
| `0x80009414` | 24 | `called_by_function_that_uses_Logger_string_1` | called by function that uses Logger string 1 | low (named by Kovah, purpose unclear) |
| `0x800098d8` | 88 | `possible_logger_called_if_no_patch3` | possible logger called if no patch3 | low (named by Kovah, purpose unclear) |
| `0x80009990` | 110 | `interesting_string_user_fptr_registration_function` | interesting string user fptr registration function | medium (named, one-line purpose only, not decompiled) |
| `0x80009a30` | 56 | `LMP__25C_called1` | LMP  25C called1 | medium (named, one-line purpose only, not decompiled) |
| `0x80009a6c` | 88 | `LMP__268__most_common_for_VSCs2_checks_fptr_patch` | LMP  268  most common for VSCs2 checks fptr patch — see `conn_record_subsystem`, `vsc_dispatcher` | high (decompiled+documented) |
| `0x80009ac8` | 80 | `LMP__25B__most_common_for_VSCs1` | LMP  25B  most common for VSCs1 | medium (named, one-line purpose only, not decompiled) |
| `0x80009b1c` | 92 | `VSC_0xfc95_called2` | VSC 0xfc95 called2 | medium (named, one-line purpose only, not decompiled) |
| `0x80009b8c` | 84 | `wraps_uninteresting_if_0x80100000!=0_which_its_not_in_my_tests` | wraps uninteresting if 0x80100000!=0 which its not in my tests | low (named by Kovah, purpose unclear) |
| `0x80009be4` | 88 | `call_fptr_if_set_with_2_args_possibly_allocates_buf_at_arg2?` | call fptr if set with 2 args possibly allocates buf at arg2? | low (named by Kovah, purpose unclear) |
| `0x80009cc0` | 106 | `reg_multiple_dptrs?_FUN_80009cc0` | reg multiple dptrs? FUN 80009cc0 | low (named by Kovah, purpose unclear) |
| `0x80009f68` | 92 | `called_at_end_of_every_HCI_CMD_via_fptr` | called at end of every HCI CMD via fptr — see `usb_transport_hci_driver` | high (decompiled+documented) |
| `0x8000bd04` | 46 | `VSC_0xfc6c_FUN_8000bd04` | VSC 0xfc6c FUN 8000bd04 | medium (named, one-line purpose only, not decompiled) |
| `0x8000bdb4` | 12 | `VSC_common_used_in_0xfc39_FUN_8000bdb4` | VSC common used in 0xfc39 FUN 8000bdb4 | medium (named, one-line purpose only, not decompiled) |
| `0x8000be84` | 26 | `VSC_0xfc39_1_FUN_8000be84` | VSC 0xfc39 1 FUN 8000be84 | medium (named, one-line purpose only, not decompiled) |
| `0x8000c09c` | 178 | `unknown_referencing_default_name_3` | unknown referencing default name 3 | low (named by Kovah, purpose unclear) |
| `0x8000c198` | 370 | `unknown_referencing_default_name_4` | unknown referencing default name 4 | low (named by Kovah, purpose unclear) |
| `0x8000c390` | 86 | `unknown_referencing_default_name_5` | unknown referencing default name 5 | low (named by Kovah, purpose unclear) |
| `0x8000e85c` | 100 | `optimized_memcpy` | optimized memcpy — see `vsc_dispatcher` | high (decompiled+documented) |
| `0x8000e98c` | 64 | `memset` | memset | medium (named, one-line purpose only, not decompiled) |
| `0x8000e9cc` | 56 | `references_patch_download_mem2` | references patch download mem2 | medium (named, one-line purpose only, not decompiled) |
| `0x8000f0a4` | 806 | `called_by_unknown_fptr_index9_1` | called by unknown fptr index9 1 | low (named by Kovah, purpose unclear) |
| `0x8000f41c` | 108 | `unknown_fptr_index9` | unknown fptr index9 | low (named by Kovah, purpose unclear) |
| `0x8000f53c` | 64 | `wraps_multi_VSC_called_if_no_patch3` | wraps multi VSC called if no patch3 | medium (named, one-line purpose only, not decompiled) |
| `0x8000fae8` | 50 | `VSC_0xfc39_wrapper_FUN_8000fae8` | VSC 0xfc39 wrapper FUN 8000fae8 | medium (named, one-line purpose only, not decompiled) |
| `0x8000fb5c` | 436 | `lots_of_initialization` | lots of initialization — see `boot_reset_sequence`, `interrupt_vectors`, `usb_transport_hci_driver` | high (decompiled+documented) |
| `0x8000fd38` | 120 | `copies_config_bdaddr` | copies config bdaddr | medium (named, one-line purpose only, not decompiled) |
| `0x8000046c` | 20 | **NOT A REAL FUNCTION** (was tentatively `usb_event_status_handler_dup` in pass 1) | zero-filled alignment padding before `0x80000480`, mis-split by Ghidra's analyzer; reclassified 2026-06-22 pass 2 — see `region_0x80000000` | n/a (non-function, excluded from named/unnamed counts) |
| `0x80000480` | 764 | `usb_event_status_handler` | USB-class event-status bit dispatcher, ends in USB-transport-drain calls — see `region_0x80000000`, `usb_transport_hci_driver` | high (decompiled+documented) |
| `0x80000a5c` | 410 | `connection_event_status_handler` | per-connection HW status-change handler (link loss/role/feature gating) — see `region_0x80000000` | high (decompiled+documented) |
| `0x80000c24` | 64 | `timer_callback_table_dispatcher_4entry` | fixed 4-entry callback-table dispatcher over a status byte — see `region_0x80000000` | high (decompiled+documented) |
| `0x80000c74` | 190 | `uart_rx_tx_byte_fifo_handler` | UART-style RX/TX circular-byte-FIFO ISR handler — see `region_0x80000000` | high (decompiled+documented) |
| `0x80000d78` | 492 | `isr_bottom_half_status_dispatcher` | ISR bottom-half/deferred-event dispatcher, reads CP0 Cause/EPC then branches to ~8 status-bit handlers — see `region_0x80000000`, `interrupt_vectors` | high (decompiled+documented) |
| `0x800011fc` | 188 | `conn_record_pending_data_drain` | linked-list byte-stream dequeue primitive over conn-record buffer pool — see `region_0x80000000`, `conn_record_subsystem` | high (decompiled+documented) |
| `0x800012b8` | 216 | `baseband_event_status_dispatcher_0xd` | 13-source baseband event dispatcher, parallel in shape to `FUN_80050810` — see `region_0x80000000`, `conn_type_dispatch_and_esco` | high (decompiled+documented) |
| `0x80009130` | 12 | `get_CP0_Cause_register` | `return Cause;` — CP0 interrupt-cause accessor — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000913c` | 12 | `get_CP0_EPC_register` | `return EPC;` — CP0 exception-PC accessor — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000a780` | 318 | `find_pool_index_by_addr_and_mark_dirty` | resolves an address to a record-pool index across 4 fixed-size pools, disables IRQs, sets a dirty bit, re-enables IRQs — see `region_0x80000000`, `conn_record_subsystem` | high (decompiled+documented) |
| `0x8000a8e8` | 308 | `conn_record_periodic_sweep_and_clear_dirty` | periodic 17-entry sweep clearing dirty/in-use flags, pairs with `find_pool_index_by_addr_and_mark_dirty` — see `region_0x80000000`, `conn_record_subsystem` | high (decompiled+documented) |
| `0x8001483c` | 104 | `set_bos_e4_role_switch_hook_bit` | sets per-connection-index bit in the `bos_base+0xe4` hw-hook bitmask — see `region_0x80000000` | high (decompiled+documented) |
| `0x80014d50` | 84 | `clear_bos_e4_role_switch_hook_bit` | clears per-connection-index bit at `bos_base+0xe4`, pairs with `set_bos_e4_role_switch_hook_bit` — see `region_0x80000000` | high (decompiled+documented) |
| `0x80060708` | 50 | `lookup_codec_or_role_type_table_7x4` | 7×4 byte-pair table lookup by role index + sub-index — see `region_0x80000000` | high (decompiled+documented) |
| `0x80042db8` | 42 | `remap_role_index_to_esco_slot_if_pending` | remaps SCO index to `idx+8` eSCO range if pending-flag set — see `region_0x80000000` | high (decompiled+documented) |
| `0x80042de8` | 36 | `remap_role_index_to_esco_slot_unconditional` | unconditional `idx+8` SCO→eSCO index remap — see `region_0x80000000` | high (decompiled+documented) |
| `0x800147b0` | 140 | `write_baseband_codec_param_triplet` | writes a 3-row codec config block for a connection via indexed register writes — see `region_0x80000000` | high (decompiled+documented) |
| `0x80035068` | 138 | `lmp_25c_procedure_completion_waiter` | 2-state synchronous busy-wait barrier for an in-flight LMP procedure — see `region_0x80000000` | high (decompiled+documented) |
| `0x80042a68` | 62 | `conn_record_role_to_esco_slot_index` | connection-record accessor returning current credit/codec slot index — see `region_0x80000000` | high (decompiled+documented) |
| `0x80000fb8` | 550 | `sco_esco_packet_credit_scheduler` | per-connection SCO/eSCO outbound packet-credit scheduler, builds HW packet-length descriptor table — see `region_0x80000000`, `conn_feature_dispatch` | high (decompiled+documented) |
| `0x8002addc` | 52 | `lookup_conn_record_by_lt_addr` | fixed 3-slot connection lookup by LT_ADDR-like parameter — see `region_0x80000000` | high (decompiled+documented) |
| `0x8002bc88` | 116 | `alloc_credit_scheduler_slot_0xd` | 13-slot (`0xd`) bitmask allocator for the credit-scheduler table — see `region_0x80000000` | high (decompiled+documented) |
| `0x8003d018` | 26 | `wrapping_subtract_masked_by_shift` | generic modular/wrapping subtract-and-mask primitive — see `region_0x80000000` | high (decompiled+documented) |
| `0x800093d0` | 14 | `set_global_status_bit_0x400` | `*global \|= 0x400` — see `region_0x80000000` | high (decompiled+documented) |
| `0x800093e4` | 16 | `clear_global_status_bit_0x400` | `*global &= ~0x400` — see `region_0x80000000` | high (decompiled+documented) |
| `0x80013dc4` | 96 | `write_codec_table_entry_and_wait_ack` | indexed codec/parameter table row write + hardware-ack poll — see `region_0x80000000` | high (decompiled+documented) |
| `0x80014c58` | 152 | `clear_codec_table_entries_for_role` | zeroes a connection's codec-table rows (teardown counterpart of `write_baseband_codec_param_triplet`) — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000b820` | 56 | `program_rf_freq_reg_and_start_poll` | programs a 10-bit RF freq/channel word + kicks off lock-wait poll — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000b864` | 26 | `poll_status_sign_bit_with_timeout_0x65` | bounded (101-iteration) hardware-ready poll on a status byte's sign bit — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000b858` | 12 | `poll_status_sign_bit_with_timeout_0x65_variant` | near-duplicate of `poll_status_sign_bit_with_timeout_0x65`, not merged — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000f624` | 4 | `trivial_jr_ra_stub` | degenerate 1-instruction (`jr ra`) real function, confirmed not padding — see `region_0x80000000` | high (decompiled+documented) |
| `0x80002974` | 246 | `encryption_key_teardown_notifier` | clears a connection's crypto key-valid flag on teardown, logs around it — see `region_0x80000000` | high (decompiled+documented) |
| `0x80002a8c` | 198 | `conditional_debug_logger_0x2be` | feature-bit-gated conditional debug-event logger, no other side effects — see `region_0x80000000` | high (decompiled+documented) |
| `0x80002b60` | 318 | `role_switch_confirmation_matcher` | role-switch confirmation/ack byte matcher against a small per-connection table — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000a4ac` | 62 | `conn_pool_quick_reset_and_log` | lightweight conn-record pool-state reset/log helper — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000a4f8` | 98 | `pool_row_register_and_optional_callback_copy` | carves N fixed-size rows out of a pool, optional per-row callback fill — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000a570` | 454 | `conn_record_7pool_init_and_clear_dirty` | 7-pool registration + zero-init/dirty-flag for the last 3 (17/17/9-entry) pools — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000b068` | 180 | `usb_or_conn_status_dispatch_loop_variant1` | bit-drain status dispatcher sibling of `usb_event_status_handler` — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000b138` | 192 | `usb_or_conn_status_dispatch_loop_variant2` | same bit-drain dispatch pattern + record-pointer binding step — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000b218` | 266 | `feature_bit_status_reconfig_handler` | feature/role-driven HW status-register reconfiguration — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000b348` | 1026 | `baseband_feature_pool_init_and_reset` | top-level baseband feature/pool (re)initialization entry point — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000b890` | 570 | `config_table_byte_stream_loader` | loads a config-table byte stream into the BD_ADDR-adjacent config table — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000bae0` | 98 | `rf_calibration_kickoff_and_poll` | RF calibration start-and-poll sequencer — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000bb58` | 50 | `rf_calibration_status_check_or_kickoff` | calibration status check / kickoff decision pair with the function above — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000bb94` | 66 | `baseband_reg_0x34_init_and_latch` | one-time baseband register-0x34 init/latch routine — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000bbe0` | 82 | `baseband_reg_0x34_role_index_setter` | register-0x34 role-index write half, pairs with the function above — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000bc38` | 188 | `indexed_register_rw_poll_primitive` | general indexed-register write/poll-ack/read-back primitive — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000bd34` | 32 | `indexed_register_write_1byte_wrapper` | thin 1-byte wrapper around `indexed_register_rw_poll_primitive` — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000bd54` | 14 | `check_status_bit_0x11_of_global` | `(*global>>0x11)&1` single-bit accessor — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000bd68` | 12 | `check_status_bit_0x2_of_global` | `(*global>>2)&1` single-bit accessor — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000bd78` | 48 | `link_active_or_config_flag_check` | link-active-or-config-override status check — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000bdc4` | 10 | `or_bits_into_global_flag_word` | `*global \|= param_1` trivial OR helper — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000bdd4` | 72 | `scrambled_bdaddr_field_writer_pair1` | XOR-merge bit-scrambled field writer, half of a pair — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000be28` | 16 | `check_bit_in_shifted_global_field` | indexed single-bit check into a shifted field — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000be3c` | 62 | `scrambled_bdaddr_field_writer_pair2` | complementary half of the scrambled-field writer pair — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000beac` | 72 | `poll_status_and_invoke_optional_fptr` | consume-pending-status with optional hook veto pattern — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000bf04` | 60 | `set_bit_in_8entry_bitmask_pair_v1` | IRQ-safe 8-entry bitmask set/copy pair — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000bf48` | 84 | `set_bit_in_8entry_bitmask_pair_v2` | IRQ-safe 8-entry bitmask set into two independent globals — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000bfa4` | 84 | `toggle_bit_in_8entry_bitmask_pair` | clear-side sibling of `set_bit_in_8entry_bitmask_pair_v2` — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000c000` | 142 | `irq_safe_4state_flag_setter` | IRQ-safe range-checked 4-entry set/clear/toggle flag setter — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000c7cc` | 116 | `sco_esco_timing_ratio_calculator` | SCO/eSCO timing-interval/ratio calculation primitive — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000c848` | 30 | `set_sign_bit_from_bool_param` | sets/clears bit 31 from a boolean param — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000c870` | 34 | `set_bit15_from_bool_param` | sets/clears bit 15 from a boolean param — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000c898` | 22 | `set_bits24_29_from_param` | writes a 6-bit field at bit-offset 24 — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000c8b8` | 92 | `program_3field_indexed_register` | multi-field (8/4/11-bit) indexed register program routine — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000c92c` | 14 | `check_inverted_bit4_of_global` | inverted single-bit check on bit 4 — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000c940` | 30 | `toggle_bit5_of_global_v1` | boolean-driven set/clear of bit 5 — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000c964` | 30 | `toggle_bit1_of_global_inverted` | boolean-driven set/clear of bit 1, inverted-logic sibling — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000c988` | 30 | `toggle_bit2_of_global` | boolean-driven set/clear of bit 2 — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000c9ac` | 32 | `toggle_bit7_of_global` | boolean-driven set/clear of bit 7 — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000c9d0` | 22 | `set_bit7_from_lsb_param` | direct (non-branching) bit-7 set from parameter LSB — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000c9ec` | 36 | `conditional_field_extract_into_global` | conditional 10-bit field-merge from a status word — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000ca1c` | 164 | `scrambled_status_field_unpacker` | status-register bit-descrambling routine — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000cad4` | 238 | `adaptive_threshold_register_programmer` | RF gain/threshold auto-adjustment register programmer — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000cbd8` | 74 | `role_dependent_bit_toggle_pair` | role-dependent multi-bit register toggle pair — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000cc34` | 28 | `set_bit1_from_lsb_param` | direct bit-1 set from parameter LSB — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000cc54` | 18 | `masked_or_into_global` | generic masked-OR merge into a global — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000cc74` | 10 | `masked_global_read` | generic masked read of a global — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000cc88` | 238 | `conn_state2_role_dependent_hw_reconfig` | connection-role-dependent HW-status reconfiguration — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000cd90` | 188 | `conn_state2_codec_or_role_field_programmer` | codec/role-field programmer, sibling of the function above — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000ce78` | 26 | `load_u32_little_endian_from_bytes` | trivial little-endian 4-byte load helper — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000cec4` | 84 | `link_state6_clear_bit15_if_feature_set` | link-state-6-gated register bit-15 clear — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000cf28` | 90 | `link_state6_set_bit15_and_mark_done` | link-state-6-gated register bit-15 set + done-flag — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000cf94` | 44 | `link_state6_set_bit3_if_active` | link-state-6-gated register bit-3 set if active — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000d01c` | 14 | `get_2bit_link_state_field` | `(*global>>0x16)&3` 2-bit link-state field accessor — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000d030` | 244 | `rf_divider_ratio_search_and_program` | RF frequency-synthesizer N-divider search-and-program routine — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000d138` | 222 | `rf_divider_ratio_table_lookup_or_search` | divider table-lookup-first, search-as-fallback entry point — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000d228` | 142 | `link_state6_afh_or_channel_feature_toggle1` | state-6-gated AFH/channel-feature toggle, 3-byte-match variant — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000d2d0` | 108 | `link_state6_afh_or_channel_feature_toggle2` | state-6-gated AFH/channel-feature toggle, register-bit variant — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000d3c0` | 138 | `link_state6_afh_or_channel_feature_toggle3` | state-6-gated AFH/channel-feature toggle, 2-bit-field variant — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000d460` | 98 | `afh_feature_toggle_dispatcher` | unifying dispatcher over the 3 AFH/channel-feature toggle siblings — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000d4d4` | 74 | `afh_feature_toggle_autotrigger` | automatic/conditional trigger wrapping the toggle dispatcher — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000d5fc` | 292 | `program_packet_type_and_timing_ratio` | central packet-type-and-timing programming routine — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000d750` | 56 | `read_current_packet_type_word` | packet-type accessor/refresh counterpart of the programmer above — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000d790` | 140 | `packet_type_change_and_threshold_update` | ties packet-type change to RF-threshold recompute — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000d83c` | 98 | `vsc_0xfc17_packet_type_change_handler` | confirmed VSC handler for opcode 0xfc17 (change packet type) — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000d8b0` | 62 | `program_default_packet_type_and_status` | default/reset-state packet-type programming helper — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000dcd4` | 40 | `trigger_callback_on_high_retry_count` | escalating-backoff-style callback trigger on retry count — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000ded8` | 158 | `link_state6_quality_recovery_poll_loop` | eSCO quality-recovery polling loop — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000df94` | 90 | `check_link_state3_and_set_recovery_flags` | gate-and-flag-set primitive for eSCO interval recompute — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000e004` | 122 | `recompute_and_apply_esco_interval` | eSCO retransmission-interval recompute-and-apply routine — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000e088` | 68 | `link_state6_quality_bit7_recovery_trigger` | trigger/entry-point wrapping the interval-recompute function — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000e0dc` | 178 | `program_rf_freq_word_from_status_or_table` | RF frequency-word programming, table-or-live-status source — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000e380` | 222 | `vsc_0xfc56_set_3word_params_and_packet_type` | confirmed VSC handler for opcode 0xfc56 (set 3 param words) — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000e470` | 68 | `build_and_send_default_status_report` | generic default-status-report builder/sender — see `region_0x80000000` | high (decompiled+documented) |
| `0x800109ac` | 916 | `calls_to_0x8010a001_as_fptr_to_install_patches` | calls to 0x8010a001 as fptr to install patches — see `boot_reset_sequence`, `interrupt_vectors`, `usb_transport_hci_driver` | high (decompiled+documented) |
| `0x80011a18` | 30 | `called_if_config[0xf2]&4` | called if config[0xf2]&4 | low (named by Kovah, purpose unclear) |
| `0x80011d9c` | 100 | `LMP_CH__0x3ee__case2_else_2_FUN_80011d9c` | LMP CH  0x3ee  case2 else 2 FUN 80011d9c | medium (named, one-line purpose only, not decompiled) |
| `0x80011e10` | 418 | `LMP_CH__0x3ee__case2_else_1_FUN_80011fc0FUN_80011e10` | LMP CH  0x3ee  case2 else 1 FUN 80011fc0FUN 80011e10 | medium (named, one-line purpose only, not decompiled) |
| `0x80011fc0` | 214 | `LMP_CH__0x3ee__case1_if_FUN_80011fc0` | LMP CH  0x3ee  case1 if FUN 80011fc0 | medium (named, one-line purpose only, not decompiled) |
| `0x800120ac` | 50 | `VSC_0xfc11_2_FUN_800120ac` | VSC 0xfc11 2 FUN 800120ac | medium (named, one-line purpose only, not decompiled) |
| `0x80012658` | 406 | `unknown_referencing_default_name_6` | unknown referencing default name 6 | low (named by Kovah, purpose unclear) |
| `0x80012c18` | 164 | `VSC_0xfc11_1_FUN_80012c18` | VSC 0xfc11 1 FUN 80012c18 | medium (named, one-line purpose only, not decompiled) |
| `0x80012e04` | 52 | `called_if_config[1]&4` | called if config[1]&4 | low (named by Kovah, purpose unclear) |
| `0x80013074` | 144 | `VSC_0xfc39_2_FUN_80013074` | VSC 0xfc39 2 FUN 80013074 | medium (named, one-line purpose only, not decompiled) |
| `0x8001343c` | 40 | `second_set_func_in_set_two_global_ptrs` | second set func in set two global ptrs | medium (named, one-line purpose only, not decompiled) |
| `0x80013474` | 4 | `return_1` | return 1 | medium (named, one-line purpose only, not decompiled) |
| `0x8001347c` | 162 | `unknown_referencing_default_name_7` | unknown referencing default name 7 | low (named by Kovah, purpose unclear) |
| `0x800138cc` | 680 | `unknown_fptr_index0` | unknown fptr index0 | medium (named, one-line purpose only, not decompiled) |
| `0x80014054` | 62 | `VSC_0xfcc0_FUN_80014054` | VSC 0xfcc0 FUN 80014054 | medium (named, one-line purpose only, not decompiled) |
| `0x80014180` | 58 | `called_on_every_HCI_CMD_via_fptr` | called on every HCI CMD via fptr — see `usb_transport_hci_driver` | high (decompiled+documented) |
| `0x800148f0` | 54 | `VSC_0xfcc2_FUN_800148f0` | VSC 0xfcc2 FUN 800148f0 | medium (named, one-line purpose only, not decompiled) |
| `0x80014a44` | 224 | `sometimes_called_with_0_3_0` | sometimes called with 0 3 0 | low (named by Kovah, purpose unclear) |
| `0x80014cf4` | 76 | `call_to_multi_VSC_e.g._0xfcc4_unknown` | call to multi VSC e.g. 0xfcc4 unknown | low (named by Kovah, purpose unclear) |
| `0x8001574c` | 100 | `send_evt_invalid_0xFF` | send evt invalid 0xFF | medium (named, one-line purpose only, not decompiled) |
| `0x800157b8` | 234 | `calls_send_evt_invalid_0xFF_0_or_1` | calls send evt invalid 0xFF 0 or 1 | medium (named, one-line purpose only, not decompiled) |
| `0x80016780` | 74 | `wrap_look_for_non_matching_bdaddr_bos_index_i.e._free_connection_slot` | wrap look for non matching bdaddr bos index i.e. free connection slot | medium (named, one-line purpose only, not decompiled) |
| `0x8001728c` | 38 | `wraps_calls_to_VSC_0xfcc0_then_calls_fptr` | wraps calls to VSC 0xfcc0 then calls fptr | low (named by Kovah, purpose unclear) |
| `0x80018c14` | 4 | `ret_wrapper` | ret wrapper | medium (named, one-line purpose only, not decompiled) |
| `0x80018d44` | 132 | `HCI_Setup_Synchronous_Connection_LMP_Feature_Checker` | HCI Setup Synchronous Connection LMP Feature Checker | low (named by Kovah, purpose unclear) |
| `0x80018e58` | 220 | `send_HCI_Command_Status_for_HCI_0x0A` | send HCI Command Status for HCI 0x0A | medium (named, one-line purpose only, not decompiled) |
| `0x80019594` | 370 | `send_HCI_Command_Status_for_HCI_0x09` | send HCI Command Status for HCI 0x09 | medium (named, one-line purpose only, not decompiled) |
| `0x80019830` | 638 | `send_HCI_Command_Status_for_HCI_0x07` | send HCI Command Status for HCI 0x07 | medium (named, one-line purpose only, not decompiled) |
| `0x80019ad0` | 172 | `OGC_3_OCF_3f` | OGC 3 OCF 3f | medium (named, one-line purpose only, not decompiled) |
| `0x80019b88` | 32 | `OGC_3_OCF_49` | OGC 3 OCF 49 | medium (named, one-line purpose only, not decompiled) |
| `0x80019bac` | 32 | `OGC_3_OCF_45` | OGC 3 OCF 45 | medium (named, one-line purpose only, not decompiled) |
| `0x80019bd0` | 32 | `OGC_3_OCF_47` | OGC 3 OCF 47 | medium (named, one-line purpose only, not decompiled) |
| `0x80019bf4` | 116 | `OGC_3_default_func_0_OCF_0x3F_and_above` | OGC 3 default func 0 OCF 0x3F and above | medium (named, one-line purpose only, not decompiled) |
| `0x80019c88` | 104 | `deal_with_OGF_3_OCF_0x3f-0x49` | deal with OGF 3 OCF 0x3f-0x49 | medium (named, one-line purpose only, not decompiled) |
| `0x80019d80` | 202 | `fHCI_Setup_Synchronous_Connection?` | fHCI Setup Synchronous Connection? | low (named by Kovah, purpose unclear) |
| `0x80019e4c` | 60 | `send_evt_HCI_Read_Remote_Extended_Features_Complete` | send evt HCI Read Remote Extended Features Complete | medium (named, one-line purpose only, not decompiled) |
| `0x80019e88` | 124 | `send_evt_HCI_Synchronous_Connection_Changed` | send evt HCI Synchronous Connection Changed | medium (named, one-line purpose only, not decompiled) |
| `0x80019f0c` | 232 | `send_evt_HCI_Synchronous_Connection_Complete` | send evt HCI Synchronous Connection Complete | medium (named, one-line purpose only, not decompiled) |
| `0x8001a0f8` | 44 | `calls_to_VSC_0xfcc0` | calls to VSC 0xfcc0 | low (named by Kovah, purpose unclear) |
| `0x8001a294` | 174 | `OGC_3_OCF_0x52_HCI_Write_Extended_Inquiry_Response_fills_0x300_then_calls_to_VSC_0xfcc0` | OGC 3 OCF 0x52 HCI Write Extended Inquiry Response fills 0x300 then calls to VSC 0xfcc0 | medium (named, one-line purpose only, not decompiled) |
| `0x8001a350` | 164 | `OGC_3_OCF_0x51_and_above_path_to_VSC_0xfcc0` | OGC 3 OCF 0x51 and above path to VSC 0xfcc0 | low (named by Kovah, purpose unclear) |
| `0x8001a420` | 56 | `send_evt_HCI_Remote_Host_Supported_Features_Notification` | send evt HCI Remote Host Supported Features Notification | low (named by Kovah, purpose unclear) |
| `0x8001a458` | 34 | `send_evt_HCI_Enhanced_Flush_Complete` | send evt HCI Enhanced Flush Complete | low (named by Kovah, purpose unclear) |
| `0x8001a47c` | 42 | `send_evt_HCI_Link_Supervision_Timeout_Changed` | send evt HCI Link Supervision Timeout Changed | low (named by Kovah, purpose unclear) |
| `0x8001a4a8` | 268 | `send_evt_HCI_Sniff_Subrating` | send evt HCI Sniff Subrating | low (named by Kovah, purpose unclear) |
| `0x8001a5b8` | 158 | `send_evt_HCI_Extended_Inquiry_Result` | send evt HCI Extended Inquiry Result | low (named by Kovah, purpose unclear) |
| `0x8001a898` | 66 | `OGC_3_default_func_2` | OGC 3 default func 2 | low (named by Kovah, purpose unclear) |
| `0x8001a9b8` | 52 | `bit_test_[bit_index_at_offset_0x16f]_within_[short_at_offset_0x24]` | bit test [bit index at offset 0x16f] within [short at offset 0x24] | low (named by Kovah, purpose unclear) |
| `0x8001aa3c` | 254 | `LMP_QUALITY_OF_SERVICE_REQ_0x2A` | LMP QUALITY OF SERVICE REQ 0x2A — see `lc_lmp_state_machine` | high (decompiled+documented) |
| `0x8001af9c` | 114 | `LMP_0x18_LMP_UNSNIFF_REQ` | LMP 0x18 LMP UNSNIFF REQ — see `lc_lmp_state_machine` | high (decompiled+documented) |
| `0x8001b23c` | 122 | `fHCI_Read_LMP_Handle_0x20` | fHCI Read LMP Handle 0x20 | medium (named, one-line purpose only, not decompiled) |
| `0x8001b2c0` | 170 | `fHCI_Read_Clock_Offset_0x1F` | fHCI Read Clock Offset 0x1F | medium (named, one-line purpose only, not decompiled) |
| `0x8001b370` | 354 | `fHCI_Read_Remote_Version_Information_0x1D_send_LMP_VERSION_REQ_0x25` | fHCI Read Remote Version Information 0x1D send LMP VERSION REQ 0x25 | medium (named, one-line purpose only, not decompiled) |
| `0x8001b4e8` | 96 | `fHCI_Read_Remote_Supported_Features_0x1B` | fHCI Read Remote Supported Features 0x1B | medium (named, one-line purpose only, not decompiled) |
| `0x8001b54c` | 496 | `fHCI_Remote_Name_Request_0x19_send_LMP_NAME_REQ_0x01` | fHCI Remote Name Request 0x19 send LMP NAME REQ 0x01 | medium (named, one-line purpose only, not decompiled) |
| `0x8001b84c` | 170 | `fHCI_Change_Connection_Packet_Type_0x0F` | fHCI Change Connection Packet Type 0x0F | medium (named, one-line purpose only, not decompiled) |
| `0x8001b8fc` | 204 | `fHCI_Add_SCO_Connection_DEPRECATED_0x07` | fHCI Add SCO Connection DEPRECATED 0x07 | medium (named, one-line purpose only, not decompiled) |
| `0x8001b9d4` | 258 | `fHCI_Disconnect_0x06` | fHCI Disconnect 0x06 | medium (named, one-line purpose only, not decompiled) |
| `0x8001baf8` | 190 | `fHCI_Reject_Connection_Request_0x0A` | fHCI Reject Connection Request 0x0A | medium (named, one-line purpose only, not decompiled) |
| `0x8001bbbc` | 360 | `fHCI_Accept_Connection_Request_0x09` | fHCI Accept Connection Request 0x09 | medium (named, one-line purpose only, not decompiled) |
| `0x8001bd38` | 512 | `fHCI_Create_Connection_0x05` | fHCI Create Connection 0x05 — see `lc_lmp_state_machine` | high (decompiled+documented) |
| `0x8001bf44` | 88 | `fHCI_Periodic_Inquiry_Mode_0x03` | fHCI Periodic Inquiry Mode 0x03 | medium (named, one-line purpose only, not decompiled) |
| `0x8001bfa0` | 50 | `fHCI_Inquiry_0x01` | fHCI Inquiry 0x01 — see `lc_lmp_state_machine` | high (decompiled+documented) |
| `0x8001c324` | 252 | `set_check_for_1_to_1` | set check for 1 to 1 | low (named by Kovah, purpose unclear) |
| `0x8001c438` | 76 | `OGC_3_default_func_4` | OGC 3 default func 4 | low (named by Kovah, purpose unclear) |
| `0x8001c490` | 186 | `HCI_OGF1_OCF0x44` | HCI OGF1 OCF0x44 | low (named by Kovah, purpose unclear) |
| `0x8001c550` | 32 | `HCI_OGF1_OCF0x43` | HCI OGF1 OCF0x43 | low (named by Kovah, purpose unclear) |
| `0x8001c574` | 304 | `HCI_OGF1_OCF0x42` | HCI OGF1 OCF0x42 | low (named by Kovah, purpose unclear) |
| `0x8001c6b8` | 204 | `HCI_OGF1_OCF0x41` | HCI OGF1 OCF0x41 | low (named by Kovah, purpose unclear) |
| `0x8001c788` | 38 | `fHCI_Truncated_Page_Cancel_0x40` | fHCI Truncated Page Cancel 0x40 | medium (named, one-line purpose only, not decompiled) |
| `0x8001c7b4` | 382 | `fHCI_Truncated_Page_0x3F` | fHCI Truncated Page 0x3F | medium (named, one-line purpose only, not decompiled) |
| `0x8001c940` | 132 | `call_to_HCI_opcodes_OGF=1_0x3F-to-0x44` | call to HCI opcodes OGF=1 0x3F-to-0x44 | low (named by Kovah, purpose unclear) |
| `0x8001ca94` | 60 | `send_evt_HCI_Inquiry_Response_Notification` | send evt HCI Inquiry Response Notification | low (named by Kovah, purpose unclear) |
| `0x8001cad4` | 36 | `send_evt_HCI_Connectionless_Peripheral_Broadcast_Channel_Map_Change` | send evt HCI Connectionless Peripheral Broadcast Channel Map Change | low (named by Kovah, purpose unclear) |
| `0x8001cafc` | 20 | `send_evt_HCI_Peripheral_Page_Response_Timeout` | send evt HCI Peripheral Page Response Timeout | low (named by Kovah, purpose unclear) |
| `0x8001cb10` | 74 | `send_evt_HCI_Connectionless_Peripheral_Broadcast_Timeout` | send evt HCI Connectionless Peripheral Broadcast Timeout | low (named by Kovah, purpose unclear) |
| `0x8001cb68` | 24 | `send_evt_HCI_Synchronization_Train_Complete` | send evt HCI Synchronization Train Complete | low (named by Kovah, purpose unclear) |
| `0x8001cb80` | 70 | `send_evt_HCI_Triggered_Clock_Capture` | send evt HCI Triggered Clock Capture | low (named by Kovah, purpose unclear) |
| `0x8001cbcc` | 52 | `send_evt_HCI_Truncated_Page_Complete` | send evt HCI Truncated Page Complete | low (named by Kovah, purpose unclear) |
| `0x8001cc04` | 110 | `send_evt_HCI_Synchronization_Train_Received` | send evt HCI Synchronization Train Received | low (named by Kovah, purpose unclear) |
| `0x8001cc80` | 226 | `send_evt_HCI_Connectionless_Peripheral_Broadcast_Receive` | send evt HCI Connectionless Peripheral Broadcast Receive | low (named by Kovah, purpose unclear) |
| `0x8001cd74` | 586 | `initialize_0x28_sized_struct` | initialize 0x28 sized struct | medium (named, one-line purpose only, not decompiled) |
| `0x8001d070` | 310 | `hci_event_sender` | hci event sender — see `hci_command_router`, `usb_transport_hci_driver`, `vsc_dispatcher` | high (decompiled+documented) |
| `0x8001d1bc` | 24 | `send_evt_HCI_Hardware_Error` | send evt HCI Hardware Error | medium (named, one-line purpose only, not decompiled) |
| `0x8001d1d4` | 34 | `send_evt_HCI_Flush_Occurred` | send evt HCI Flush Occurred | low (named by Kovah, purpose unclear) |
| `0x8001d1f8` | 74 | `send_evt_HCI_Connection_Complete` | send evt HCI Connection Complete | medium (named, one-line purpose only, not decompiled) |
| `0x8001d244` | 58 | `send_evt_HCI_Loopback_Command` | send evt HCI Loopback Command | low (named by Kovah, purpose unclear) |
| `0x8001d280` | 130 | `send_evt_0x21_HCI_Flow_Specification_Complete` | send evt 0x21 HCI Flow Specification Complete | low (named by Kovah, purpose unclear) |
| `0x8001d308` | 122 | `send_evt_0x0D_HCI_QoS_Setup_Complete` | send evt 0x0D HCI QoS Setup Complete | low (named by Kovah, purpose unclear) |
| `0x8001d388` | 70 | `send_evt_HCI_Role_Change` | send evt HCI Role Change | low (named by Kovah, purpose unclear) |
| `0x8001d3d4` | 36 | `send_evt_HCI_Max_Slots_Change` | send evt HCI Max Slots Change | low (named by Kovah, purpose unclear) |
| `0x8001d3f8` | 44 | `send_event_HCI_Connection_Packet_Type_Changed` | send event HCI Connection Packet Type Changed | low (named by Kovah, purpose unclear) |
| `0x8001d424` | 76 | `called_by_fHCI_Read_LMP_Handle_send_evt_HCI_Command_Complete` | called by fHCI Read LMP Handle send evt HCI Command Complete | medium (named, one-line purpose only, not decompiled) |
| `0x8001d474` | 44 | `send_evt_HCI_Read_Clock_Offset_Complete` | send evt HCI Read Clock Offset Complete | low (named by Kovah, purpose unclear) |
| `0x8001d4a0` | 134 | `send_evt_HCI_Read_Remote_Version_Information_Complete` | send evt HCI Read Remote Version Information Complete — see `lmp_version_conn_setup` | high (decompiled+documented) |
| `0x8001d534` | 48 | `send_evt_HCI_Read_Remote_Supported_Features_Complete` | send evt HCI Read Remote Supported Features Complete | low (named by Kovah, purpose unclear) |
| `0x8001d564` | 74 | `send_evt_HCI_Remote_Name_Request_Complete` | send evt HCI Remote Name Request Complete | low (named by Kovah, purpose unclear) |
| `0x8001d5b4` | 68 | `send_evt_0x14_HCI_Mode_Change` | send evt 0x14 HCI Mode Change | medium (named, one-line purpose only, not decompiled) |
| `0x8001d5fc` | 460 | `send_evt_HCI_Disconnection_Complete` | send evt HCI Disconnection Complete | medium (named, one-line purpose only, not decompiled) |
| `0x8001d804` | 64 | `send_evt_HCI_Connection_Request` | send evt HCI Connection Request | medium (named, one-line purpose only, not decompiled) |
| `0x8001d844` | 178 | `send_evt_HCI_Connection_Complete` | send evt HCI Connection Complete | low (named by Kovah, purpose unclear) |
| `0x8001d904` | 256 | `send_evt_HCI_Inquiry_Result_or_HCI_Inquiry_Result_with_RSSI` | send evt HCI Inquiry Result or HCI Inquiry Result with RSSI | low (named by Kovah, purpose unclear) |
| `0x8001da0c` | 40 | `send_evt_HCI_Inquiry_Complete` | send evt HCI Inquiry Complete | medium (named, one-line purpose only, not decompiled) |
| `0x8001da3c` | 438 | `send_evt_HCI_Number_Of_Completed_Packets` | send evt HCI Number Of Completed Packets | low (named by Kovah, purpose unclear) |
| `0x8001dc10` | 2454 | `OGC_3_OCF_TONS_deal_with_return_status_referencing_default_name_10` | OGC 3 OCF TONS deal with return status referencing default name 10 | low (named by Kovah, purpose unclear) |
| `0x8001e5d8` | 52 | `send_evt_HCI_Command_Status` | send evt HCI Command Status | medium (named, one-line purpose only, not decompiled) |
| `0x8001e610` | 52 | `OGC_3_OCF_01` | OGC 3 OCF 01 | medium (named, one-line purpose only, not decompiled) |
| `0x8001e648` | 48 | `OGC_3_OCF_33` | OGC 3 OCF 33 | low (named by Kovah, purpose unclear) |
| `0x8001e67c` | 12 | `OGC_3_OCF_2a` | OGC 3 OCF 2a | low (named by Kovah, purpose unclear) |
| `0x8001e68c` | 20 | `OGC_3_OCF_2f` | OGC 3 OCF 2f | low (named by Kovah, purpose unclear) |
| `0x8001e6a4` | 20 | `OGC_3_OCF_31` | OGC 3 OCF 31 | low (named by Kovah, purpose unclear) |
| `0x8001e6bc` | 56 | `OGC_3_OCF_13_referencing_default_name` | OGC 3 OCF 13 referencing default name | medium (named, one-line purpose only, not decompiled) |
| `0x8001e6fc` | 44 | `OGC_3_OCF_16` | OGC 3 OCF 16 | medium (named, one-line purpose only, not decompiled) |
| `0x8001e72c` | 22 | `OGC_3_OCF_18` | OGC 3 OCF 18 | medium (named, one-line purpose only, not decompiled) |
| `0x8001e748` | 20 | `OGC_3_OCF_3c` | OGC 3 OCF 3c | low (named by Kovah, purpose unclear) |
| `0x8001e760` | 28 | `OGC_3_OCF_3e` | OGC 3 OCF 3e | low (named by Kovah, purpose unclear) |
| `0x8001e780` | 4 | `HCI_Read_Loopback_Mode` | HCI Read Loopback Mode — see `hci_command_router` | high (decompiled+documented) |
| `0x8001e784` | 28 | `HCI_Enable_Device_Under_Test_Mode` | HCI Enable Device Under Test Mode | medium (named, one-line purpose only, not decompiled) |
| `0x8001ea34` | 264 | `HCI_Write_Loopback_Mode` | HCI Write Loopback Mode — see `hci_command_router` | high (decompiled+documented) |
| `0x8001eb50` | 88 | `OGC_3_OCF_28` | OGC 3 OCF 28 | medium (named, one-line purpose only, not decompiled) |
| `0x8001ebac` | 34 | `OGC_3_OCF_27` | OGC 3 OCF 27 | medium (named, one-line purpose only, not decompiled) |
| `0x8001ebd0` | 78 | `OGC_3_OCF_36` | OGC 3 OCF 36 | low (named by Kovah, purpose unclear) |
| `0x8001ec20` | 180 | `OGC_3_OCF_37` | OGC 3 OCF 37 | low (named by Kovah, purpose unclear) |
| `0x8001ecd8` | 180 | `OGC_3_OCF_26` | OGC 3 OCF 26 | medium (named, one-line purpose only, not decompiled) |
| `0x8001ed98` | 68 | `OGC_3_OCF_24` | OGC 3 OCF 24 | medium (named, one-line purpose only, not decompiled) |
| `0x8001ede4` | 74 | `OGC_3_OCF_1e` | OGC 3 OCF 1e | medium (named, one-line purpose only, not decompiled) |
| `0x8001ee34` | 106 | `OGC_3_OCF_1c` | OGC 3 OCF 1c | medium (named, one-line purpose only, not decompiled) |
| `0x8001eea4` | 90 | `OGC_3_OCF_1a` | OGC 3 OCF 1a | medium (named, one-line purpose only, not decompiled) |
| `0x8001ef54` | 314 | `OGC_3_OCF_35` | OGC 3 OCF 35 | low (named by Kovah, purpose unclear) |
| `0x8001f098` | 210 | `OGC_3_OCF_2d` | OGC 3 OCF 2d | low (named by Kovah, purpose unclear) |
| `0x8001f184` | 164 | `OGC_3_OCF_3a` | OGC 3 OCF 3a | low (named by Kovah, purpose unclear) |
| `0x8001f230` | 120 | `OGC_3_OCF_08` | OGC 3 OCF 08 | medium (named, one-line purpose only, not decompiled) |
| `0x8001f2ac` | 338 | `OGC_3_OCF_05` | OGC 3 OCF 05 | medium (named, one-line purpose only, not decompiled) |
| `0x8001f408` | 586 | `unknown_referencing_default_name_7` | unknown referencing default name 7 | low (named by Kovah, purpose unclear) |
| `0x8001f94c` | 116 | `OGC_3_default_func_5` | OGC 3 default func 5 | low (named by Kovah, purpose unclear) |
| `0x8001f9cc` | 320 | `fHCI_Remote_OOB_Extended_Data_Request_Reply_0x45` | fHCI Remote OOB Extended Data Request Reply 0x45 | low (named by Kovah, purpose unclear) |
| `0x8001fb10` | 60 | `wrap_fHCI_Remote_OOB_Extended_Data_Request_Reply_0x45` | wrap fHCI Remote OOB Extended Data Request Reply 0x45 | low (named by Kovah, purpose unclear) |
| `0x8001fb4c` | 34 | `send_evt_HCI_Authenticated_Payload_Timeout_Expired` | send evt HCI Authenticated Payload Timeout Expired | low (named by Kovah, purpose unclear) |
| `0x8001fce0` | 62 | `HCI_EVT_0x1fc_FUN_8001fce0` | HCI EVT 0x1fc FUN 8001fce0 | low (named by Kovah, purpose unclear) |
| `0x8001fef8` | 216 | `HCI_EVT_0x1f6_FUN_8001fef8` | HCI EVT 0x1f6 FUN 8001fef8 | low (named by Kovah, purpose unclear) |
| `0x800200a8` | 142 | `HCI_CMD_OGF_06__TestMode__big_switch_FUN_800200a8` | HCI CMD OGF 06  TestMode  big switch FUN 800200a8 — see `hci_command_router` | high (decompiled+documented) |
| `0x8002013c` | 74 | `HCI_CMD_OGF_04?__wraps__possible_OGF0?_referencing_default_name_10` | HCI CMD OGF 04?  wraps  possible OGF0? referencing default name 10 — see `hci_command_router` | high (decompiled+documented) |
| `0x80020188` | 288 | `HCI_CMD_OGF_05__Status_Parameters__FUN_80020188` | HCI CMD OGF 05  Status Parameters  FUN 80020188 — see `hci_command_router` | high (decompiled+documented) |
| `0x800202c0` | 600 | `HCI_CMD_OGF_03__Controller_and_Baseband__big_switch_FUN_800202c0` | HCI CMD OGF 03  Controller and Baseband  big switch FUN 800202c0 — see `hci_command_router` | high (decompiled+documented) |
| `0x8002060c` | 464 | `HCI_CMD_OGF_02__Link_Policy__FUN_8002060c` | HCI CMD OGF 02  Link Policy  FUN 8002060c — see `hci_command_router`, `lc_lmp_state_machine` | high (decompiled+documented) |
| `0x80020814` | 872 | `HCI_CMD_OGF_01__Link_Control__FUN_80020814` | HCI CMD OGF 01  Link Control  FUN 80020814 — see `hci_command_router`, `lc_lmp_state_machine`, `usb_transport_hci_driver` | high (decompiled+documented) |
| `0x80020bec` | 718 | `assoc_w_tHCI_EVT` | assoc w tHCI EVT — see `usb_transport_hci_driver` | high (decompiled+documented) |
| `0x80020ee0` | 672 | `assoc_w_tHCI_CMD` | assoc w tHCI CMD — see `hci_command_router`, `usb_transport_hci_driver` | high (decompiled+documented) |
| `0x800211b4` | 28 | `copy_bytes_in_LSB_order` | copy bytes in LSB order | low (named by Kovah, purpose unclear) |
| `0x800211f4` | 72 | `HCI_EVT_0x452_if_arg<0x41_copy_8_bytes` | HCI EVT 0x452 if arg<0x41 copy 8 bytes | low (named by Kovah, purpose unclear) |
| `0x80021924` | 76 | `initialize_some_global_struct_FUN_80021924` | initialize some global struct FUN 80021924 | medium (named, one-line purpose only, not decompiled) |
| `0x80021ab0` | 154 | `interesting_string_user_FUN_80021ab0` | interesting string user FUN 80021ab0 | medium (named, one-line purpose only, not decompiled) |
| `0x80021ba0` | 208 | `calls_reg_multiple_dptrs?_FUN_80021ba0` | calls reg multiple dptrs? FUN 80021ba0 | low (named by Kovah, purpose unclear) |
| `0x80021c9c` | 28 | `calls_interesting_string_user_FUN_80021c9c` | calls interesting string user FUN 80021c9c | medium (named, one-line purpose only, not decompiled) |
| `0x80021ec8` | 28 | `return_if_encryption_enabled_byte_at_bos_offset_0x58_ptr_index[0x26]` | return if encryption enabled byte at bos offset 0x58 ptr index[0x26] | low (named by Kovah, purpose unclear) |
| `0x80021f44` | 34 | `get_byte[0x26]_in_unknown_ptr_0x58_points_to_struct_at_least_0x27_big` | get byte[0x26] in unknown ptr 0x58 points to struct at least 0x27 big | low (named by Kovah, purpose unclear) |
| `0x80022030` | 86 | `LMP__266__FUN_80022030` | LMP  266  FUN 80022030 | medium (named, one-line purpose only, not decompiled) |
| `0x8002271c` | 60 | `send_evt_HCI_Encryption_Change[v1]` | send evt HCI Encryption Change[v1] | low (named by Kovah, purpose unclear) |
| `0x8002275c` | 52 | `send_evt_HCI_Change_Connection_Link_Key_Complete` | send evt HCI Change Connection Link Key Complete | low (named by Kovah, purpose unclear) |
| `0x80022794` | 278 | `send_evt_HCI_Link_Key_Notification` | send evt HCI Link Key Notification | low (named by Kovah, purpose unclear) |
| `0x800228b4` | 52 | `send_evt_HCI_Authentication_Complete_0x06` | send evt HCI Authentication Complete 0x06 | low (named by Kovah, purpose unclear) |
| `0x800228ec` | 100 | `send_evt_HCI_Return_Link_Keys` | send evt HCI Return Link Keys | low (named by Kovah, purpose unclear) |
| `0x80022c40` | 66 | `send_evt_HCI_PIN_Code_Request` | send evt HCI PIN Code Request | low (named by Kovah, purpose unclear) |
| `0x80022eec` | 104 | `many_sub_if_else_cases_on_param2` | many sub if else cases on param2 | low (named by Kovah, purpose unclear) |
| `0x80022f54` | 66 | `send_evt_HCI_Link_Key_Request` | send evt HCI Link Key Request | low (named by Kovah, purpose unclear) |
| `0x800233e8` | 400 | `HCI_Write_Simple_Pairing_Debug_Mode` | HCI Write Simple Pairing Debug Mode — see `hardware_layer` | high (decompiled+documented) |
| `0x80023ba4` | 52 | `send_evt_HCI_Encryption_Key_Refresh_Complete` | send evt HCI Encryption Key Refresh Complete | low (named by Kovah, purpose unclear) |
| `0x80023c4c` | 64 | `send_evt_HCI_Keypress_Notification` | send evt HCI Keypress Notification | low (named by Kovah, purpose unclear) |
| `0x80023c90` | 52 | `send_evt_HCI_Simple_Pairing_Complete_0x36` | send evt HCI Simple Pairing Complete 0x36 | low (named by Kovah, purpose unclear) |
| `0x80023cc8` | 72 | `send_evt_HCI_IO_Capability_Response` | send evt HCI IO Capability Response | low (named by Kovah, purpose unclear) |
| `0x80023e38` | 70 | `send_evt_HCI_User_Passkey_Notification` | send evt HCI User Passkey Notification | low (named by Kovah, purpose unclear) |
| `0x80023e84` | 66 | `send_evt_HCI_Remote_OOB_Data_Request` | send evt HCI Remote OOB Data Request | low (named by Kovah, purpose unclear) |
| `0x80023ecc` | 66 | `send_evt_HCI_User_Passkey_Request` | send evt HCI User Passkey Request | low (named by Kovah, purpose unclear) |
| `0x80023f14` | 84 | `send_evt_HCI_User_Confirmation_Request` | send evt HCI User Confirmation Request | low (named by Kovah, purpose unclear) |
| `0x80023f6c` | 66 | `send_evt_HCI_IO_Capability_Request` | send evt HCI IO Capability Request | low (named by Kovah, purpose unclear) |
| `0x80023fb4` | 4 | `set_arg1+1_to_arg2` | set arg1+1 to arg2 | low (named by Kovah, purpose unclear) |
| `0x80023fd0` | 10 | `some_case_0x2d` | some case 0x2d | low (named by Kovah, purpose unclear) |
| `0x800240f4` | 24 | `ret_bool_based_on_crypto_struct_0x50` | ret bool based on crypto struct 0x50 | low (named by Kovah, purpose unclear) |
| `0x8002442c` | 62 | `wrap_send_LMP_NOT_ACCEPTED` | wrap send LMP NOT ACCEPTED | low (named by Kovah, purpose unclear) |
| `0x8002469c` | 92 | `wrap_send_LMP_ACCEPTED_and_some_other_things` | wrap send LMP ACCEPTED and some other things | low (named by Kovah, purpose unclear) |
| `0x80024bd8` | 48 | `copy_fields_within_crypto_struct` | copy fields within crypto struct | low (named by Kovah, purpose unclear) |
| `0x80024ca4` | 864 | `start_with_fptr_called_by_call_send_evt_HCI_Simple_Pairing_Complete__state_machine_update?` | start with fptr called by call send evt HCI Simple Pairing Complete  state machine update? | low (named by Kovah, purpose unclear) |
| `0x80025cb4` | 118 | `LMP__271__FUN_80025cb4` | LMP  271  FUN 80025cb4 | medium (named, one-line purpose only, not decompiled) |
| `0x80025d34` | 160 | `some_case_0x3b_or_0x3c_possible_HCI_Passkey_Notification_or_HCI_Keypress_Notification` | some case 0x3b or 0x3c possible HCI Passkey Notification or HCI Keypress Notification | low (named by Kovah, purpose unclear) |
| `0x80026608` | 140 | `call_send_evt_HCI_Simple_Pairing_Complete` | call send evt HCI Simple Pairing Complete | low (named by Kovah, purpose unclear) |
| `0x80026c38` | 536 | `LMP_ENCRYPTION_MODE_REQ_0x0F` | LMP ENCRYPTION MODE REQ 0x0F — see `encryption_engine` | high (decompiled+documented) |
| `0x80026e64` | 232 | `LMP_ENCRYPTION_KEY_SIZE_REQ_0x10_possibility2` | LMP ENCRYPTION KEY SIZE REQ 0x10 possibility2 | medium (named, one-line purpose only, not decompiled) |
| `0x80026f54` | 416 | `LMP_COMB_KEY_0x09` | LMP COMB KEY 0x09 — see `encryption_engine` | high (decompiled+documented) |
| `0x80027100` | 364 | `LMP_SRES_0x0C` | LMP SRES 0x0C — see `encryption_engine` | high (decompiled+documented) |
| `0x80027454` | 466 | `LMP_IN_RAND_0x08` | LMP IN RAND 0x08 | medium (named, one-line purpose only, not decompiled) |
| `0x8002763c` | 834 | `LMP_AU_RAND_0x0B` | LMP AU RAND 0x0B — see `encryption_engine` | high (decompiled+documented) |
| `0x80027de0` | 326 | `LMP_ENCRYPTION_KEY_SIZE_REQ_0x10` | LMP ENCRYPTION KEY SIZE REQ 0x10 | medium (named, one-line purpose only, not decompiled) |
| `0x80027f30` | 74 | `LMP_ENCRYPTION_KEY_SIZE_MASK_RES_0x3B` | LMP ENCRYPTION KEY SIZE MASK RES 0x3B | medium (named, one-line purpose only, not decompiled) |
| `0x80027f80` | 76 | `LMP_ENCRYPTION_KEY_SIZE_MASK_REQ_0x3A` | LMP ENCRYPTION KEY SIZE MASK REQ 0x3A | medium (named, one-line purpose only, not decompiled) |
| `0x80027fd4` | 206 | `LMP_STOP_ENCRYPTION_REQ_0x12` | LMP STOP ENCRYPTION REQ 0x12 | medium (named, one-line purpose only, not decompiled) |
| `0x800281c4` | 160 | `LMP_NOT_ACCEPTED_0x04` | LMP NOT ACCEPTED 0x04 | medium (named, one-line purpose only, not decompiled) |
| `0x80028264` | 568 | `LMP_encryption_opcode_handlers` | LMP encryption opcode handlers — see `encryption_engine`, `hardware_layer`, `lc_lmp_state_machine` | high (decompiled+documented) |
| `0x800287b8` | 316 | `LMP_DHKEY_CHECK_0x41` | LMP DHKEY CHECK 0x41 | medium (named, one-line purpose only, not decompiled) |
| `0x80028904` | 68 | `wraps_LMP_DHKEY_CHECK_0x41` | wraps LMP DHKEY CHECK 0x41 | medium (named, one-line purpose only, not decompiled) |
| `0x80028950` | 550 | `LMP_SIMPLE_PAIRING_NUMBER_0x40` | LMP SIMPLE PAIRING NUMBER 0x40 | medium (named, one-line purpose only, not decompiled) |
| `0x80028bb8` | 294 | `LMP_SIMPLE_PAIRING_CONFIRM_0x3F` | LMP SIMPLE PAIRING CONFIRM 0x3F | medium (named, one-line purpose only, not decompiled) |
| `0x80028fc4` | 646 | `LMP_PAUSE_ENCRYPTION_AES_REQ_0x66` | LMP PAUSE ENCRYPTION AES REQ 0x66 — see `encryption_engine` | high (decompiled+documented) |
| `0x800297bc` | 110 | `LMP_USE_SEMI_PERMANENT_KEY_0x32` | LMP USE SEMI PERMANENT KEY 0x32 | medium (named, one-line purpose only, not decompiled) |
| `0x80029830` | 156 | `LMP_TEMP_KEY_0x0E` | LMP TEMP KEY 0x0E | medium (named, one-line purpose only, not decompiled) |
| `0x800298d0` | 112 | `LMP_TEMP_RAND_0x0D` | LMP TEMP RAND 0x0D — see `encryption_engine` | high (decompiled+documented) |
| `0x80029a50` | 66 | `send_evt_HCI_Link_Key_Type_Changed_0x0A` | send evt HCI Link Key Type Changed 0x0A | low (named by Kovah, purpose unclear) |
| `0x80029a98` | 200 | `wraps_send_evt_HCI_Link_Key_Type_Changed_0x0A` | wraps send evt HCI Link Key Type Changed 0x0A | low (named by Kovah, purpose unclear) |
| `0x80029c5c` | 120 | `calls_send_evt_HCI_Link_Key_Type_Changed_0x0A_and_possible_LMP_DETACH` | calls send evt HCI Link Key Type Changed 0x0A and possible LMP DETACH | low (named by Kovah, purpose unclear) |
| `0x8002a334` | 156 | `HCI_EVT_0x1fd_FUN_8002a334` | HCI EVT 0x1fd FUN 8002a334 | low (named by Kovah, purpose unclear) |
| `0x8002c338` | 602 | `thing_that_uses_SHA_and_BLAKE` | thing that uses SHA and BLAKE | low (named by Kovah, purpose unclear) |
| `0x8002c59c` | 144 | `reverse_path_to_thing_that_uses_SHA_and_BLAKE__1` | reverse path to thing that uses SHA and BLAKE  1 | low (named by Kovah, purpose unclear) |
| `0x8002c888` | 150 | `get_DHKey_to_3rd_param?` | get DHKey to 3rd param? | low (named by Kovah, purpose unclear) |
| `0x8002eae0` | 168 | `LMP__26E__FUN_8002eae0` | LMP  26E  FUN 8002eae0 | low (named by Kovah, purpose unclear) |
| `0x8002edb8` | 44 | `send_evt_HCI_Number_Of_Completed_Packets` | send evt HCI Number Of Completed Packets | low (named by Kovah, purpose unclear) |
| `0x8002f518` | 962 | `assoc_w_tHCI_TD_FUN_8002f518` | assoc w tHCI TD FUN 8002f518 | low (named by Kovah, purpose unclear) |
| `0x8002fae0` | 84 | `VSC_0xfc93_FUN_8002fae0` | VSC 0xfc93 FUN 8002fae0 | low (named by Kovah, purpose unclear) |
| `0x8002fd3c` | 328 | `VSC_0xfd40_FUN_8002fd3c` | VSC 0xfd40 FUN 8002fd3c | low (named by Kovah, purpose unclear) |
| `0x8002fea0` | 58 | `wrapper_multi-VSC_Handler_FUN_8002fea0` | wrapper multi-VSC Handler FUN 8002fea0 | low (named by Kovah, purpose unclear) |
| `0x8002fee0` | 186 | `VSC_0xfc20__download_patch__FUN_8002fee0` | VSC 0xfc20  download patch  FUN 8002fee0 | medium (named, one-line purpose only, not decompiled) |
| `0x8003003c` | 116 | `VSC_0xfc46_FUN_8003003c` | VSC 0xfc46 FUN 8003003c | low (named by Kovah, purpose unclear) |
| `0x800300c4` | 102 | `VSC_0xfc95_FUN_800300c4` | VSC 0xfc95 FUN 800300c4 | low (named by Kovah, purpose unclear) |
| `0x800302ac` | 272 | `references_patch_download_mem4` | references patch download mem4 | low (named by Kovah, purpose unclear) |
| `0x800303f4` | 306 | `VSC_0xfc35_FUN_800303f4` | VSC 0xfc35 FUN 800303f4 | low (named by Kovah, purpose unclear) |
| `0x80030b2c` | 150 | `VSC_0xfc27_FUN_80030b2c` | VSC 0xfc27 FUN 80030b2c | low (named by Kovah, purpose unclear) |
| `0x80030bdc` | 346 | `VSC_0xfc64_FUN_80030bdc` | VSC 0xfc64 FUN 80030bdc | low (named by Kovah, purpose unclear) |
| `0x80030dd8` | 268 | `VSC_0xfc61_write_to_relevant_data_FUN_80030dd8` | VSC 0xfc61 write to relevant data FUN 80030dd8 | low (named by Kovah, purpose unclear) |
| `0x80030eec` | 40 | `VSC_0xfc8b_FUN_80030eec` | VSC 0xfc8b FUN 80030eec | low (named by Kovah, purpose unclear) |
| `0x80030f1c` | 4372 | `HCI_CMD_OGF_3F__Vendor_Specific__FUN_80030f1c` | HCI CMD OGF 3F  Vendor Specific  FUN 80030f1c — see `hci_command_router` | high (decompiled+documented) |
| `0x80032540` | 2068 | `multi-VSC_Handler_FUN_80032540` | multi-VSC Handler FUN 80032540 | low (named by Kovah, purpose unclear) |
| `0x80032e28` | 20 | `called_by_function_that_uses_Logger_string_2_initialize_something_at_offset_0x800` | called by function that uses Logger string 2 initialize something at offset 0x800 | low (named by Kovah, purpose unclear) |
| `0x80033188` | 182 | `calls_fptr_down_LMP__47E_path` | calls fptr down LMP  47E path | low (named by Kovah, purpose unclear) |
| `0x80034a38` | 378 | `idk_takes_new_new_power_val` | idk takes new new power val | low (named by Kovah, purpose unclear) |
| `0x80034be0` | 120 | `set_new_power_val` | set new power val | low (named by Kovah, purpose unclear) |
| `0x80035068` | 138 | `LMP__25C_called2` | LMP  25C called2 | low (named by Kovah, purpose unclear) |
| `0x80036bd0` | 336 | `fHCI_[Create_Connection_0x08]_or_[Remote_Name_Request_0x1A]_Cancel` | fHCI [Create Connection 0x08] or [Remote Name Request 0x1A] Cancel | low (named by Kovah, purpose unclear) |
| `0x80036d44` | 86 | `fHCI_Inquiry_Cancel_0x02_1` | fHCI Inquiry Cancel 0x02 1 | low (named by Kovah, purpose unclear) |
| `0x80036df8` | 316 | `called_by_fHCI_Remote_Name_Request_5` | called by fHCI Remote Name Request 5 — see `lc_lmp_state_machine` | high (decompiled+documented) |
| `0x8003bbf0` | 94 | `VSC_0xfd49_FUN_8003bbf0` | VSC 0xfd49 FUN 8003bbf0 | low (named by Kovah, purpose unclear) |
| `0x80041c18` | 64 | `fHCI_Exit_Periodic_Inquiry_Mode_0x04` | fHCI Exit Periodic Inquiry Mode 0x04 | low (named by Kovah, purpose unclear) |
| `0x80042188` | 634 | `assoc_w_tLC_RX` | assoc w tLC RX | medium (named, one-line purpose only, not decompiled) |
| `0x80042420` | 418 | `assoc_w_tLC_TX` | assoc w tLC TX | medium (named, one-line purpose only, not decompiled) |
| `0x80042a14` | 18 | `check_new_power_val!=0` | check new power val!=0 | low (named by Kovah, purpose unclear) |
| `0x80042a28` | 16 | `check_if_at_max_power_(6)` | check if at max power (6) | low (named by Kovah, purpose unclear) |
| `0x80042a3c` | 22 | `increment_new_power_val_if_<_6` | increment new power val if < 6 | low (named by Kovah, purpose unclear) |
| `0x80042a58` | 16 | `increment_new_power_val_if_!=_0` | increment new power val if != 0 | low (named by Kovah, purpose unclear) |
| `0x80042b38` | 62 | `return_RSSI` | return RSSI | low (named by Kovah, purpose unclear) |
| `0x80043810` | 102 | `called_by_fHCI_Remote_Name_Request_3` | called by fHCI Remote Name Request 3 | low (named by Kovah, purpose unclear) |
| `0x80044430` | 90 | `OGC_3_default_func_3` | OGC 3 default func 3 | low (named by Kovah, purpose unclear) |
| `0x80044674` | 176 | `HCI_CMD_OGF_08__LE_Commands__big_switch` | HCI CMD OGF 08  LE Commands  big switch — see `ble_link_layer`, `hci_command_router` | high (decompiled+documented) |
| `0x800447a4` | 90 | `send_evt_Meta_subevent_0x13` | send evt Meta subevent 0x13 — see `ble_link_layer` | high (decompiled+documented) |
| `0x80044804` | 26 | `send_evt_Meta_subevent_0x11` | send evt Meta subevent 0x11 — see `ble_link_layer` | high (decompiled+documented) |
| `0x80044820` | 36 | `send_evt_Meta_subevent_0x14_HCI_LE_Channel_Selection_Algorithm` | send evt Meta subevent 0x14 HCI LE Channel Selection Algorithm — see `ble_link_layer` | high (decompiled+documented) |
| `0x80044844` | 74 | `send_evt_Meta_subevent_0x07` | send evt Meta subevent 0x07 — see `ble_link_layer` | high (decompiled+documented) |
| `0x80044890` | 50 | `send_evt_Meta_subevent_0x04` | send evt Meta subevent 0x04 — see `ble_link_layer` | high (decompiled+documented) |
| `0x800448c4` | 46 | `send_evt_Meta_subevent_0x03_HCI_LE_Connection_Update_Complete` | send evt Meta subevent 0x03 HCI LE Connection Update Complete — see `ble_link_layer` | high (decompiled+documented) |
| `0x80044990` | 148 | `send_evt_Meta_subevent_0x16` | send evt Meta subevent 0x16 — see `ble_link_layer` | high (decompiled+documented) |
| `0x80044a24` | 38 | `send_evt_Meta_subevent_0x17` | send evt Meta subevent 0x17 — see `ble_link_layer` | high (decompiled+documented) |
| `0x80044c7c` | 594 | `send_evt_Meta_subevent_0_or_1` | send evt Meta subevent 0 or 1 — see `ble_link_layer` | high (decompiled+documented) |
| `0x800454e8` | 74 | `send_evt_Meta_subevent_0x12` | send evt Meta subevent 0x12 — see `ble_link_layer` | high (decompiled+documented) |
| `0x80045534` | 66 | `send_evt_Meta_subevent_0x0c_HCI_LE_PHY_Update_Complete` | send evt Meta subevent 0x0c HCI LE PHY Update Complete — see `ble_link_layer` | high (decompiled+documented) |
| `0x80045578` | 56 | `send_evt_Meta_subevent_0x09_HCI_LE_Generate_DHKey_Complete` | send evt Meta subevent 0x09 HCI LE Generate DHKey Complete — see `ble_link_layer` | high (decompiled+documented) |
| `0x800455b8` | 58 | `send_evt_Meta_subevent_0x08_HCI_LE_Read_Local_P-256_Public_Key_Complete` | send evt Meta subevent 0x08 HCI LE Read Local P-256 Public Key Complete — see `ble_link_layer` | high (decompiled+documented) |
| `0x800455f4` | 332 | `send_evt_Meta_subevent_0x2_or_0x0b` | send evt Meta subevent 0x2 or 0x0b — see `ble_link_layer` | high (decompiled+documented) |
| `0x80045c00` | 112 | `send_evt_Meta_subevent_0x05_HCI_LE_Long_Term_Key_Request` | send evt Meta subevent 0x05 HCI LE Long Term Key Request — see `ble_link_layer` | high (decompiled+documented) |
| `0x80045e8c` | 1032 | `send_evt_Meta_subevent_0x01_or_0x0a_HCI_LE_Connection_Complete_or_HCI_LE_Enhanced_Connection_Complete` | send evt Meta subevent 0x01 or 0x0a HCI LE Connection Complete or HCI LE Enhanced Connection Complete — see `ble_link_layer` | high (decompiled+documented) |
| `0x80046620` | 34 | `put_0x1f4_struct_pointer_from_index_arg1_into_arg2` | put 0x1f4 struct pointer from index arg1 into arg2 — see `ble_link_layer`, `hci_command_router` | high (decompiled+documented) |
| `0x8004a71c` | 16 | `VSC_0xfc95_called1` | VSC 0xfc95 called1 | low (named by Kovah, purpose unclear) |
| `0x8004c0f4` | 472 | `LMP__26F__sends_LE_HCI_Events` | LMP  26F  sends LE HCI Events | low (named by Kovah, purpose unclear) |
| `0x800525b4` | 36 | `send_evt_Meta_buf_at_arg1+0x100` | send evt Meta buf at arg1+0x100 | low (named by Kovah, purpose unclear) |
| `0x800525d8` | 62 | `send_evt_Meta_buf_at_arg1` | send evt Meta buf at arg1 | low (named by Kovah, purpose unclear) |
| `0x800566f8` | 58 | `VSC_0xfc97_1_FUN_800566f8` | VSC 0xfc97 1 FUN 800566f8 | medium (named, one-line purpose only, not decompiled) |
| `0x8005681c` | 84 | `VSC_0xfc73_3_FUN_8005681c` | VSC 0xfc73 3 FUN 8005681c | low (named by Kovah, purpose unclear) |
| `0x80056878` | 84 | `VSC_0xfc73_2_FUN_80056878` | VSC 0xfc73 2 FUN 80056878 | low (named by Kovah, purpose unclear) |
| `0x800568d4` | 94 | `VSC_0xfc73_1_FUN_800568d4` | VSC 0xfc73 1 FUN 800568d4 | low (named by Kovah, purpose unclear) |
| `0x8005770c` | 166 | `VSC_0xfc97_2_FUN_8005770c` | VSC 0xfc97 2 FUN 8005770c | medium (named, one-line purpose only, not decompiled) |
| `0x800596c8` | 50 | `get_0x1ac_struct_ptr_by_index` | get 0x1ac struct ptr by index | low (named by Kovah, purpose unclear) |
| `0x8005a298` | 62 | `get_TX_or_RX_PHY` | get TX or RX PHY | low (named by Kovah, purpose unclear) |
| `0x8005d26c` | 88 | `assign_pointer_to_0x1AC_offset_0x134` | assign pointer to 0x1AC offset 0x134 — see `lmp_version_conn_setup` | high (decompiled+documented) |
| `0x8005e23c` | 118 | `access_config_at_0xa5_and_0x1ac_stuct_stuff` | access config at 0xa5 and 0x1ac stuct stuff — see `lmp_version_conn_setup` | high (decompiled+documented) |
| `0x8005e3b8` | 80 | `c_by_fHCI_Read_Remote_Version_Information_various_0x1ac_manip` | c by fHCI Read Remote Version Information various 0x1ac manip | low (named by Kovah, purpose unclear) |
| `0x800600e8` | 218 | `LMP__270__FUN_800600e8` | LMP  270  FUN 800600e8 | low (named by Kovah, purpose unclear) |
| `0x800605a4` | 4 | `just_return_0` | just return 0 | low (named by Kovah, purpose unclear) |
| `0x800605a8` | 308 | `get_status_bits_by_LMP_Opcode` | get status bits by LMP Opcode | low (named by Kovah, purpose unclear) |
| `0x80060740` | 44 | `lookup_up_to_3_bos_array_indices_by_connection_handle` | lookup up to 3 bos array indices by connection handle | low (named by Kovah, purpose unclear) |
| `0x800608f0` | 222 | `some_case_0x2b` | some case 0x2b | low (named by Kovah, purpose unclear) |
| `0x80060c30` | 80 | `look_for_non_matching_bdaddr_bos_index_i.e._free_connection_slot` | look for non matching bdaddr bos index i.e. free connection slot | low (named by Kovah, purpose unclear) |
| `0x80060cfc` | 16 | `zero_initialize_6_bytes_at_param1` | zero initialize 6 bytes at param1 | low (named by Kovah, purpose unclear) |
| `0x80060d0c` | 186 | `called_by_fHCI_Remote_Name_Request_6_nop_if_not_patched?` | called by fHCI Remote Name Request 6 nop if not patched? | low (named by Kovah, purpose unclear) |
| `0x80060dd8` | 188 | `return_big_ol_array_offset` | return big ol array offset | low (named by Kovah, purpose unclear) |
| `0x800611e4` | 720 | `send_LMP_pkt` | send LMP pkt — see `lc_lmp_state_machine` | high (decompiled+documented) |
| `0x800615a8` | 62 | `send_extended_opcode_LMP_reply?` | send extended opcode LMP reply? | low (named by Kovah, purpose unclear) |
| `0x80061624` | 122 | `LMP_PACKET_TYPE_TABLE_REQ_0x7F_0B` | LMP PACKET TYPE TABLE REQ 0x7F 0B | low (named by Kovah, purpose unclear) |
| `0x80061754` | 44 | `if_arg1<3_memcpy_features_to_arg2_else_HCI_Inquiry_Cancel` | if arg1<3 memcpy features to arg2 else HCI Inquiry Cancel | low (named by Kovah, purpose unclear) |
| `0x80061784` | 100 | `copy_feature_and_send_extended_LMP_reply` | copy feature and send extended LMP reply | low (named by Kovah, purpose unclear) |
| `0x800617ec` | 592 | `LMP_FEATURES_RES_EXT_0x7F_04` | LMP FEATURES RES EXT 0x7F 04 | low (named by Kovah, purpose unclear) |
| `0x80061a4c` | 70 | `LMP_FEATURES_REQ_EXT_0x7F_03` | LMP FEATURES REQ EXT 0x7F 03 | low (named by Kovah, purpose unclear) |
| `0x80061ad8` | 88 | `LMP_NOT_ACCEPTED_EXT_0x7F_02` | LMP NOT ACCEPTED EXT 0x7F 02 | low (named by Kovah, purpose unclear) |
| `0x80061b34` | 80 | `LMP_ACCEPTED_EXT_0x7F_01` | LMP ACCEPTED EXT 0x7F 01 | low (named by Kovah, purpose unclear) |
| `0x80061bb8` | 156 | `LMP_extended_opcode_handler_0x01-0x11` | LMP extended opcode handler 0x01-0x11 | low (named by Kovah, purpose unclear) |
| `0x80061e70` | 60 | `some_case_0x37_2` | some case 0x37 2 | low (named by Kovah, purpose unclear) |
| `0x80061eb0` | 160 | `LMP_SNIFF_SUBRATING_RES_0x7F_16` | LMP SNIFF SUBRATING RES 0x7F 16 | low (named by Kovah, purpose unclear) |
| `0x80062054` | 238 | `LMP_SNIFF_SUBRATING_REQ_0x7F_15` | LMP SNIFF SUBRATING REQ 0x7F 15 | low (named by Kovah, purpose unclear) |
| `0x80062158` | 70 | `LMP_extended_opcode_handler_0x15-0x16` | LMP extended opcode handler 0x15-0x16 | low (named by Kovah, purpose unclear) |
| `0x80062270` | 166 | `LMP__267__FUN_80062270` | LMP  267  FUN 80062270 | low (named by Kovah, purpose unclear) |
| `0x8006251c` | 132 | `LMP_POWER_CONTROL_RES` | LMP POWER CONTROL RES | low (named by Kovah, purpose unclear) |
| `0x80062658` | 150 | `LMP_POWER_CONTROL_REQ` | LMP POWER CONTROL REQ | low (named by Kovah, purpose unclear) |
| `0x800626f8` | 116 | `0x7F_LMP_POWER_REQ_RES_0x1F_0x20` | 0x7F LMP POWER REQ RES 0x1F 0x20 | low (named by Kovah, purpose unclear) |
| `0x80062924` | 76 | `LMP_CLK_ADJ_ACK_0x7F_0x06` | LMP CLK ADJ ACK 0x7F 0x06 | low (named by Kovah, purpose unclear) |
| `0x80062a58` | 568 | `VSC_0xfcd9_FUN_80062a58` | VSC 0xfcd9 FUN 80062a58 | low (named by Kovah, purpose unclear) |
| `0x80062cac` | 390 | `LMP_CLK_ADJ_0x7F_0x05` | LMP CLK ADJ 0x7F 0x05 | low (named by Kovah, purpose unclear) |
| `0x80062e44` | 318 | `LMP_CLK_ADJ_REQ_0x7F_0x07` | LMP CLK ADJ REQ 0x7F 0x07 | low (named by Kovah, purpose unclear) |
| `0x80062f94` | 126 | `0x7F_LMP_CLK_ADJ(0x05)_ADJ_ACK(0x06)_ADJ_REQ(0x07)` | 0x7F LMP CLK ADJ(0x05) ADJ ACK(0x06) ADJ REQ(0x07) | low (named by Kovah, purpose unclear) |
| `0x80063458` | 96 | `LMP_CHANNEL_CLASSIFICATION_0x7F_11` | LMP CHANNEL CLASSIFICATION 0x7F 11 | low (named by Kovah, purpose unclear) |
| `0x800634c0` | 204 | `LMP_CHANNEL_CLASSIFICATION_REQ_0x7F_10` | LMP CHANNEL CLASSIFICATION REQ 0x7F 10 | low (named by Kovah, purpose unclear) |
| `0x80063cc4` | 438 | `LMP_SET_AFH_0x3C` | LMP SET AFH 0x3C | low (named by Kovah, purpose unclear) |
| `0x800656bc` | 256 | `LMP_CH__0x3ea__FUN_800656bc` | LMP CH  0x3ea  FUN 800656bc | low (named by Kovah, purpose unclear) |
| `0x80066e68` | 200 | `assoc_w_tLMP_CH` | assoc w tLMP CH | medium (named, one-line purpose only, not decompiled) |
| `0x80067128` | 160 | `set_check_for_1_to_1` | set check for 1 to 1 | low (named by Kovah, purpose unclear) |
| `0x80067a2c` | 680 | `unknown_referencing_default_name(2x)_9` | unknown referencing default name(2x) 9 | low (named by Kovah, purpose unclear) |
| `0x800683d8` | 40 | `calls_fptr` | calls fptr | low (named by Kovah, purpose unclear) |
| `0x80068400` | 40 | `c_by_LMP_ENCAPSULATED_PAYLOAD_0x3E_call_fptr` | c by LMP ENCAPSULATED PAYLOAD 0x3E call fptr | low (named by Kovah, purpose unclear) |
| `0x8006845c` | 78 | `call_if_encapsulated_payload_or_header_rejected` | call if encapsulated payload or header rejected | low (named by Kovah, purpose unclear) |
| `0x800684c8` | 36 | `LMP_NOT_ACCEPTED` | LMP NOT ACCEPTED | low (named by Kovah, purpose unclear) |
| `0x800684ec` | 192 | `LMP_ENCAPSULATED_PAYLOAD_0x3E` | LMP ENCAPSULATED PAYLOAD 0x3E | low (named by Kovah, purpose unclear) |
| `0x800685b4` | 110 | `LMP_ENCAPSULATED_HEADER_0x3D` | LMP ENCAPSULATED HEADER 0x3D | low (named by Kovah, purpose unclear) |
| `0x80068680` | 122 | `send_LMP_ENCAPSULATED_HEADER_reply` | send LMP ENCAPSULATED HEADER reply | low (named by Kovah, purpose unclear) |
| `0x800686fc` | 94 | `wraps_send_LMP_ENCAPSULATED_HEADER_reply2` | wraps send LMP ENCAPSULATED HEADER reply2 | low (named by Kovah, purpose unclear) |
| `0x80068764` | 76 | `wraps_send_LMP_ENCAPSULATED_HEADER_reply` | wraps send LMP ENCAPSULATED HEADER reply | low (named by Kovah, purpose unclear) |
| `0x800687b8` | 46 | `wraps_send_LMP_ENCAPSULATED_HEADER_reply_1_and_2` | wraps send LMP ENCAPSULATED HEADER reply 1 and 2 | low (named by Kovah, purpose unclear) |
| `0x800687e8` | 130 | `LMP_encapsulated_header_and_payload_0x3D_0x3E` | LMP encapsulated header and payload 0x3D 0x3E | low (named by Kovah, purpose unclear) |
| `0x800688f4` | 32 | `LMP_TIMING_ACCURACY_RES_0x30` | LMP TIMING ACCURACY RES 0x30 | low (named by Kovah, purpose unclear) |
| `0x80068918` | 28 | `LMP_MIN_POWER_0x22` | LMP MIN POWER 0x22 | low (named by Kovah, purpose unclear) |
| `0x80068938` | 28 | `LMP_MAX_POWER_0x21` | LMP MAX POWER 0x21 | low (named by Kovah, purpose unclear) |
| `0x80068a2c` | 180 | `LMP_TEST_ACTIVATE_0x38` | LMP TEST ACTIVATE 0x38 | low (named by Kovah, purpose unclear) |
| `0x80068aec` | 130 | `recv_LMP_VERSION_REQ_0x25_send_LMP_VERSION_RES_0x26` | recv LMP VERSION REQ 0x25 send LMP VERSION RES 0x26 | low (named by Kovah, purpose unclear) |
| `0x80068f74` | 108 | `LMP_SLOT_OFFSET_0x34` | LMP SLOT OFFSET 0x34 | low (named by Kovah, purpose unclear) |
| `0x80068fe4` | 62 | `send_LMP_FEATURES_REQ_or_RES` | send LMP FEATURES REQ or RES | low (named by Kovah, purpose unclear) |
| `0x80069028` | 52 | `LMP_FEATURES_REQ_0x27` | LMP FEATURES REQ 0x27 | low (named by Kovah, purpose unclear) |
| `0x80069060` | 116 | `LMP_TEST_CONTROL_0x39` | LMP TEST CONTROL 0x39 | low (named by Kovah, purpose unclear) |
| `0x8006943c` | 214 | `LMP_INCR_POWER_REQ_0x1f` | LMP INCR POWER REQ 0x1f | low (named by Kovah, purpose unclear) |
| `0x80069534` | 94 | `LMP_TIMING_ACCURACY_REQ_0x2F` | LMP TIMING ACCURACY REQ 0x2F | low (named by Kovah, purpose unclear) |
| `0x8006959c` | 84 | `LMP_CLKOFFSET_REQ_0x05` | LMP CLKOFFSET REQ 0x05 | low (named by Kovah, purpose unclear) |
| `0x800695f4` | 90 | `LMP_NAME_REQ_0x01` | LMP NAME REQ 0x01 | low (named by Kovah, purpose unclear) |
| `0x80069658` | 214 | `LMP_DECR_POWER_REQ_0x20` | LMP DECR POWER REQ 0x20 | low (named by Kovah, purpose unclear) |
| `0x80069750` | 62 | `LMP_PAGE_MODE_RES_0x36` | LMP PAGE MODE RES 0x36 | low (named by Kovah, purpose unclear) |
| `0x80069794` | 298 | `LMP_QUALITY_OF_SERVICE_REQ_0x2A` | LMP QUALITY OF SERVICE REQ 0x2A | low (named by Kovah, purpose unclear) |
| `0x800698c8` | 62 | `LMP_QUALITY_OF_SERVICE_0x29` | LMP QUALITY OF SERVICE 0x29 | low (named by Kovah, purpose unclear) |
| `0x8006990c` | 128 | `LMP_PAGE_MODE_REQ_0x35` | LMP PAGE MODE REQ 0x35 | low (named by Kovah, purpose unclear) |
| `0x80069998` | 168 | `LMP_START_ENCRYPTION_REQ_0x18` | LMP START ENCRYPTION REQ 0x18 | low (named by Kovah, purpose unclear) |
| `0x80069a4c` | 560 | `LMP_START_ENCRYPTION_REQ_0x17` | LMP START ENCRYPTION REQ 0x17 | low (named by Kovah, purpose unclear) |
| `0x80069c94` | 194 | `LMP_DETACH_0x07` | LMP DETACH 0x07 | low (named by Kovah, purpose unclear) |
| `0x80069d6c` | 42 | `LMP_AUTO_RATE_0x23` | LMP AUTO RATE 0x23 | low (named by Kovah, purpose unclear) |
| `0x80069d9c` | 154 | `LMP_PREFERRED_RATE_0x24` | LMP PREFERRED RATE 0x24 | low (named by Kovah, purpose unclear) |
| `0x80069e40` | 84 | `LMP_SETUP_COMPLETE_0x31` | LMP SETUP COMPLETE 0x31 | low (named by Kovah, purpose unclear) |
| `0x80069e98` | 310 | `LMP_HOST_CONNECTION_REQ_0x33` | LMP HOST CONNECTION REQ 0x33 | low (named by Kovah, purpose unclear) |
| `0x80069fe4` | 148 | `LMP_SUPERVISION_TIMEOUT_0x37` | LMP SUPERVISION TIMEOUT 0x37 | low (named by Kovah, purpose unclear) |
| `0x8006a084` | 74 | `LMP_CLKOFFSET_RES_0x06` | LMP CLKOFFSET RES 0x06 | low (named by Kovah, purpose unclear) |
| `0x8006a0d4` | 90 | `LMP_VERSION_RES_0x26` | LMP VERSION RES 0x26 | low (named by Kovah, purpose unclear) |
| `0x8006a134` | 626 | `LMP_SWITCH_REQ_0x13` | LMP SWITCH REQ 0x13 | low (named by Kovah, purpose unclear) |
| `0x8006a3dc` | 108 | `LMP_MAX_SLOT_0x2D` | LMP MAX SLOT 0x2D | low (named by Kovah, purpose unclear) |
| `0x8006a450` | 140 | `LMP_MAX_SLOT_REQ_0x2E` | LMP MAX SLOT REQ 0x2E | low (named by Kovah, purpose unclear) |
| `0x8006a4e8` | 296 | `LMP_NAME_RES_0x02` | LMP NAME RES 0x02 | low (named by Kovah, purpose unclear) |
| `0x8006a698` | 242 | `helper_function_send_reply_LMP_FEATURES_RES_0x28` | helper function send reply LMP FEATURES RES 0x28 | low (named by Kovah, purpose unclear) |
| `0x8006a794` | 238 | `LMP_FEATURES_RES_0x28` | LMP FEATURES RES 0x28 | low (named by Kovah, purpose unclear) |
| `0x8006aae4` | 122 | `LMP_NOT_ACCEPTED_0x04` | LMP NOT ACCEPTED 0x04 | low (named by Kovah, purpose unclear) |
| `0x8006ac9c` | 190 | `LMP_ACCEPTED_0x03` | LMP ACCEPTED 0x03 | low (named by Kovah, purpose unclear) |
| `0x8006b1e4` | 58 | `lookup_some_sort_of_connection_struct_index_by_connection_handle` | lookup some sort of connection struct index by connection handle | low (named by Kovah, purpose unclear) |
| `0x8006bcfc` | 164 | `LMP_REMOVE_SCO_LINK_REQ_0x2C` | LMP REMOVE SCO LINK REQ 0x2C | low (named by Kovah, purpose unclear) |
| `0x8006c6e0` | 168 | `LMP_SCO_LINK_REQ_0x2B` | LMP SCO LINK REQ 0x2B | low (named by Kovah, purpose unclear) |
| `0x8006c858` | 36 | `called_by_fHCI_Read_LMP_Handle_3` | called by fHCI Read LMP Handle 3 | low (named by Kovah, purpose unclear) |
| `0x8006eff0` | 208 | `LMP_REMOVE_eSCO_LINK_REQ_0x7F_0D` | LMP REMOVE eSCO LINK REQ 0x7F 0D | low (named by Kovah, purpose unclear) |
| `0x8006f0d0` | 1600 | `LMP_eSCO_LINK_REQ_0x7F_0C` | LMP eSCO LINK REQ 0x7F 0C | low (named by Kovah, purpose unclear) |
| `0x8006f870` | 106 | `some_case_0x37_1` | some case 0x37 1 | low (named by Kovah, purpose unclear) |
| `0x8006f8e8` | 96 | `path2_send_evt_0x14_HCI_Mode_Change` | path2 send evt 0x14 HCI Mode Change | low (named by Kovah, purpose unclear) |
| `0x8006ff00` | 40 | `some_case_0x13` | some case 0x13 | low (named by Kovah, purpose unclear) |
| `0x80070248` | 144 | `LMP__48A__FUN_80070248` | LMP  48A  FUN 80070248 | low (named by Kovah, purpose unclear) |
| `0x800702e4` | 246 | `LMP__259__FUN_800702e4` | LMP  259  FUN 800702e4 | low (named by Kovah, purpose unclear) |
| `0x800703f0` | 68 | `LMP__600__FUN_800703f0` | LMP  600  FUN 800703f0 | low (named by Kovah, purpose unclear) |
| `0x80070454` | 272 | `possible_LMP_DETACH` | possible LMP DETACH | low (named by Kovah, purpose unclear) |
| `0x800707dc` | 164 | `HCI_EVT_0x500_FUN_800707dc` | HCI EVT 0x500 FUN 800707dc | low (named by Kovah, purpose unclear) |
| `0x8007088c` | 48 | `LMP__25C_called3` | LMP  25C called3 | low (named by Kovah, purpose unclear) |
| `0x8007095c` | 568 | `LMP__489__various_sub-cases` | LMP  489  various sub-cases | low (named by Kovah, purpose unclear) |
| `0x80070ba4` | 92 | `LMP__25C__FUN_80070ba4` | LMP  25C  FUN 80070ba4 | low (named by Kovah, purpose unclear) |
| `0x80070c04` | 1192 | `LMP_"480"_only_path_that_goes_to_real_LMP_switch` | LMP "480" only path that goes to real LMP switch | low (named by Kovah, purpose unclear) |
| `0x80071370` | 82 | `LMP__47F__FUN_80071370` | LMP  47F  FUN 80071370 | low (named by Kovah, purpose unclear) |
| `0x800713d4` | 182 | `LMP__47E__FUN_800713d4` | LMP  47E  FUN 800713d4 | low (named by Kovah, purpose unclear) |
| `0x800714a0` | 220 | `LMP__267__FUN_800714a0` | LMP  267  FUN 800714a0 | low (named by Kovah, purpose unclear) |
| `0x80071620` | 20 | `called_at_end_of_crypto_state_machine_update` | called at end of crypto state machine update | low (named by Kovah, purpose unclear) |
| `0x80071634` | 462 | `assoc_w_tLMP` | assoc w tLMP | medium (named, one-line purpose only, not decompiled) |
| `0x80071b50` | 44 | `LMP__264__FUN_80071b50` | LMP  264  FUN 80071b50 | low (named by Kovah, purpose unclear) |
| `0x80071b84` | 26 | `set_bos[bosi].0xb2_index=arg2` | set bos[bosi].0xb2 index=arg2 | low (named by Kovah, purpose unclear) |
| `0x80071ba4` | 26 | `check_if_80122df0_is_non-zero_else_ret_0xff` | check if 80122df0 is non-zero else ret 0xff | low (named by Kovah, purpose unclear) |
| `0x80071d98` | 306 | `something_using_LMP_features` | something using LMP features | low (named by Kovah, purpose unclear) |
| `0x80072404` | 54 | `send_LMP_NOT_ACCEPTED` | send LMP NOT ACCEPTED | low (named by Kovah, purpose unclear) |
| `0x8007243c` | 56 | `send_LMP_ACCEPTED` | send LMP ACCEPTED | low (named by Kovah, purpose unclear) |
| `0x80072648` | 70 | `LMP_unknown_else` | LMP unknown else | low (named by Kovah, purpose unclear) |
| `0x80073348` | 362 | `called_by_called_at_end_of_crypto_state_machine_update` | called by called at end of crypto state machine update | low (named by Kovah, purpose unclear) |
| `0x80073b74` | 348 | `HCI_Disconnect_on_error` | HCI Disconnect on error | low (named by Kovah, purpose unclear) |
| `0x80074c8c` | 232 | `LMP_CH__0x3ed__FUN_80074c8c` | LMP CH  0x3ed  FUN 80074c8c | low (named by Kovah, purpose unclear) |
| `0x80074d84` | 14 | `set_two_global_ptrs` | set two global ptrs | low (named by Kovah, purpose unclear) |
| `0x80074dfc` | 42 | `called_by_unknown_fptr_indexA_2` | called by unknown fptr indexA 2 | low (named by Kovah, purpose unclear) |
| `0x80074e38` | 50 | `possible_logger_called_if_no_patch2` | possible logger called if no patch2 | low (named by Kovah, purpose unclear) |
| `0x80074e84` | 38 | `called_by_unknown_fptr_indexA_1` | called by unknown fptr indexA 1 | low (named by Kovah, purpose unclear) |
| `0x80074eb4` | 42 | `unknown_fptr_indexA` | unknown fptr indexA | low (named by Kovah, purpose unclear) |
| `0x80074ee0` | 64 | `function_that_uses_Logger_string` | function that uses Logger string | low (named by Kovah, purpose unclear) |
| `0x80074f38` | 94 | `possible_logger_called_if_no_patch1` | possible logger called if no patch1 | low (named by Kovah, purpose unclear) |
| `0x80074fa8` | 204 | `possible_logging_function?_var_args` | possible logging function? var args — see `conn_record_subsystem`, `interrupt_vectors` | high (decompiled+documented) |
| `0x80075084` | 402 | `unknown_referencing_default_name_8` | unknown referencing default name 8 | low (named by Kovah, purpose unclear) |
| `0x80075324` | 224 | `func1_that_uses_structs_at_0x80100000` | func1 that uses structs at 0x80100000 | low (named by Kovah, purpose unclear) |
| `0x800754c4` | 22 | `func3_that_uses_structs_at_0x80100000` | func3 that uses structs at 0x80100000 | low (named by Kovah, purpose unclear) |
| `0x80075540` | 258 | `uninteresting_if_0x80100000!=0_which_its_not_in_my_tests` | uninteresting if 0x80100000!=0 which its not in my tests | low (named by Kovah, purpose unclear) |
| `0x80075650` | 102 | `func4_that_uses_structs_at_0x80100000` | func4 that uses structs at 0x80100000 | low (named by Kovah, purpose unclear) |
| `0x800756c0` | 62 | `func5_that_uses_structs_at_0x80100000` | func5 that uses structs at 0x80100000 | low (named by Kovah, purpose unclear) |
| `0x80075704` | 34 | `func6_that_uses_structs_at_0x80100000` | func6 that uses structs at 0x80100000 | low (named by Kovah, purpose unclear) |
| `0x8007572c` | 106 | `func7_that_uses_structs_at_0x80100000` | func7 that uses structs at 0x80100000 | low (named by Kovah, purpose unclear) |
| `0x8007579c` | 188 | `func8_that_uses_structs_at_0x80100000` | func8 that uses structs at 0x80100000 | low (named by Kovah, purpose unclear) |
| `0x80075948` | 258 | `memcpy_to_MMIO_for_sending_packets?` | memcpy to MMIO for sending packets? | low (named by Kovah, purpose unclear) |
| `0x80075e34` | 106 | `possible_logger_called_if_no_patch4_recursive_to_possible_logger` | possible logger called if no patch4 recursive to possible logger | low (named by Kovah, purpose unclear) |
| `0x800761f4` | 116 | `LMP__25B_meat` | LMP  25B meat | low (named by Kovah, purpose unclear) |
| `0x800762f4` | 852 | `called_by_unknown_fptr_index1_big_do_while_true` | called by unknown fptr index1 big do while true | low (named by Kovah, purpose unclear) |
| `0x8007666c` | 22 | `unknown_fptr_index1` | unknown fptr index1 | low (named by Kovah, purpose unclear) |
| `0x80076bd8` | 48 | `swap_byte_order` | swap byte order | low (named by Kovah, purpose unclear) |
| `0x80077474` | 130 | `VSC_0xfca1_FUN_80077474` | VSC 0xfca1 FUN 80077474 | low (named by Kovah, purpose unclear) |
| `0x80077620` | 22 | `call2funcs` | call2funcs | low (named by Kovah, purpose unclear) |
| `0x80078e68` | 72 | `VSC_0xfc7a_FUN_80078e68` | VSC 0xfc7a FUN 80078e68 | low (named by Kovah, purpose unclear) |
| `0x8007943c` | 36 | `send_evt_INVALID_opcode_0xFF` | send evt INVALID opcode 0xFF | low (named by Kovah, purpose unclear) |
| `0x800798b0` | 122 | `call_to_HCI_Disconnect_on_error` | call to HCI Disconnect on error | low (named by Kovah, purpose unclear) |
| `0x800013a4` | 174 | `select_and_program_sco_esco_packet_type_for_conn` | chooses SCO/eSCO packet-type word and programs it — see `region_0x80000000` | high (decompiled+documented) |
| `0x80001470` | 146 | `esco_quality_window_recovery_check` | eSCO degrade-on-quality-window-miss recovery step — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000151c` | 64 | `scheduler_find_next_min_deadline` | scheduler-tick/next-timeout finder — see `region_0x80000000` | high (decompiled+documented) |
| `0x80001564` | 220 | `feature_bit_packet_type_policy_chooser` | feature-bit-driven packet-type policy chooser — see `region_0x80000000` | high (decompiled+documented) |
| `0x80001648` | 728 | `apply_codec_type_and_role_switch_hook_dispatch` | central codec-type/role-switch-hook apply routine — see `region_0x80000000` | high (decompiled+documented) |
| `0x80001944` | 58 | `vsc_clear_bit13_and_log_0x2cd` | clears reg 0x32 bit 13 and logs event 0x2cd — see `region_0x80000000` | high (decompiled+documented) |
| `0x80001990` | 292 | `role_switch_or_afh_table_entry_toggle_and_log` | role-switch/AFH table-entry toggle and log — see `region_0x80000000` | high (decompiled+documented) |
| `0x80001ad8` | 86 | `lmp_role_switch_param_fixup_and_log_confirm_mismatch` | role-switch-PDU param fixup + confirmation-mismatch logger — see `region_0x80000000` | high (decompiled+documented) |
| `0x80001b3c` | 238 | `role_switch_hook_clear_and_packet_type_reset_seq4` | state-4-to-state-8 role-switch teardown step — see `region_0x80000000` | high (decompiled+documented) |
| `0x80001c4c` | 700 | `role_switch_completion_or_abort_handler` | role-switch completion/abort handler — see `region_0x80000000` | high (decompiled+documented) |
| `0x80001f34` | 248 | `esco_renegotiation_request_gate` | gates whether an eSCO renegotiation request can proceed now or must wait — see `region_0x80000000` | high (decompiled+documented) |
| `0x80002048` | 334 | `page_response_timing_and_afh_update_counters` | page-response/AFH-bookkeeping update routine — see `region_0x80000000` | high (decompiled+documented) |
| `0x800021c0` | 254 | `link_quality_mode3_packet_type_reprogram` | link-quality-driven packet-type downgrade-to-SCO — see `region_0x80000000` | high (decompiled+documented) |
| `0x800022e4` | 384 | `truncated_page_complete_status_dispatcher` | top-level HCI Truncated Page Complete status-word dispatcher — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000c350` | 56 | `lmp_25b_afh_toggle_via_vsc_0xfc95` | AFH/channel-feature-toggle call site (4th sibling) — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000c3f4` | 414 | `feature_bit_status_word_propagator` | central feature-bit-driven status propagation routine — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000c5c8` | 136 | `swap_status_bits_between_globals_if_consistent` | bit-swap-if-consistent primitive — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000c664` | 32 | `set_bit7_of_global_from_param` | bit-7 set/clear helper — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000c688` | 152 | `pack_freq_and_status_fields_from_globals` | multi-field status/frequency-word packer — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000c738` | 54 | `set_bit0_and_mirror_if_feature_set` | set-and-conditionally-propagate bit-0 helper — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000c77c` | 58 | `apply_freq_field_or_call_optional_fptr` | RF-register-accessor leaf — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000cfcc` | 12 | `or_bit4_into_global` | trivial unconditional bit-4 OR-in helper — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000cfdc` | 28 | `toggle_bit5_of_global_v2` | second, distinct bit-5 toggle helper — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000cffc` | 28 | `toggle_bit6_of_global` | boolean-driven bit-6 toggle — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000d8f8` | 694 | `afh_or_rf_divider_reconfig_orchestrator` | top-level AFH/RF-divider reconfig orchestrator — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000dbdc` | 226 | `program_packet_type_with_default_fallback` | packet-type-program-with-cascading-fallback wrapper — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000e1b0` | 430 | `vsc_0xfc56_payload_apply_and_rf_reconfig` | payload-apply body for VSC 0xfc56 — see `region_0x80000000` | high (decompiled+documented) |
| `0x80002488` | 1136 | `status_word_multiflag_link_event_dispatcher` | master multi-flag status-word link-event dispatcher — see `region_0x80000000` | high (decompiled+documented) |
| `0x80002cc0` | 398 | `auth_retry_counter_escalation_handler` | encryption/auth-retry escalation-to-detach handler — see `region_0x80000000` | high (decompiled+documented) |
| `0x80002e64` | 512 | `role_switch_packet_type_reset_and_log` | role-switch/eSCO-slot teardown-and-packet-type-reset — see `region_0x80000000` | high (decompiled+documented) |
| `0x800030a0` | 618 | `status_word_multiflag_link_event_dispatcher2` | sibling status-word multi-flag link-event dispatcher — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000333c` | 490 | `select_packet_type_and_renegotiate_or_log` | packet-type-select-and-renegotiate variant — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000355c` | 252 | `role_switch_esco_mode_dispatch_gate` | role-switch/eSCO-mode dispatch gate — see `region_0x80000000` | high (decompiled+documented) |
| `0x80003674` | 1534 | `rf_channel_freq_word_programmer_for_esco_mode` | RF channel/frequency-word programmer for SCO/eSCO mode transitions — see `region_0x80000000` | high (decompiled+documented) |
| `0x80003cc0` | 74 | `status_bit_gated_role_state_logger_dispatch` | status-bit dispatch to role-state logger leaves — see `region_0x80000000` | high (decompiled+documented) |
| `0x800045b4` | 36 | `conditional_feature_gated_init_wrapper` | conditional feature-gated init wrapper — see `region_0x80000000` | high (decompiled+documented) |
| `0x800045e0` | 182 | `link_status_bit_dispatch_for_role_state_notify` | link-status-bit dispatcher for role-state notify — see `region_0x80000000` | high (decompiled+documented) |
| `0x800046b8` | 312 | `conn_event_packet_type_update_and_reschedule` | per-connection-event packet-type-refresh-and-reschedule — see `region_0x80000000` | high (decompiled+documented) |
| `0x80004820` | 1230 | `conn_teardown_and_link_loss_cleanup_handler` | top-level connection-teardown/link-loss cleanup handler — see `region_0x80000000` | high (decompiled+documented) |
| `0x80004d74` | 124 | `batch_conn_teardown_and_packet_type_sweep` | batch dispatcher sweeping conn-teardown+packet-type-update across handles — see `region_0x80000000` | high (decompiled+documented) |
| `0x80004df8` | 148 | `bitmask_sweep_dispatch_to_8003e760_3entry` | 3-slot bitmask sweep dispatcher — see `region_0x80000000` | high (decompiled+documented) |
| `0x80004e9c` | 130 | `bitmask_sweep_dispatch_to_8003e98c_3entry` | 3-slot bitmask sweep dispatcher (shifted bit range) — see `region_0x80000000` | high (decompiled+documented) |
| `0x80004f28` | 140 | `role_index_dispatch_or_log_0x2cd` | role-index dispatch or log event 0x2cd — see `region_0x80000000` | high (decompiled+documented) |
| `0x80008a7c` | 22 | `conditional_feature_bit_enable_0x15` | conditional feature-bit enable (status==0x15) — see `region_0x80000000` | high (decompiled+documented) |
| `0x80008a94` | 16 | `feature_bit_enable_helper_v1` | feature-bit enable helper — see `region_0x80000000` | high (decompiled+documented) |
| `0x80008aa4` | 16 | `feature_bit_disable_helper_v1` | feature-bit disable helper — see `region_0x80000000` | high (decompiled+documented) |
| `0x80008ab4` | 16 | `feature_bit_enable_helper_v2` | feature-bit enable helper variant — see `region_0x80000000` | high (decompiled+documented) |
| `0x80008ac4` | 16 | `feature_bit_disable_helper_v2` | feature-bit disable helper variant — see `region_0x80000000` | high (decompiled+documented) |
| `0x80008ad4` | 24 | `program_feature_bit_0x200_pair2` | programs feature-bit register with bit 1 set — see `region_0x80000000` | high (decompiled+documented) |
| `0x80008aec` | 64 | `program_feature_bit_0x100_and_log` | programs feature-bit 0x100 and logs on failure — see `region_0x80000000` | high (decompiled+documented) |
| `0x80008b30` | 62 | `program_feature_bit_0x80_and_log` | programs feature-bit 0x80 and logs on failure — see `region_0x80000000` | high (decompiled+documented) |
| `0x80008b74` | 48 | `program_feature_bit_0x1_and_log` | programs feature-bit 0x1 and logs on failure — see `region_0x80000000` | high (decompiled+documented) |
| `0x80008ba8` | 56 | `program_feature_bit_0x200_and_log` | programs feature-bit 0x200 and logs on failure — see `region_0x80000000` | high (decompiled+documented) |
| `0x80008be8` | 56 | `program_feature_bit_0x400_and_log` | programs feature-bit 0x400 and logs on failure — see `region_0x80000000` | high (decompiled+documented) |
| `0x80008c28` | 50 | `program_feature_bit_0x1000_and_log` | programs feature-bit 0x1000 and logs on failure — see `region_0x80000000` | high (decompiled+documented) |
| `0x80008c60` | 50 | `program_feature_bit_0x2000_and_log` | programs feature-bit 0x2000 and logs on failure — see `region_0x80000000` | high (decompiled+documented) |
| `0x80008c98` | 56 | `program_feature_bit_0x4000_and_log` | programs feature-bit 0x4000 and logs on failure — see `region_0x80000000` | high (decompiled+documented) |
| `0x80008cd8` | 56 | `program_feature_bit_0x8000_and_log` | programs feature-bit 0x8000 and logs on failure — see `region_0x80000000` | high (decompiled+documented) |

---

## Unnamed functions (2114, originally 2278)

The remaining 2114 functions in the `rom` block (2739 total − 624 named − 1
reclassified non-function) carry Ghidra's auto-generated `FUN_8000xxxx`
label and have not been individually triaged. Per Phase 9 scoping, giving
each of these a real name and purpose is explicitly out of scope for this
doc — it's the rest of Phase 9's ongoing, best-effort work. This section
satisfies "every function" at the index/coverage level via aggregate stats
instead of 2278 rows of "unknown."

**Confidence: unanalyzed** (for all 2114, as a single flag — no further
granularity is meaningful until individual triage happens).

### Address-range distribution (by 0x10000-aligned region)

| Address range | Unnamed function count | % of unnamed total |
|---|---|---|
| `0x80000000`–`0x8000ffff` | 143 (307 − 12 pass-1 − 19 pass-2 − 74 pass-3 − 27 pass-4 − 31 pass-5 − 1 reclassified non-function, 2026-06-22) | 6.8% |
| `0x80010000`–`0x8001ffff` | 268 | 12.7% |
| `0x80020000`–`0x8002ffff` | 321 | 15.2% |
| `0x80030000`–`0x8003ffff` | 290 | 13.7% |
| `0x80040000`–`0x8004ffff` | 307 | 14.5% |
| `0x80050000`–`0x8005ffff` | 354 | 16.7% |
| `0x80060000`–`0x8006ffff` | 238 | 11.3% |
| `0x80070000`–`0x8007ffff` | 193 | 9.1% |
| **Total** | **2114** | **100%** |

Distribution is fairly even across the whole ROM outside the
`0x80000000`-`0x8000ffff` region (9.1–16.7% per 64 KiB region), which has
now dropped to 6.8% of the unnamed total thanks to 5 passes of Phase 9's
region sweep concentrating there; the other 7 regions are unchanged from
the 2026-06-21 baseline and still interleave named/unnamed functions
throughout, consistent with how Phases 1–8 worked (tracing call chains and
protocol handlers rather than sweeping linearly through address space).

### Size statistics

**Stale baseline (2026-06-21), not re-run this pass** — these aggregate
byte-level stats (count/total-bytes/average) require a fresh
`RomCoverageStats.java`-style pass over the now-shrunk unnamed set to be
accurate; the per-region distribution table above (manually recomputed each
pass from the named-table deltas) is the authoritative up-to-date count
(2114, not 2278) until that re-run happens.

| Metric | Value |
|--------|-------|
| Count | 2278 (stale; see note above — true current count is 2114) |
| Smallest function | 1 byte |
| Largest function | 2484 bytes |
| Total bytes across all unnamed functions | 356289 bytes (≈348 KiB) (stale) |
| Average size | 156.4 bytes (stale) |
| Address span | `0x80000000`–`0x80079934` |

Many of the very small (1–12 byte) entries are switch-table case stubs,
alignment padding, or single-instruction thunks auto-split by Ghidra's
analyzer rather than "real" hand-written functions — consistent with the
`caseD_*`/`switchdataD_*`/`thunk_FUN_*` auto-names seen during this doc's
cross-reference pass (those carry Ghidra-internal names, not
`SourceType.USER_DEFINED`, so they fall in the unnamed bucket here even
though their name isn't literally `FUN_*`).

---

## How to re-run

```python
mcp__wairz__run_ghidra_headless(
    binary_path="2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf",
    script_name="RomNamedFuncAddrs.java",
    use_saved_project=True,
    timeout=300,
)
mcp__wairz__run_ghidra_headless(
    binary_path="2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf",
    script_name="ExtractAnnotations.java",
    use_saved_project=True,
    timeout=300,
)
mcp__wairz__run_ghidra_headless(
    binary_path="2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf",
    script_name="ListAllFunctions.java",
    use_saved_project=True,
    timeout=300,
)
```

`RomNamedFuncAddrs.java` gives the authoritative 461-address set (filtered by
`SourceType.USER_DEFINED`/`IMPORTED`, not by name pattern — this is why it
correctly excludes Ghidra's own `thunk_FUN_*`/`switchdataD_*`/`caseD_*`
auto-names even though those don't match `FUN_*`). `ExtractAnnotations.java`
supplies the name string for each address (run across all blocks; filter to
the `0x8000xxxx`–`0x8007xxxx` range for ROM-only). `ListAllFunctions.java`
supplies sizes for every function in the program (filter the same way). All
three are read-only and safe to re-run at any time.

Cross-referencing each address against `analysis/rom/*.md`,
`analysis/reverse_engineering_*.md`, and `analysis/kovah_function_list.md`
(to assign the confidence flags) was done with a one-off Python pass over
the headless run's full output — not a saved Ghidra script, since it's a
text cross-reference over existing markdown, not a Ghidra operation. Re-run
by extracting `0x[0-9a-f]{8}`-shaped hex literals from each doc and checking
set membership against the address list above.

## Tool notes (carried forward from `rom_coverage_baseline.md`)

- `run_ghidra_headless` truncates its returned stdout at ~30 KB for
  larger scripts (`RomNamedFuncAddrs.java`'s 461-line output and
  `ExtractAnnotations.java`'s/`ListAllFunctions.java`'s full-program output
  all exceeded this). The **full untruncated output is still written to
  Ghidra's `application.log`** inside the wairz container's overlay
  filesystem (path varies per run — locate via
  `find / -name application.log -newer <recent-file>`), and can be read
  directly with `grep`/`sed` keyed on the script's `=== ..._START ===` /
  `=== ..._END ===` markers. This is how this doc's full 461-row table and
  2739-row size list were recovered without needing a new script.
- `save_ghidra_script` overwrites of an existing filename don't take
  effect, and the 50-slot cap is full with no delete/evict tool — this doc
  was produced entirely from existing generic scripts
  (`RomNamedFuncAddrs.java`, `ExtractAnnotations.java`,
  `ListAllFunctions.java`), no new script was needed or attempted.
