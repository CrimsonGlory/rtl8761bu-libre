# ROM Function Index

> **⚠️ KNOWN ISSUE (2026-06-25, unresolved — see `wairz_requested_changes.txt`):**
> Function renames applied via `mcp__wairz__run_ghidra_headless` (GZF process mode) do
> **not** persist to the project state read by `list_functions`/`decompile_function`/etc.
> Confirmed by direct testing: re-running a previously-"applied" rename script
> (`RenamePass9Region80050000.java`, targeting `0x8005b79c` → `release_connection_record`)
> reports `RENAMED`/`RENAME COMPLETE`/`Save succeeded`, but an immediate
> `decompile_function` call — even after a full forced re-analysis
> (`start_binary_analysis(force_reanalyze=true)`) — still shows the function under its
> original `FUN_8005b79c` name. **This means many/most "renamed, HIGH confidence" rows
> below may not actually exist as symbol names in the live Ghidra project**, even though
> the underlying decompiled-code evidence and reasoning recorded for them is real. Until
> wairz fixes this, do not assume a name in this table resolves via
> `list_functions`/`decompile_function` — verify independently if it matters, and treat
> this table (not the GZF) as the authoritative address→name mapping.

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
| Named functions (this doc's table) | 752 (690 + 3 region-0x80050000 Pass-6 + 1 region-0x80050000 Pass-7 + 1 region-0x80050000 Pass-9 + 10 region-0x80030000 Pass-3 + 5 region-0x80030000 Pass-5 + 6 region-0x80030000 Pass-7 + 20 region-0x80030000 Pass-8 + 16 region-0x80020000 Pass-3, as of 2026-06-25; regions 0x80000000/0x80020000/0x80030000/0x80060000 sweep complete, 0x80010000/0x80050000 in progress) |
| Unnamed (`FUN_*`) functions (summarized below) | 1754 (2012 − 238 region-0x80060000 Pass-2-3 renames − 20 region-0x80030000 Pass-8 renames, 2026-06-25) |
| Named-function confidence: **high** (decompiled + written up in a dedicated `rom/*.md`) | 548 (502 + 20 region-0x80020000 Pass-5 + 6 region-0x80030000 Pass-7 + 20 region-0x80030000 Pass-8 newly-decompiled, 2026-06-25) |
| Named-function confidence: **medium** (named, one-line purpose only, not decompiled) | 38 (39 − 1 region-0x80020000 Pass-5 `0x80022030` medium→high upgrade, 2026-06-25) |
| Named-function confidence: **low** (named by Kovah, purpose unclear) | 237 (256 − 19 region-0x80020000 Pass-5 low→high upgrades, 2026-06-25) |

**Known pre-existing tally drift (carried, not introduced this pass)**: high+medium+low = 361+68+257 = 686, one more than the 685 named-functions total. The same +1 drift already existed at the pass-1 baseline (323+88+271=682 vs 681 named) — this pass's edits are arithmetically consistent deltas on top of that baseline, not a new miscount. Flagging per the doc's standing practice rather than silently correcting an unverified pre-existing baseline; a future pass should audit the full named-function table's confidence column against a fresh count to locate the single double-counted or missing row.

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

**2026-06-22 update (pass 6, same-day continuation)**: resolved 13 more
functions in the same region (`0x80000000`-`0x8000ffff`), targeting the
`0x80004fd8`-`0x80006cb8` head of the stretch flagged as highest-value since
pass 5. The single biggest find: `top_level_link_event_status_dispatcher_loop`
(`0x80004fd8`) — the master status-word-driven dispatcher loop that the
entire packet-type/role-switch/eSCO/conn-teardown supercluster (passes 2-5)
funnels into, confirming the whole supercluster is one cohesive link-event
state machine rooted at this single function. The other 12 resolved this
pass form a related but distinct "apply packet-type/codec change and log
it" layer (SCO/eSCO slot-timing-offset calculators, a packet-type-apply-
plus-codec-table-sync routine, and 2 status-word state-machine dispatchers)
— NOT more siblings of the role-switch theme as pass 5 had hypothesized for
this stretch. See `rom/reverse_engineering_region_0x80000000.md`'s
"Resolved functions — pass 6" section for full per-function evidence,
including a flagged **open arithmetic discrepancy** (the thin-named bucket
inherited from pass 4/5 appears to be undercounted by 2 — not introduced
this pass, flagged for the next pass to re-derive directly rather than
carry forward unverified). Reconciled total: **159 of 220 in-scope gap
functions resolved**, **61 remain** (42 still-unnamed + 16 thin-named
tracked, see caveat above re: possible undercount; 2 of the 16 already
"high confidence" via other docs). The 13 new rows are appended after the
pass-5 rows below.

**2026-06-22 update (pass 7, same-day continuation)**: first re-derived
pass 6's flagged tally discrepancy from scratch (per the ticket's explicit
instruction not to defer it a third time) — root cause found: **15
already-resolved, already-"high"-confidence functions**
(`0x80008a7c`-`0x80008cd8`, the feature-bit-enable/disable/program cluster)
had simply never been tabulated into `reverse_engineering_region_0x80000000.md`'s
own pass-N tables, so a table-scrape-based recount undercounted the true
resolved total by exactly 15. The pre-existing "16 genuinely-open
thin-named" figure was correct all along and needed no correction. See
that doc's "Tally reconciliation (pass 7)" section for the full
address-by-address derivation. Then resolved 12 more functions, completing
the entire `0x80004fd8`-`0x800085a4` stretch pass 6 left partially open —
led with the 4 functions pass 6 flagged as having known caller context
(`0x80007af0`/`0x800083ec`/`0x800085a4` as direct callees of
`top_level_link_event_status_dispatcher_loop`, `0x800066fc` as a callee of
`batch_conn_status_word_sweep_3entry`) and found `0x800085a4` to be a
**second top-level master status-word dispatcher**
(`top_level_link_event_status_dispatcher_loop2`), structurally parallel to
pass 6's find but keyed off a different status-word pair and feeding a
largely-disjoint handler set — see that doc's new open-question #9 on the
two loops' unresolved relationship. Reconciled total: **171 of 220
in-scope gap functions resolved**, **49 remain** (30 still-unnamed + 16
genuinely-open thin-named + 2 thin-named already "high confidence" + 1
non-function — arithmetic fully closed, no open items). The 12 new rows
are appended after the pass-6 rows below.

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

**2026-06-22 update (pass 9, same-day continuation — region 0x80000000
gap-sweep COMPLETE)**: resolved the final 26 functions in this region's
two-gap scope: the `0x8000f4a0`-`0x8000fb20` 9-function stretch, all 8
remaining small isolated `FUN_*` functions, the 1398B `0x8000aa64`
(carried since pass 3), and all 16 pre-existing genuinely-open thin-named
functions (8 renamed for clarity, e.g. `unknown_referencing_default_name_3/4/5`
→ the `codec_mode_*`/`codec_config_*` cluster, `unknown_fptr_index9` →
`mailbox_message_dispatcher_index9`; the other 8 kept their existing
Kovah names, now fully decompiled/evidenced rather than renamed, since
those names were already accurate). Most notably identified
`lmp_pdu_received_top_level_processor` (`0x80003d10`, 2044B) as **the
single largest function in the entire region** — a central orchestrator
calling into nearly every cluster passes 1-8 had named. Post-pass
`ListRegion0x80000_Gaps.java` re-run confirms **zero `DEFAULT`/`FUN_*`
entries remain in the 220-function gap scope — every single entry is now
`USER_DEFINED`**. Final tally: **219 of 220 real functions resolved + 1
confirmed non-function (`0x8000046c`) = 220, with 0 unresolved.** This
region's gap-sweep ticket is complete; see
`reverse_engineering_region_0x80000000.md`'s pass-9 section and updated
"Remaining scope" for full detail. The 26 new/upgraded rows are folded
into the table below (most appear near their pass-1-8 siblings rather than
strictly appended, to keep related functions co-located).

**2026-06-22 update (region 0x80010000 pass 1, new region started)**: with
`0x80000000`-`0x8000ffff` complete, started the next region in the Phase 9
sweep order: `0x80010000`-`0x8001ffff` (the largest thin-named bucket of
any region, 130 thin-named per the original estimate). A fresh
authoritative listing (new scripts `ListRegion0x80010000.java` +
`ListRegion0x80010000_Upper.java`, both saved this pass) found 408 total
functions in-range: 15 already-high-confidence, 64 medium + 66 low
thin-named (130 total, matching the original estimate), and 263 unnamed
(vs. the original 268 estimate — small drift from an earlier index
snapshot). Pass 1 resolved the **`send_evt_HCI_*` event-sender cluster**,
`0x8001ca94`-`0x8001da3c`: 30 thin-named functions confirmed-by-decompile
(8 medium→high, 22 low→high) plus 2 unnamed leaf helpers renamed
(`rssi_threshold_delta_for_bos_index`/`0x8001cfc4`,
`log_hci_evt_0x1fc_if_no_patch3`/`0x8001d03c`) — 32 resolved total. Found
and confirmed the shared TX primitive `hci_event_sender` (`0x8001d070`,
already high-confidence) is called by every function in the cluster;
discovered Kovah used the identical name `send_evt_HCI_Connection_Complete`
for two structurally-different functions (`0x8001d1f8` simple path,
`0x8001d844` role-switch-aware path) — both confirmed real, not a
duplicate/typo. See `reverse_engineering_region_0x80010000.md` for full
per-function detail, the "Remaining scope" section's prioritized next
targets (an apparent OGF=3 vendor-command opcode-handler cluster is the
top recommendation), and the region's own scoping/tally notes. The 32
new/upgraded rows are appended after the region-0x80000000 pass-9 rows
below.

**2026-06-22 update (region 0x80010000 pass 2)**: resolved pass 1's
top-priority recommended target, the **`OGC_3_OCF_*` cluster** —
38 functions (34 thin-named upgraded to high via decompile-confirmation:
20 medium + 14 low; 4 newly named from unnamed `FUN_*`). Decisive finding:
decompiling the standard-router's OGF=3 dispatcher
(`HCI_CMD_OGF_03__Controller_and_Baseband__big_switch_FUN_800202c0`,
`0x800202c0`, region `0x80020000`, already high-confidence) showed its
`CALLEES` list is exactly this region's `OGC_3_OCF_*` functions — so this
is the **already-documented standard OGF 0x03 (Controller & Baseband)**
command group (per `hci_command_router.md`'s OGF table), not a
previously-unexplored vendor opcode group as pass 1 had hypothesized;
Realtek simply placed the OCF-handler bodies in this region while the
dispatcher switch lives in `0x80020000`. Also discovered
`unknown_referencing_default_name_7` (`0x8001f408`, 586B) is the real
`HCI_Reset` (OGF=3/OCF=3) implementation — renamed
`fHCI_Reset_0x03_full_subsystem_teardown`. Renamed 3 more from `FUN_*`:
`OGF1_3_extended_OCF_0x51_0x5b_fallback_handler` (`0x8001a658`),
`OGC_3_OCF_67_vendor_ext_write_byte_param` (`0x8001a838`, opcode `0xc68`
confirmed via decompile), `OGC_3_OCF_62_vendor_ext_set_conn_flag_via_
FUN_80017930` (`0x8001a128`, opcode not fully confirmed — flagged for a
future pass). See `reverse_engineering_region_0x80010000.md`'s "Pass 2"
section for full per-function detail and the updated "Remaining scope"
priority list (now led by the `HCI_OGF1_OCF0x4#` cluster,
`0x8001c490`-`0x8001c788`). The 38 new/upgraded rows are folded into the
table below near their pass-1 siblings (not strictly appended).

**2026-06-24 update (region 0x80030000 Pass 5)**: full decompile review of
the 6 tier-1 candidates flagged by Pass 4's cold-triage. 5 functions renamed
from `FUN_*` to real names (`apply_SCO_connection_params_to_hw`/`0x8003d7bc`,
`validate_connection_setup_preconditions`/`0x80033f8c`,
`apply_LAP_derived_hopping_params`/`0x8003cb80`,
`release_SCO_connection_resources`/`0x8003ec48`,
`apply_eSCO_SCO_packet_type_params`/`0x80037e28`), all HIGH confidence; the
6th candidate (`0x80032540`, `multi-VSC_Handler_FUN_80032540`) was confirmed
already correctly named from a prior pass and upgraded from low to HIGH
confidence on full-decompile confirmation (12-opcode secondary VSC
dispatcher). See `reverse_engineering_region_0x80030000.md`'s "Pass 5"
section for full per-function evidence. Region coverage: 27 → 32 of 309
functions named (8.7% → 10.4%). The 5 new rows are folded into the table
below near their region-0x80030000 siblings (not strictly appended).

---

## Named functions (688, was 461 at the 2026-06-21 baseline)

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
| `0x800009c0` | 150 | `link_status_bit_escalation_dispatcher_v3` | status-bit-escalation-ladder dispatcher (3rd variant), config-feature-bit gated — see `region_0x80000000` | high (decompiled+documented) |
| `0x80003d10` | 2044 | `lmp_pdu_received_top_level_processor` | the largest function in the region — top-level connection-event/LMP-PDU-received orchestrator tying together most of the region's named clusters — see `region_0x80000000` | high (decompiled+documented) |
| `0x80008d18` | 380 | `log_many_2_0x72_0x121-0x14e` | VSC-0xfca1-mask-driven 12-event conditional logger — see `region_0x80000000` | high (decompiled+documented) |
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
| `0x8000bd04` | 46 | `vsc_0xfc6c_indexed_reg_read_with_0xff_sentinel` | VSC 0xfc6c indexed register read with 0xff default/sentinel — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000bdb4` | 12 | `clear_bits_in_global_0xfc39_helper` | `*global &= ~param` bit-clear helper shared by VSC 0xfc39 handling — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000be84` | 26 | `set_or_clear_bits_in_global_0xfc39_helper` | set-or-clear-bits-in-global companion to the above, VSC 0xfc39 — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000c09c` | 178 | `codec_mode_select_and_audio_buffer_dispatch` | codec-mode-select dispatcher feeding the audio circular-buffer writer — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000c198` | 370 | `codec_config_param_table_initializer` | config-blob-driven audio/codec parameter-table setup routine — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000c390` | 86 | `codec_mode_apply_and_afh_toggle` | combined codec-mode-apply-and-AFH-toggle step, completes the codec-mode 3-function cluster — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000e85c` | 100 | `optimized_memcpy` | optimized memcpy — see `vsc_dispatcher` | high (decompiled+documented) |
| `0x8000e98c` | 64 | `memset` | memset — completes the region's memcpy/memmove/memset libc-style trio — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000e9cc` | 56 | `references_patch_download_mem2` | redirects a stale patch-image pointer to the real patch entry point `0x8010a000` — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000f0a4` | 806 | `multi_field_opcode_dispatcher_type1_msg` | central 6-bit-field-ID-routed message handler invoked by `mailbox_message_dispatcher_index9` — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000f41c` | 108 | `mailbox_message_dispatcher_index9` | single-slot mailbox/queue dispatcher keyed on message type, the actual "index9" fptr-table entry — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000f53c` | 64 | `wraps_multi_VSC_called_if_no_patch3` | installed-VSC-callback wrapper with conditional logging on failure — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000fae8` | 50 | `vsc_0xfc39_wrapper` | VSC 0xfc39 wrapper, dispatches to bit-clear/set helpers or a 3rd VSC handler by config field — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000fb5c` | 436 | `lots_of_initialization` | lots of initialization — see `boot_reset_sequence`, `interrupt_vectors`, `usb_transport_hci_driver` | high (decompiled+documented) |
| `0x8000fd38` | 120 | `copies_config_bdaddr` | clears reserved config-blob bits, memcmp+memcpy's BD_ADDR into the config blob if changed — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000046c` | 20 | **NOT A REAL FUNCTION** (was tentatively `usb_event_status_handler_dup` in pass 1) | zero-filled alignment padding before `0x80000480`, mis-split by Ghidra's analyzer; reclassified 2026-06-22 pass 2 — see `region_0x80000000` | n/a (non-function, excluded from named/unnamed counts) |
| `0x80000480` | 764 | `usb_event_status_handler` | USB-class event-status bit dispatcher, ends in USB-transport-drain calls — see `region_0x80000000`, `usb_transport_hci_driver` | high (decompiled+documented) |
| `0x80000a5c` | 410 | `connection_event_status_handler` | per-connection HW status-change handler (link loss/role/feature gating) — see `region_0x80000000` | high (decompiled+documented) |
| `0x80000c24` | 64 | `timer_callback_table_dispatcher_4entry` | fixed 4-entry callback-table dispatcher over a status byte — see `region_0x80000000` | high (decompiled+documented) |
| `0x80000c74` | 190 | `uart_rx_tx_byte_fifo_handler` | UART-style RX/TX circular-byte-FIFO ISR handler — see `region_0x80000000` | high (decompiled+documented) |
| `0x80000d78` | 492 | `isr_bottom_half_status_dispatcher` | ISR bottom-half/deferred-event dispatcher, reads CP0 Cause/EPC then branches to ~8 status-bit handlers — see `region_0x80000000`, `interrupt_vectors` | high (decompiled+documented) |
| `0x800011fc` | 188 | `conn_record_pending_data_drain` | linked-list byte-stream dequeue primitive over conn-record buffer pool — see `region_0x80000000`, `conn_record_subsystem` | high (decompiled+documented) |
| `0x800012b8` | 216 | `baseband_event_status_dispatcher_0xd` | 13-source baseband event dispatcher, parallel in shape to `FUN_80050810` — see `region_0x80000000`, `conn_type_dispatch_and_esco` | high (decompiled+documented) |
| `0x800007d0` | 68 | `escalating_feature_bit_enable_and_report` | enable-feature-and-report escalation ladder, called from `connection_event_status_handler` — see `region_0x80000000` | high (decompiled+documented) |
| `0x80000820` | 364 | `link_state_feature_bit_escalation_and_log_dispatcher` | larger link-state-gated escalation/log dispatcher, called from `isr_bottom_half_status_dispatcher` — see `region_0x80000000` | high (decompiled+documented) |
| `0x80008eac` | 80 | `vsc_param_apply_with_log_0x6b_0xce` | thin VSC-parameter-apply wrapper with logging on entry/failure — see `region_0x80000000` | high (decompiled+documented) |
| `0x80008f04` | 264 | `feature_bit_mask_dispatch_via_vsc_0xfca1` | VSC-0xfca1-mask-gated sibling dispatcher to the `program_feature_bit_0x*` cluster — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000dd00` | 404 | `afh_rf_reconfig_retry_state_machine` | 3-state retry/backoff state machine around AFH/RF-divider reconfiguration — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000fdc0` | 42 | `config_bit_gated_reg_field_write_idx0xb` | config-bit-gated baseband-register-field write at field index 0xb — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000fdf8` | 76 | `flag_gated_reg_field_write_idx0xb` | flag-gated companion register-field write at field index 0xb — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000fe4c` | 610 | `min_timing_credit_selector_5way` | 5-way minimum-timing/credit selector, richer sibling of `scheduler_find_next_min_deadline` — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000aa64` | 1398 | `pdu_type_dispatch_enqueue_to_per_type_ring_and_notify` | central incoming-PDU type-dispatch-and-enqueue function, enqueue-side sibling to `conn_record_pending_data_drain` — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000f4a0` | 58 | `clear_state_and_dispatch_reset_table_entry` | state-reset-and-dispatch primitive, IRQ-bracketed — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000f4f4` | 56 | `counter_threshold_gated_reset_trigger` | counter-driven reset trigger feeding the above — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000f584` | 96 | `conditional_table_entry_registration_init` | conditional fptr-table-entry registration via `interesting_string_user_fptr_registration_function` — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000f628` | 32 | `shift_amount_from_2bit_field_calc` | trivial 2-bit-field-to-shift-amount leaf calc — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000f658` | 562 | `multi_condition_readiness_status_checker` | ~10-condition bitmask-accumulating eligibility checker — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000f8bc` | 336 | `secondary_readiness_check_and_param_capture` | second-stage readiness check + timing/RF parameter capture — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000fa48` | 86 | `config_derived_shift_and_threshold_setter` | config-blob-derived shift-exponent + threshold-byte setter — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000fab0` | 50 | `clear_reg0x100_bit15_if_bit6_set` | conditional baseband-register-0x100 bit-clear wrapper — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000fb20` | 50 | `log_event_0x3ee_if_status_not_idle` | conditional-log helper, event 0x3ee unless status is idle-sentinel — see `region_0x80000000` | high (decompiled+documented) |
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
| `0x80019ad0` | 172 | `OGC_3_OCF_3f` | OGC 3 OCF 3f | high (decompiled+documented — see `region_0x80010000`) |
| `0x80019b88` | 32 | `OGC_3_OCF_49` | OGC 3 OCF 49 | high (decompiled+documented — see `region_0x80010000`) |
| `0x80019bac` | 32 | `OGC_3_OCF_45` | OGC 3 OCF 45 | high (decompiled+documented — see `region_0x80010000`) |
| `0x80019bd0` | 32 | `OGC_3_OCF_47` | OGC 3 OCF 47 | high (decompiled+documented — see `region_0x80010000`) |
| `0x80019bf4` | 116 | `OGC_3_default_func_0_OCF_0x3F_and_above` | OGC 3 default func 0 OCF 0x3F and above | high (decompiled+documented — see `region_0x80010000`) |
| `0x80019c88` | 104 | `deal_with_OGF_3_OCF_0x3f-0x49` | deal with OGF 3 OCF 0x3f-0x49 | high (decompiled+documented — see `region_0x80010000`) |
| `0x80019d80` | 202 | `fHCI_Setup_Synchronous_Connection?` | fHCI Setup Synchronous Connection? | low (named by Kovah, purpose unclear) |
| `0x80019e4c` | 60 | `send_evt_HCI_Read_Remote_Extended_Features_Complete` | send evt HCI Read Remote Extended Features Complete | medium (named, one-line purpose only, not decompiled) |
| `0x80019e88` | 124 | `send_evt_HCI_Synchronous_Connection_Changed` | send evt HCI Synchronous Connection Changed | medium (named, one-line purpose only, not decompiled) |
| `0x80019f0c` | 232 | `send_evt_HCI_Synchronous_Connection_Complete` | send evt HCI Synchronous Connection Complete | medium (named, one-line purpose only, not decompiled) |
| `0x8001a0f8` | 44 | `calls_to_VSC_0xfcc0` | calls to VSC 0xfcc0 | low (named by Kovah, purpose unclear) |
| `0x8001a128` | 86 | `OGC_3_OCF_62_vendor_ext_set_conn_flag_via_FUN_80017930` | renamed from `FUN_8001a128`; OCF number not fully opcode-confirmed this pass (flagged) — see `region_0x80010000` | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001a294` | 174 | `OGC_3_OCF_0x52_HCI_Write_Extended_Inquiry_Response_fills_0x300_then_calls_to_VSC_0xfcc0` | OGC 3 OCF 0x52 HCI Write Extended Inquiry Response fills 0x300 then calls to VSC 0xfcc0 | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001a350` | 164 | `OGC_3_OCF_0x51_and_above_path_to_VSC_0xfcc0` | OGC 3 OCF 0x51 and above path to VSC 0xfcc0 | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001a420` | 56 | `send_evt_HCI_Remote_Host_Supported_Features_Notification` | send evt HCI Remote Host Supported Features Notification | low (named by Kovah, purpose unclear) |
| `0x8001a458` | 34 | `send_evt_HCI_Enhanced_Flush_Complete` | send evt HCI Enhanced Flush Complete | low (named by Kovah, purpose unclear) |
| `0x8001a47c` | 42 | `send_evt_HCI_Link_Supervision_Timeout_Changed` | send evt HCI Link Supervision Timeout Changed | low (named by Kovah, purpose unclear) |
| `0x8001a4a8` | 268 | `send_evt_HCI_Sniff_Subrating` | send evt HCI Sniff Subrating | low (named by Kovah, purpose unclear) |
| `0x8001a5b8` | 158 | `send_evt_HCI_Extended_Inquiry_Result` | send evt HCI Extended Inquiry Result | low (named by Kovah, purpose unclear) |
| `0x8001a658` | 346 | `OGF1_3_extended_OCF_0x51_0x5b_fallback_handler` | renamed from `FUN_8001a658`; generic per-opcode fallback for OCF 0x51-0x5b range — see `region_0x80010000` | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001a838` | 90 | `OGC_3_OCF_67_vendor_ext_write_byte_param` | renamed from `FUN_8001a838`; opcode `0xc68` confirmed via decompile — see `region_0x80010000` | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001a898` | 66 | `OGC_3_default_func_2` | OGC 3 default func 2 | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001a9b8` | 52 | `bit_test_[bit_index_at_offset_0x16f]_within_[short_at_offset_0x24]` | bit test [bit index at offset 0x16f] within [short at offset 0x24] | low (named by Kovah, purpose unclear) |
| `0x8001aa3c` | 254 | `LMP_QUALITY_OF_SERVICE_REQ_0x2A` | LMP QUALITY OF SERVICE REQ 0x2A — see `lc_lmp_state_machine` | high (decompiled+documented) |
| `0x8001af9c` | 114 | `LMP_0x18_LMP_UNSNIFF_REQ` | LMP 0x18 LMP UNSNIFF REQ — see `lc_lmp_state_machine` | high (decompiled+documented) |
| `0x8001b23c` | 122 | `fHCI_Read_LMP_Handle_0x20` | fHCI Read LMP Handle 0x20 | medium (named, one-line purpose only, not decompiled) |
| `0x8001b2c0` | 170 | `fHCI_Read_Clock_Offset_0x1F` | fHCI Read Clock Offset 0x1F | medium (named, one-line purpose only, not decompiled) |
| `0x8001b370` | 354 | `fHCI_Read_Remote_Version_Information_0x1D_send_LMP_VERSION_REQ_0x25` | HCI Read Remote Version Information command handler; initiates LMP VERSION_REQ PDU (opcode 0x25) via ROM connection-record lookup. See `region_0x80010000`, PASS 6 | high (decompiled+documented) |
| `0x8001b4e8` | 96 | `fHCI_Read_Remote_Supported_Features_0x1B` | fHCI Read Remote Supported Features 0x1B | medium (named, one-line purpose only, not decompiled) |
| `0x8001b54c` | 496 | `fHCI_Remote_Name_Request_0x19_send_LMP_NAME_REQ_0x01` | fHCI Remote Name Request 0x19 — thin wrapper sends LMP NAME REQ 0x01 with error handling | high (decompiled+documented) |
| `0x8001b84c` | 170 | `fHCI_Change_Connection_Packet_Type_0x0F` | fHCI Change Connection Packet Type 0x0F — HCI command handler; updates connection record + calls ROM encryption/state checkers; (0x040f) | high (decompiled+documented, PASS 5) |
| `0x8001b8fc` | 204 | `fHCI_Add_SCO_Connection_DEPRECATED_0x07` | fHCI Add SCO Connection DEPRECATED 0x07 — deprecated BT command handler; initializes SCO params + sends status/complete events (0x0407) | high (decompiled+documented, PASS 5) |
| `0x8001b9d4` | 258 | `fHCI_Disconnect_0x06` | fHCI Disconnect 0x06 — thin wrapper sends LMP DETACH with error handling | high (decompiled+documented) |
| `0x8001baf8` | 190 | `fHCI_Reject_Connection_Request_0x0A` | fHCI Reject Connection Request 0x0A — thin wrapper sends LMP NOT ACCEPTED 0x0d with error handling | high (decompiled+documented) |
| `0x8001bbbc` | 360 | `fHCI_Accept_Connection_Request_0x09` | fHCI Accept Connection Request 0x09 — thin wrapper sends LMP ACCEPTED with error handling | high (decompiled+documented) |
| `0x8001bd38` | 512 | `fHCI_Create_Connection_0x05` | fHCI Create Connection 0x05 — see `lc_lmp_state_machine` | high (decompiled+documented) |
| `0x8001bf44` | 88 | `fHCI_Periodic_Inquiry_Mode_0x03` | fHCI Periodic Inquiry Mode 0x03 — thin wrapper; parses periodic inquiry params + delegates to ROM handler (0x0403) | high (decompiled+documented, PASS 5) |
| `0x8001bfa0` | 50 | `fHCI_Inquiry_0x01` | fHCI Inquiry 0x01 — see `lc_lmp_state_machine` | high (decompiled+documented) |
| `0x8001c324` | 252 | `set_check_for_1_to_1` | set check for 1 to 1 | low (named by Kovah, purpose unclear) |
| `0x8001c438` | 76 | `OGC_3_default_func_4` | OGC 3 default func 4 | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001c490` | 186 | `HCI_OGF1_OCF0x44` | HCI OGF1 OCF0x44 | low (named by Kovah, purpose unclear) |
| `0x8001c550` | 32 | `HCI_OGF1_OCF0x43` | HCI OGF1 OCF0x43 | low (named by Kovah, purpose unclear) |
| `0x8001c574` | 304 | `HCI_OGF1_OCF0x42` | HCI OGF1 OCF0x42 | low (named by Kovah, purpose unclear) |
| `0x8001c6b8` | 204 | `HCI_OGF1_OCF0x41` | HCI OGF1 OCF0x41 | low (named by Kovah, purpose unclear) |
| `0x8001c788` | 38 | `fHCI_Truncated_Page_Cancel_0x40` | fHCI Truncated Page Cancel 0x40 | medium (named, one-line purpose only, not decompiled) |
| `0x8001c7b4` | 382 | `fHCI_Truncated_Page_0x3F` | fHCI Truncated Page 0x3F — thin wrapper sends LMP with error handling | high (decompiled+documented) |
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
| `0x8001dc10` | 2454 | `OGC_3_OCF_TONS_deal_with_return_status_referencing_default_name_10` | OGC 3 OCF TONS deal with return status referencing default name 10 | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001e5d8` | 52 | `send_evt_HCI_Command_Status` | send evt HCI Command Status | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001e610` | 52 | `OGC_3_OCF_01` | OGC 3 OCF 01 | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001e648` | 48 | `OGC_3_OCF_33` | OGC 3 OCF 33 | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001e67c` | 12 | `OGC_3_OCF_2a` | OGC 3 OCF 2a | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001e68c` | 20 | `OGC_3_OCF_2f` | OGC 3 OCF 2f | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001e6a4` | 20 | `OGC_3_OCF_31` | OGC 3 OCF 31 | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001e6bc` | 56 | `OGC_3_OCF_13_referencing_default_name` | OGC 3 OCF 13 referencing default name | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001e6fc` | 44 | `OGC_3_OCF_16` | OGC 3 OCF 16 | medium (named, one-line purpose only, not decompiled) |
| `0x8001e72c` | 22 | `OGC_3_OCF_18` | OGC 3 OCF 18 | medium (named, one-line purpose only, not decompiled) |
| `0x8001e748` | 20 | `OGC_3_OCF_3c` | OGC 3 OCF 3c | low (named by Kovah, purpose unclear) |
| `0x8001e760` | 28 | `OGC_3_OCF_3e` | OGC 3 OCF 3e | low (named by Kovah, purpose unclear) |
| `0x8001e780` | 4 | `HCI_Read_Loopback_Mode` | HCI Read Loopback Mode — see `hci_command_router` | high (decompiled+documented) |
| `0x8001e784` | 28 | `HCI_Enable_Device_Under_Test_Mode` | HCI Enable Device Under Test Mode | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001ea34` | 264 | `HCI_Write_Loopback_Mode` | HCI Write Loopback Mode — see `hci_command_router` | high (decompiled+documented) |
| `0x8001eb50` | 88 | `OGC_3_OCF_28` | OGC 3 OCF 28 | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001ebac` | 34 | `OGC_3_OCF_27` | OGC 3 OCF 27 | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001ebd0` | 78 | `OGC_3_OCF_36` | OGC 3 OCF 36 | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001ec20` | 180 | `OGC_3_OCF_37` | OGC 3 OCF 37 | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001ecd8` | 180 | `OGC_3_OCF_26` | OGC 3 OCF 26 | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001ed98` | 68 | `OGC_3_OCF_24` | OGC 3 OCF 24 | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001ede4` | 74 | `OGC_3_OCF_1e` | OGC 3 OCF 1e | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001ee34` | 106 | `OGC_3_OCF_1c` | OGC 3 OCF 1c | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001eea4` | 90 | `OGC_3_OCF_1a` | OGC 3 OCF 1a | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001ef54` | 314 | `OGC_3_OCF_35` | OGC 3 OCF 35 | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001f098` | 210 | `OGC_3_OCF_2d` | OGC 3 OCF 2d | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001f184` | 164 | `OGC_3_OCF_3a` | OGC 3 OCF 3a | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001f230` | 120 | `OGC_3_OCF_08` | OGC 3 OCF 08 | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001f2ac` | 338 | `OGC_3_OCF_05` | OGC 3 OCF 05 | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001f408` | 586 | `fHCI_Reset_0x03_full_subsystem_teardown` | renamed from `unknown_referencing_default_name_7`; real `HCI_Reset` (OGF=3/OCF=3) implementation — see `region_0x80010000` | high (decompiled+documented — see `region_0x80010000`) |
| `0x8001f94c` | 116 | `OGC_3_default_func_5` | OGC 3 default func 5 | high (decompiled+documented — see `region_0x80010000`) |
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
| `0x80022030` | 86 | `LMP__266__FUN_80022030` | LMP__266__FUN_80022030: utility function (partial decompile output) — see region_0x80020000 Pass 5 | **high (decompiled+documented, Pass 5 2026-06-25, partial output)** |
| `0x8002271c` | 60 | `send_evt_HCI_Encryption_Change[v1]` | send_evt_HCI_Encryption_Change[v1]: thin wrapper, calls hci_event_sender | **high (decompiled+documented, Pass 5 2026-06-25)** |
| `0x8002275c` | 52 | `send_evt_HCI_Change_Connection_Link_Key_Complete` | send_evt_HCI_Change_Connection_Link_Key_Complete: thin wrapper, calls hci_event_sender | **high (decompiled+documented, Pass 5 2026-06-25)** |
| `0x80022794` | 278 | `send_evt_HCI_Link_Key_Notification` | send_evt_HCI_Link_Key_Notification: link-key PDU build + logging, calls hci_event_sender | **high (decompiled+documented, Pass 5 2026-06-25)** |
| `0x800228b4` | 52 | `send_evt_HCI_Authentication_Complete_0x06` | send_evt_HCI_Authentication_Complete_0x06: thin wrapper, calls hci_event_sender | **high (decompiled+documented, Pass 5 2026-06-25)** |
| `0x800228ec` | 100 | `send_evt_HCI_Return_Link_Keys` | send_evt_HCI_Return_Link_Keys: multi-link pack-loop, calls hci_event_sender | **high (decompiled+documented, Pass 5 2026-06-25)** |
| `0x80022c40` | 66 | `send_evt_HCI_PIN_Code_Request` | send_evt_HCI_PIN_Code_Request: BD_ADDR extract + ROM notify (FUN_8001d03c), calls hci_event_sender | **high (decompiled+documented, Pass 5 2026-06-25)** |
| `0x80022eec` | 104 | `many_sub_if_else_cases_on_param2` | many sub if else cases on param2 | low (named by Kovah, purpose unclear) |
| `0x80022f54` | 66 | `send_evt_HCI_Link_Key_Request` | send_evt_HCI_Link_Key_Request: BD_ADDR extract + ROM notify (FUN_8001d03c), calls hci_event_sender | **high (decompiled+documented, Pass 5 2026-06-25)** |
| `0x800233e8` | 400 | `HCI_Write_Simple_Pairing_Debug_Mode` | HCI Write Simple Pairing Debug Mode — see `hardware_layer` | high (decompiled+documented) |
| `0x80023ba4` | 52 | `send_evt_HCI_Encryption_Key_Refresh_Complete` | send_evt_HCI_Encryption_Key_Refresh_Complete: thin wrapper, calls hci_event_sender | **high (decompiled+documented, Pass 5 2026-06-25)** |
| `0x80023c4c` | 64 | `send_evt_HCI_Keypress_Notification` | send_evt_HCI_Keypress_Notification: large2-struct dispatch, calls hci_event_sender | **high (decompiled+documented, Pass 5 2026-06-25)** |
| `0x80023c90` | 52 | `send_evt_HCI_Simple_Pairing_Complete_0x36` | send_evt_HCI_Simple_Pairing_Complete_0x36: BD_ADDR + status pack, calls hci_event_sender | **high (decompiled+documented, Pass 5 2026-06-25)** |
| `0x80023cc8` | 72 | `send_evt_HCI_IO_Capability_Response` | send_evt_HCI_IO_Capability_Response: BD_ADDR + param extract, calls hci_event_sender | **high (decompiled+documented, Pass 5 2026-06-25)** |
| `0x80023e38` | 70 | `send_evt_HCI_User_Passkey_Notification` | send_evt_HCI_User_Passkey_Notification: BD_ADDR + u32 LSB-order pack, calls hci_event_sender | **high (decompiled+documented, Pass 5 2026-06-25)** |
| `0x80023e84` | 66 | `send_evt_HCI_Remote_OOB_Data_Request` | send_evt_HCI_Remote_OOB_Data_Request: BD_ADDR extract + ROM notify (FUN_8001d03c), calls hci_event_sender | **high (decompiled+documented, Pass 5 2026-06-25)** |
| `0x80023ecc` | 66 | `send_evt_HCI_User_Passkey_Request` | send_evt_HCI_User_Passkey_Request: thin wrapper (partial decompile output), calls hci_event_sender | **high (decompiled+documented, Pass 5 2026-06-25, partial output)** |
| `0x80023f14` | 84 | `send_evt_HCI_User_Confirmation_Request` | send_evt_HCI_User_Confirmation_Request: thin wrapper (partial decompile output), calls hci_event_sender | **high (decompiled+documented, Pass 5 2026-06-25, partial output)** |
| `0x80023f6c` | 66 | `send_evt_HCI_IO_Capability_Request` | send_evt_HCI_IO_Capability_Request: thin wrapper (partial decompile output), calls hci_event_sender | **high (decompiled+documented, Pass 5 2026-06-25, partial output)** |
| `0x80023fb4` | 4 | `set_arg1+1_to_arg2` | set arg1+1 to arg2 | low (named by Kovah, purpose unclear) |
| `0x80023fd0` | 10 | `some_case_0x2d` | some case 0x2d | low (named by Kovah, purpose unclear) |
| `0x800240f4` | 24 | `ret_bool_based_on_crypto_struct_0x50` | ret bool based on crypto struct 0x50 | low (named by Kovah, purpose unclear) |
| `0x8002442c` | 62 | `wrap_send_LMP_NOT_ACCEPTED` | wrap_send_LMP_NOT_ACCEPTED: LMP reject wrapper (partial decompile output) | **high (decompiled+documented, Pass 5 2026-06-25, partial output)** |
| `0x8002469c` | 92 | `wrap_send_LMP_ACCEPTED_and_some_other_things` | wrap_send_LMP_ACCEPTED_and_some_other_things: LMP accept wrapper (partial decompile output) | **high (decompiled+documented, Pass 5 2026-06-25, partial output)** |
| `0x80024bd8` | 48 | `copy_fields_within_crypto_struct` | copy_fields_within_crypto_struct: crypto-struct field copy utility (partial decompile output) | **high (decompiled+documented, Pass 5 2026-06-25, partial output)** |
| `0x80024ca4` | 864 | `start_with_fptr_called_by_call_send_evt_HCI_Simple_Pairing_Complete__state_machine_update?` | start with fptr called by call send evt HCI Simple Pairing Complete  state machine update? | low (named by Kovah, purpose unclear) |
| `0x80025cb4` | 118 | `LMP__271__FUN_80025cb4` | LMP  271  FUN 80025cb4 | medium (named, one-line purpose only, not decompiled) |
| `0x80025d34` | 160 | `some_case_0x3b_or_0x3c_possible_HCI_Passkey_Notification_or_HCI_Keypress_Notification` | some case 0x3b or 0x3c possible HCI Passkey Notification or HCI Keypress Notification | low (named by Kovah, purpose unclear) |
| `0x80026608` | 140 | `call_send_evt_HCI_Simple_Pairing_Complete` | call send evt HCI Simple Pairing Complete | low (named by Kovah, purpose unclear) |
| `0x80026c38` | 536 | `LMP_ENCRYPTION_MODE_REQ_0x0F` | LMP encryption mode negotiator; 7+ state machine (idle/initiating/pending); validates role + capability flags — see `region_0x80020000` Pass 3 | **high (decompiled+documented, Pass 3 2026-06-24)** |
| `0x80026e64` | 232 | `LMP_ENCRYPTION_KEY_SIZE_REQ_0x10_possibility2` | Alternative key size path; handles 3 role variants (0x0e/0x1a/0x0d) — see `region_0x80020000` Pass 3 | **high (decompiled+documented, Pass 3 2026-06-24)** |
| `0x80026f54` | 416 | `LMP_COMB_KEY_0x09` | Combination key (legacy authentication); 5 role variants; feature-page gating — see `region_0x80020000` Pass 3 | **high (decompiled+documented, Pass 3 2026-06-24)** |
| `0x80027100` | 364 | `LMP_SRES_0x0C` | Authentication response calculator; stores response at offset +0xbe; triggers SSP continuation — see `region_0x80020000` Pass 3 | **high (decompiled+documented, Pass 3 2026-06-24)** |
| `0x80027454` | 466 | `LMP_IN_RAND_0x08` | Input random validator; validates challenge receipt; 7+ role paths; feature-page checks — see `region_0x80020000` Pass 3 | **high (decompiled+documented, Pass 3 2026-06-24)** |
| `0x8002763c` | 834 | `LMP_AU_RAND_0x0B` | **Largest function (834B):** Authentication random generator; multi-state machine with 15+ role variants; integrates SSP with challenge flow — see `region_0x80020000` Pass 3 | **high (decompiled+documented, Pass 3 2026-06-24)** |
| `0x80027de0` | 326 | `LMP_ENCRYPTION_KEY_SIZE_REQ_0x10` | LMP key size selector; bounds-checked (7–16 bytes); per-state logic (idle/pending/negotiating) — see `region_0x80020000` Pass 3 | **high (decompiled+documented, Pass 3 2026-06-24)** |
| `0x80027f30` | 74 | `LMP_ENCRYPTION_KEY_SIZE_MASK_RES_0x3B` | Key size mask response (master→slave); validates role bit; writes to crypto struct offset +0x24 — see `region_0x80020000` Pass 3 | **high (decompiled+documented, Pass 3 2026-06-24)** |
| `0x80027f80` | 76 | `LMP_ENCRYPTION_KEY_SIZE_MASK_REQ_0x3A` | Key size mask request (slave→master); feature-page gate; dispatches by feature bit state — see `region_0x80020000` Pass 3 | **high (decompiled+documented, Pass 3 2026-06-24)** |
| `0x80027fd4` | 206 | `LMP_STOP_ENCRYPTION_REQ_0x12` | Encryption terminator; state-dependent (role 0x3f/0x3e–0x43); calls ROM cleanup chains — see `region_0x80020000` Pass 3 | **high (decompiled+documented, Pass 3 2026-06-24)** |
| `0x800281c4` | 160 | `LMP_NOT_ACCEPTED_0x04` | LMP NOT ACCEPTED 0x04 | medium (named, one-line purpose only, not decompiled) |
| `0x80028264` | 568 | `LMP_encryption_opcode_handlers` | LMP encryption opcode handlers — see `encryption_engine`, `hardware_layer`, `lc_lmp_state_machine` | high (decompiled+documented) |
| `0x800287b8` | 316 | `LMP_DHKEY_CHECK_0x41` | ECDH public key verification; confirms ECDH secret derivation — see `region_0x80020000` Pass 3 | **high (decompiled+documented, Pass 3 2026-06-24)** |
| `0x80028904` | 68 | `wraps_LMP_DHKEY_CHECK_0x41` | DHKEY wrapper/dispatcher; calls DHKEY_CHECK with state dispatch — see `region_0x80020000` Pass 3 | **high (decompiled+documented, Pass 3 2026-06-24)** |
| `0x80028950` | 550 | `LMP_SIMPLE_PAIRING_NUMBER_0x40` | SSP number (passkey) exchange; role-dependent handling — see `region_0x80020000` Pass 3 | **high (decompiled+documented, Pass 3 2026-06-24)** |
| `0x80028bb8` | 294 | `LMP_SIMPLE_PAIRING_CONFIRM_0x3F` | SSP confirm validator; state-dependent (role 0x12/0x1c/0x1d); triggers IO capability exchange on mismatch — see `region_0x80020000` Pass 3 | **high (decompiled+documented, Pass 3 2026-06-24)** |
| `0x80028fc4` | 646 | `LMP_PAUSE_ENCRYPTION_AES_REQ_0x66` | Encryption pause handler; AES pause request processing; manages encryption state transitions — see `region_0x80020000` Pass 3 | **high (decompiled+documented, Pass 3 2026-06-24)** |
| `0x800297bc` | 110 | `LMP_USE_SEMI_PERMANENT_KEY_0x32` | Semi-Permanent Key (SPK) activation; sets persistent link-key mode — see `region_0x80020000` Pass 3 | **high (decompiled+documented, Pass 3 2026-06-24)** |
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
| `0x8003003c` | 116 | `VSC_0xfc46_remote_query` | VSC 0xfc46 remote feature/version query handler — stores query results to capability struct | **high (decompiled+documented, Pass 3 2026-06-24)** |
| `0x800300c4` | 102 | `VSC_0xfc95_feature_toggle` | VSC 0xfc95 feature enable/disable controller; toggles 11-bit feature flags; calls LMP_25B/268 gateways | **high (decompiled+documented, Pass 3 2026-06-24)** |
| `0x800302ac` | 272 | `references_patch_download_mem4` | references patch download memory region | low (named by Kovah, purpose unclear) |
| `0x800303f4` | 306 | `VSC_0xfc35_config_update` | VSC 0xfc35 device configuration loader (9B entry records, up to ~40 devices); TLV-style blob processor; calls FUN_8007442c cleanup | **high (decompiled+documented, Pass 3 2026-06-24)** |
| `0x80030b2c` | 150 | `VSC_0xfc27_param_query` | VSC 0xfc27 parameter read/write with interrupt masking; supports 2-byte parameter pairs | **high (decompiled+documented, Pass 3 2026-06-24)** |
| `0x80030bdc` | 346 | `VSC_0xfc64_link_quality` | VSC 0xfc64 link quality monitor: 9-case dispatch on param bits[7:4]; AFH register 0x2d poll | **high (decompiled+documented, Pass 3 2026-06-24)** |
| `0x80030dd8` | 268 | `VSC_0xfc61_config_update` | VSC 0xfc61 unified hardware register reader/writer; supports 1/2/4-byte sizes; alignment checks | **high (decompiled+documented, Pass 3 2026-06-24)** |
| `0x80030eec` | 40 | `VSC_0xfc8b_diagnostic_query` | VSC 0xfc8b hardware diagnostic read (1–2 bit positions); returns register value via status struct | **high (decompiled+documented, Pass 3 2026-06-24)** |
| `0x80030f1c` | 4372 | `HCI_CMD_OGF_3F__Vendor_Specific__FUN_80030f1c` | HCI CMD OGF 3F  Vendor Specific  FUN 80030f1c — see `hci_command_router` | high (decompiled+documented) |
| `0x80032540` | 2068 | `multi-VSC_Handler_FUN_80032540` | confirmed master secondary VSC dispatcher: switch/if-chain over opcodes 0xfc1f/0xfc20/0xfc22/0xfc27/0xfc55/0xfc56/0xfc61/0xfc65/0xfc8b/0xfcf0/0xfd41/0xfd49 — see `region_0x80030000` | **high (decompiled+documented, Pass 5 2026-06-24)** |
| `0x80032e28` | 20 | `called_by_function_that_uses_Logger_string_2_initialize_something_at_offset_0x800` | called by function that uses Logger string 2 initialize something at offset 0x800 | low (named by Kovah, purpose unclear) |
| `0x80033188` | 182 | `calls_fptr_down_LMP__47E_path` | calls fptr down LMP  47E path | low (named by Kovah, purpose unclear) |
| `0x80033f8c` | 930 | `validate_connection_setup_preconditions` | pure boolean gate chaining ~15 precondition checks against bos_base flags (offsets 0x1a4/0x1d0/0x28/0x44) and clock/instant comparisons before allowing a new connection/role-switch — see `region_0x80030000` | **high (decompiled+documented, Pass 5 2026-06-24)** |
| `0x80034a38` | 378 | `idk_takes_new_new_power_val` | idk takes new new power val | low (named by Kovah, purpose unclear) |
| `0x80034be0` | 120 | `set_new_power_val` | set new power val | low (named by Kovah, purpose unclear) |
| `0x80035068` | 138 | `LMP__25C_called2` | LMP  25C called2 | low (named by Kovah, purpose unclear) |
| `0x80036bd0` | 336 | `fHCI_conn_req_cancel` | HCI Create Connection / Remote Name Request cancellation handler — BD_ADDR lookup; clears connection record | **high (decompiled+documented, Pass 3 2026-06-24)** |
| `0x80036d44` | 86 | `fHCI_inquiry_cancel` | HCI Inquiry Cancel (OGF 1 / OCF 2) handler; clears EIR data structure; calls ROM cleanup fns | **high (decompiled+documented, Pass 3 2026-06-24)** |
| `0x80036df8` | 316 | `called_by_fHCI_Remote_Name_Request_5` | called by fHCI Remote Name Request 5 — see `lc_lmp_state_machine` | high (decompiled+documented) |
| `0x80037e28` | 932 | `apply_eSCO_SCO_packet_type_params` | selects baseband packet-type bitmask by switching on connection-type constants 0xa000/0xb000/0xe000/0xf000; applies via FUN_80013be4/FUN_80013c0c — see `region_0x80030000` | **high (decompiled+documented, Pass 5 2026-06-24)** |
| `0x8003bbf0` | 94 | `VSC_0xfd49_extended_diagnostic` | VSC 0xfd49 extended diagnostic dispatcher; loops over diagnostic modes | **high (decompiled+documented, Pass 3 2026-06-24)** |
| `0x8003cb80` | 686 | `apply_LAP_derived_hopping_params` | reads BD_ADDR LAP (_x142_LAP struct field) and writes derived values into baseband hopping-sequence registers 0x14/0x16/0x10/0x12/0xaa; packs LAP-derived bits with link-policy flags — see `region_0x80030000` | **high (decompiled+documented, Pass 5 2026-06-24)** |
| `0x8003d7bc` | 1524 | `apply_SCO_connection_params_to_hw` | per-connection-index SCO/eSCO param apply: writes baseband regs 0xde/0x9e/0x5e/0x1ec/0x1ee/0x23c; packet-type-derived link-supervision values; interrupt-bracketed; calls FUN_80043400/FUN_80043438 — see `region_0x80030000` | **high (decompiled+documented, Pass 5 2026-06-24)** |
| `0x8003ec48` | 628 | `release_SCO_connection_resources` | connection teardown counterpart to apply_SCO_connection_params_to_hw: clears connection-table entry, decrements ref counters, writes baseband regs 0xee/0x56/0x260/0x27e/0xe0/0x298, calls FUN_8003d204 + cleanup hook fptr — see `region_0x80030000` | **high (decompiled+documented, Pass 5 2026-06-24)** |
| `0x80041c18` | 64 | `fHCI_Exit_Periodic_Inquiry_Mode_0x04` | clears 4 state fields, calls `FUN_800408ec`, `LMP__25B__most_common_for_VSCs1`, clears EIR-data ptr/len, calls `FUN_80043a60` — periodic inquiry teardown, consistent with HCI Exit Periodic Inquiry Mode (0x0404) | high (decompiled+documented, Pass 2) |
| `0x80042188` | 634 | `LC_event_RX_dispatcher` (was `assoc_w_tLC_RX`) | large switch on opcode field (param_1[4], range 0x2b8-0x4b7) dispatching ~15 Link-Controller RX event handlers (logging, FUN_800378e4/7e04ce70/etc, send_evt_Meta_buf_at_arg1, send_evt_Meta_subevent_0x11) — confirms Kovah's "assoc w/ tLC RX" hint as a real LC inbound event dispatcher | high (decompiled+documented, Pass 2) |
| `0x80042420` | 418 | `LC_event_TX_dispatcher` (was `assoc_w_tLC_TX`) | large switch on opcode field (param_1[2], range 0x321-0x4b6) dispatching ~14 Link-Controller TX event handlers, incl. interrupt-disable + linked-list teardown loop (case 0x32f) — confirms Kovah's "assoc w/ tLC TX" hint as the LC outbound counterpart | high (decompiled+documented, Pass 2) |
| `0x80042a14` | 18 | `check_new_power_val_nonzero` | `return param_1 != 0` — trivial nonzero-check on a power-control byte param | high (decompiled+documented, Pass 2) |
| `0x80042a28` | 16 | `check_power_val_below_max_limit_6` (was `check_if_at_max_power_(6)`) | `return param_1 < *PTR_DAT_80042a38` — compares power val against a configured max-limit byte (named "6" by Kovah, likely the observed runtime value of the limit, not a hardcoded constant) | high (decompiled+documented, Pass 2) |
| `0x80042a3c` | 22 | `increment_power_val_if_less_than_6` (was `increment_new_power_val_if_<_6`) | `if (param_1 < *PTR_DAT_80042a54) param_1++` — confirms increment-with-clamp semantics | high (decompiled+documented, Pass 2) |
| `0x80042a58` | 16 | `decrement_power_val_if_nonzero` (was `increment_new_power_val_if_!=_0`, **name corrected**) | `if (param_1 != 0) param_1 = param_1 - 1` — decompile shows this **decrements**, not increments as Kovah's original guess stated; renamed to reflect actual decompiled behavior | high (decompiled+documented, Pass 2 — corrects prior mislabel) |
| `0x80042b38` | 62 | `return_RSSI_value` (was `return_RSSI`) | calls an optional function-pointer hook (`PTR_DAT_80042b78`) for RSSI; on failure/absence falls back to a config-struct-derived formula `((param&0x3f)*2 + config->field453_0x1d1) >> 24`-style scaling — confirms RSSI getter with HW-hook override | high (decompiled+documented, Pass 2) |
| `0x80043810` | 102 | `remote_name_request_feature_index_selector` (was `called_by_fHCI_Remote_Name_Request_3`) | selects a feature/connection index 0-0xff based on config-struct flag `field208_0xd8 & 4` and feature-page byte `0x16f`/`0x24`, with a 0xff sentinel for "no match" — used during Remote Name Request to pick the right feature-page/connection-slot index | high (decompiled+documented, Pass 2) |
| `0x80043884` | 210 | `remote_name_request_feature_apply_8` (was `FUN_80043884`) | apply-side counterpart to `remote_name_request_feature_index_selector` — identical gating fields (`field208_0xd8 & 8`, `byte_0x16a`/`int_0x10`, `field_0x171`/`field_0x173`), but on gate-pass computes an offset from a feature-page ushort and calls `FUN_8003ca28()` to commit it into a struct field at `+0x11c` | high (decompiled+documented, region_0x80040000 Pass 6) |
| `0x80043984` | 178 | `remote_name_request_feature_apply_4` (was `FUN_80043984`) | structural twin of `remote_name_request_feature_apply_8`, differing only in the `field208_0xd8` bitmask tested (`4` vs `8`) and the commit function called (`FUN_8003c94c` vs `FUN_8003ca28`) | high (decompiled+documented, region_0x80040000 Pass 6) |
| `0x80044430` | 90 | `LMP_PDU_0xc6d_feature_page_bit_toggle` (was `OGC_3_default_func_3`) | only handles LMP opcode 0xc6d (returns success-0 for 0xc6c, error 0x12 for any other unsupported opcode); for 0xc6d toggles bits 2/4 of feature-page byte at `PTR_some_feature_page_base_8004448c+8` based on 2 single-bit params — an OGF=3-area LMP PDU handler for feature-page negotiation, not a generic "default" stub | high (decompiled+documented, Pass 2) |
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
| `0x8004a71c` | 16 | `VSC_0xfc95_clear_bit_helper` (was `VSC_0xfc95_called1`) | `*(ushort*)(base+0x16) &= ~(1 << (param&0x1f))` — clears one bit in a 16-bit mask at a fixed struct offset, indexed by param; small helper for VSC 0xfc95 | high (decompiled+documented, Pass 2) |
| `0x80049d20` | 1476 | `HCI_Setup_Synchronous_Connection_handler` (was `FUN_80049d20`) | Validates a packed SCO/eSCO parameter block (bandwidth/packet-type/retransmission-window range checks matching SCO/eSCO bounds), writes results into `big_ol_struct` fields `+0x1a6..+0x1ce`, terminates with `send_evt_HCI_Command_Status` — canonical HCI command-handler signature; param shape matches HCI Setup/Accept Synchronous Connection | high (decompiled+documented, Pass 3) |
| `0x8004966c` | 696 | `HCI_Accept_Synchronous_Connection_Request_handler` (was `FUN_8004966c`) | Validates SCO/eSCO bandwidth/packet-type/retransmission-window params via nearly identical bounds checks to `HCI_Setup_Synchronous_Connection_handler` (0x80049d20), writes into `get_0x1ac_struct_ptr_by_index`-addressed connection-record fields, terminates via `send_evt_HCI_Command_Status` on every path — sibling "Accept" handler to the "Setup" handler | high (decompiled+documented, Pass 3 continuation) |
| `0x8004c0f4` | 472 | `LMP_opcode_0x26F_LE_event_router` (was `LMP__26F__sends_LE_HCI_Events`) | big 11-case switch on low 5 bits of an LMP-0x26F PDU's opcode field, routing to `send_evt_HCI_Connection_Complete`/`Enhanced`, `send_evt_Meta_subevent_0x05/0x08/0x09/0x0c/0x14`, `hci_event_sender` (evt 0x30/0x4), and `send_evt_HCI_Disconnection_Complete` — confirms this is the central dispatcher feeding the already-documented LE Meta Event sender cluster (0x80044730-0x80046620) | high (decompiled+documented, Pass 2) |
| `0x8004ca7c` | 192 | `conn_link_quality_history_reset_and_vsc_0xfc95_trigger` (was `FUN_8004ca7c`) | Keyed by `(conn_idx, mode)`: clears the 4 already-confirmed RSSI/link-quality rolling counters (`field70_0x46`/`field76_0x4c`/`field74_0x4a`/`field68_0x44` — "good"/"bad-class-A"/"bad-class-B"/"marginal", per `conn_rssi_quality_history_update`) when mode==0, copies the RSSI field (`field40_0x28`) into `field72_0x48`, then calls the established VSC_0xfc95 triad (`LMP__25B__most_common_for_VSCs1`/`VSC_0xfc95_called2`/`LMP__268__most_common_for_VSCs2_checks_fptr_patch`) — same triad seen gating the `link_state6_afh_or_channel_feature_toggle1/2/3` siblings in region 0x80000000 | high (decompiled+documented, Pass 4) |
| `0x8004d8b8` | 1898 | `init_global_connection_table_and_bt_state` (was `FUN_8004d8b8`) | Global BT-state/connection-table initializer: memsets the entire `PTR_base_of_0x1ac_struct_array_0xA_large2` 11-entry connection-record array, sets default LST `0xa0a` (same constant `init_connection_record` uses), default poll intervals, BD_ADDR/feature fields from config struct, calls 4 sub-initializers — top-level counterpart to the per-record `init_connection_record` (0x8005b9d8) | high (decompiled+documented, Pass 3) |
| `0x800525b4` | 36 | `send_evt_LE_Meta_Subevent_variant` | sends LE Meta subevent with pre-incremented length field — see `region_0x80050000` | high (decompiled+documented) |
| `0x800525d8` | 62 | `send_evt_LE_Meta_Subevent` | generic LE Meta subevent sender with HCI event 0x3E header — see `region_0x80050000` | high (decompiled+documented) |
| `0x800566f8` | 58 | `VSC_0xfc97_Set_Extended_Advertising_Parameters_variant_1` | validates and applies extended advertising parameters for LE — see `region_0x80050000` | high (decompiled+documented) |
| `0x8005681c` | 84 | `VSC_0xfc73_AFH_Channel_Assessment_variant_3` | AFH channel assessment updates with connection context (variant 3) — see `region_0x80050000` | high (decompiled+documented) |
| `0x80056878` | 84 | `VSC_0xfc73_AFH_Channel_Assessment_variant_2` | processes AFH channel assessment reports with global state update (variant 2) — see `region_0x80050000` | high (decompiled+documented) |
| `0x800568d4` | 94 | `VSC_0xfc73_AFH_Channel_Assessment_variant_1` | main AFH channel map update dispatcher with map flags and commitment (variant 1) — see `region_0x80050000` | high (decompiled+documented) |
| `0x80056ca8` | 542 | `afh_channel_quality_poll_commit` | register-polling counterpart of the AFH report handler `0x80056988`; writes RX triple (+0x28/0x2a/0x2c) and TX triple (+0x2e/0x30/0x32) into the per-link sub-record — see `region_0x80050000` Pass 3c | high (decompiled+documented) |
| `0x8005770c` | 166 | `VSC_0xfc97_Set_Extended_Advertising_Parameters_variant_2` | comprehensive extended advertising parameter handler with interval validation — see `region_0x80050000` | high (decompiled+documented) |
| `0x800596c8` | 50 | `query_config_struct_0x1ac_by_index` | config struct array accessor; retrieves 0x1ac-sized records by index (BD_ADDR/HW config blocks) — see `region_0x80050000` | high (decompiled+documented) |
| `0x8005a298` | 62 | `query_current_PHY_by_connection_index` | PHY query helper; returns active TX or RX PHY for a connection — see `region_0x80050000` | high (decompiled+documented) |
| `0x80059454` | 532 | `lmp_packet_completion_event_drain_dispatch` | LMP/baseband packet-completion event drain/dispatcher; drains a linked list of completed-packet records (0x6b stride), checks credit availability, posts completion. Confirmed direct callee of `conn_index_status_bit_apply_and_log` (0x80007330, region 0x80000000) — called immediately after a connection is committed "active" (field297_0x130=1), matching the packet-completion-posting role exactly — see `region_0x80050000` Pass 4 | high (decompiled+documented) |
| `0x8005b79c` | 312 | `release_connection_record` | Connection-record release/deallocator counterpart to `init_connection_record` (0x8005b9d8): checks an override hook and a per-slot active-bit, clears named timer/role-tracker bytes via three duplicate-index checks, calls `LMP__25B__most_common_for_VSCs1` for any pending function-pointer slots, `memset`s the bulk of the 0x1ac record, then restores the exact same allocator-time defaults (LST 0xa0a at +0x140/+0x144, poll interval 3000=0xbb8 at +0x110) — the alloc/free pairing with `init_connection_record`'s defaults is unambiguous — see `region_0x80050000` Pass 9 | high (decompiled+documented, Pass 9 2026-06-24) |
| `0x8005b9d8` | 950 | `init_connection_record` | connection-record allocator/initializer for a new ACL/SCO/eSCO link (central or peripheral); memset+populates the full 0x1ac struct, sets default LST/poll interval, PHY power tables, conditionally invokes the VSC_0xfc97 extended-advertising-parameter handlers — see `region_0x80050000` Pass 3c | high (decompiled+documented) |
| `0x8005c27c` | 550 | `afh_report_worst_channel` | AFH channel-classification worst-channel picker/reporter (event code 0x777); iterates the AFH bitmap incrementing a shared counter table, then reports the worst channel — see `region_0x80050000` Pass 3c | high (decompiled+documented) |
| `0x800538b4` | 352 | `sched_event_sorted_insert_with_overlap_pushback` | sorted doubly-linked-list insertion keyed by a wraparound-masked time/slot field; walks the list to find the insertion point, adjusting the neighboring node's duration field (+0x26) to push back and resolve time-window overlap — timer-wheel/scheduled-event queue insert — see `region_0x80050000` Pass 5 | high (decompiled+documented) |
| `0x80050304` | 408 | `conn_diagnostic_batch_dump` | per-connection diagnostic/status batch-dump; collects metrics across active connections and flushes in groups of 5 via `diagnostic_batch_entry_log_emit`; promoted to high after confirming the callee is a pure log-emit primitive — see `region_0x80050000` Pass 6 | high (decompiled+documented) |
| `0x8004fe64` | 124 | `diagnostic_batch_entry_log_emit` | pure per-entry diagnostic log-emit helper; calls `possible_logging_function__var_args` with 5 paired entries from its array arguments, no other logic — see `region_0x80050000` Pass 6 | high (decompiled+documented) |
| `0x8005261c` | 338 | `hci_evt_pack_conn_field_into_buf` | HCI event-buffer field-packer; packs connection-record fields (handle, conditional BD_ADDR, role/mode bitfields, clock offset, variable tail) into a serial buffer, flushing via `send_evt_Meta_buf_at_arg1` on overflow — see `region_0x80050000` Pass 6 | high (decompiled+documented) |
| `0x80058bb8` | 508 | `conn_slot_alloc_and_commit_dispatch` | connection-slot allocate+commit dispatcher; dispatches on type code (0/1/2) to type-specific free-list slot allocators (`FUN_80058974` types 0/1, `FUN_80058a5c` type 2 — both confirmed circular free-list slot-pop primitives over fixed-size record pools, copying a 6-byte BD_ADDR plus type-specific key material into the new record), validates capacity, logs success/failure, commits HW/status-table fields via `FUN_80057094`; promoted to high after confirming both type-specific helpers are self-contained slot-allocate primitives — see `region_0x80050000` Pass 7 | high (decompiled+documented) |
| `0x80055204` | 260 | `le_channel_selection_algorithm_event_dispatch` | connection-setup-completion notification dispatcher; explicitly calls already-named `send_evt_Meta_subevent_0x12` (LE Channel Selection Algorithm meta-subevent, 0x800454e8) immediately after triggering `conn_diagnostic_batch_dump`, then performs per-channel-slot enable/ref-count bookkeeping via `FUN_8004fd6c` and logs a full status report — see `region_0x80050000` Pass 8 | high (decompiled+documented) |
| `0x8005d26c` | 88 | `assign_pointer_to_0x1AC_offset_0x134` | assign pointer to 0x1AC offset 0x134 — see `lmp_version_conn_setup` | high (decompiled+documented) |
| `0x8005e23c` | 118 | `access_config_at_0xa5_and_0x1ac_stuct_stuff` | access config at 0xa5 and 0x1ac stuct stuff — see `lmp_version_conn_setup` | high (decompiled+documented) |
| `0x8005e3b8` | 80 | `fHCI_Read_Remote_Version_Information_config_handler` | HCI Read Remote Version Information handler; updates config struct 0x1ac with remote LMP version — see `region_0x80050000` | high (decompiled+documented) |
| `0x800600e8` | 218 | `LMP__270__FUN_800600e8` | LMP  270  FUN 800600e8 | high (decompiled+documented in region_0x80060000) |
| `0x800605a4` | 4 | `just_return_0` | just return 0 | high (decompiled+documented in region_0x80060000) |
| `0x800605a8` | 308 | `get_status_bits_by_LMP_Opcode` | get status bits by LMP Opcode | high (decompiled+documented in region_0x80060000) |
| `0x80060740` | 44 | `lookup_up_to_3_bos_array_indices_by_connection_handle` | lookup up to 3 bos array indices by connection handle | high (decompiled+documented in region_0x80060000) |
| `0x800608f0` | 222 | `some_case_0x2b` | some case 0x2b | high (decompiled+documented in region_0x80060000) |
| `0x80060c30` | 80 | `look_for_non_matching_bdaddr_bos_index_i.e._free_connection_slot` | look for non matching bdaddr bos index i.e. free connection slot | high (decompiled+documented in region_0x80060000) |
| `0x80060cfc` | 16 | `zero_initialize_6_bytes_at_param1` | zero initialize 6 bytes at param1 | high (decompiled+documented in region_0x80060000) |
| `0x80060d0c` | 186 | `called_by_fHCI_Remote_Name_Request_6_nop_if_not_patched?` | called by fHCI Remote Name Request 6 nop if not patched? | high (decompiled+documented in region_0x80060000) |
| `0x80060dd8` | 188 | `return_big_ol_array_offset` | return big ol array offset | high (decompiled+documented in region_0x80060000) |
| `0x800611e4` | 720 | `send_LMP_pkt` | send LMP pkt — see `lc_lmp_state_machine` | high (decompiled+documented) |
| `0x800615a8` | 62 | `send_extended_opcode_LMP_reply?` | send extended opcode LMP reply? | high (decompiled+documented in region_0x80060000) |
| `0x80061624` | 122 | `LMP_PACKET_TYPE_TABLE_REQ_0x7F_0B` | LMP PACKET TYPE TABLE REQ 0x7F 0B | high (decompiled+documented in region_0x80060000) |
| `0x80061754` | 44 | `if_arg1<3_memcpy_features_to_arg2_else_HCI_Inquiry_Cancel` | if arg1<3 memcpy features to arg2 else HCI Inquiry Cancel | high (decompiled+documented in region_0x80060000) |
| `0x80061784` | 100 | `copy_feature_and_send_extended_LMP_reply` | copy feature and send extended LMP reply | high (decompiled+documented in region_0x80060000) |
| `0x800617ec` | 592 | `LMP_FEATURES_RES_EXT_0x7F_04` | LMP FEATURES RES EXT 0x7F 04 | high (decompiled+documented in region_0x80060000) |
| `0x80061a4c` | 70 | `LMP_FEATURES_REQ_EXT_0x7F_03` | LMP FEATURES REQ EXT 0x7F 03 | high (decompiled+documented in region_0x80060000) |
| `0x80061ad8` | 88 | `LMP_NOT_ACCEPTED_EXT_0x7F_02` | LMP NOT ACCEPTED EXT 0x7F 02 | high (decompiled+documented in region_0x80060000) |
| `0x80061b34` | 80 | `LMP_ACCEPTED_EXT_0x7F_01` | LMP ACCEPTED EXT 0x7F 01 | high (decompiled+documented in region_0x80060000) |
| `0x80061bb8` | 156 | `LMP_extended_opcode_handler_0x01-0x11` | LMP extended opcode handler 0x01-0x11 | high (decompiled+documented in region_0x80060000) |
| `0x80061e70` | 60 | `some_case_0x37_2` | some case 0x37 2 | high (decompiled+documented in region_0x80060000) |
| `0x80061eb0` | 160 | `LMP_SNIFF_SUBRATING_RES_0x7F_16` | LMP SNIFF SUBRATING RES 0x7F 16 | high (decompiled+documented in region_0x80060000) |
| `0x80062054` | 238 | `LMP_SNIFF_SUBRATING_REQ_0x7F_15` | LMP SNIFF SUBRATING REQ 0x7F 15 | high (decompiled+documented in region_0x80060000) |
| `0x80062158` | 70 | `LMP_extended_opcode_handler_0x15-0x16` | LMP extended opcode handler 0x15-0x16 | high (decompiled+documented in region_0x80060000) |
| `0x80062270` | 166 | `LMP__267__FUN_80062270` | LMP  267  FUN 80062270 | high (decompiled+documented in region_0x80060000) |
| `0x8006251c` | 132 | `LMP_POWER_CONTROL_RES` | LMP POWER CONTROL RES | high (decompiled+documented in region_0x80060000) |
| `0x80062658` | 150 | `LMP_POWER_CONTROL_REQ` | LMP POWER CONTROL REQ | high (decompiled+documented in region_0x80060000) |
| `0x800626f8` | 116 | `0x7F_LMP_POWER_REQ_RES_0x1F_0x20` | 0x7F LMP POWER REQ RES 0x1F 0x20 | high (decompiled+documented in region_0x80060000) |
| `0x80062924` | 76 | `LMP_CLK_ADJ_ACK_0x7F_0x06` | LMP CLK ADJ ACK 0x7F 0x06 | high (decompiled+documented in region_0x80060000) |
| `0x80062a58` | 568 | `VSC_0xfcd9_FUN_80062a58` | VSC 0xfcd9 FUN 80062a58 | high (decompiled+documented in region_0x80060000) |
| `0x80062cac` | 390 | `LMP_CLK_ADJ_0x7F_0x05` | LMP CLK ADJ 0x7F 0x05 | high (decompiled+documented in region_0x80060000) |
| `0x80062e44` | 318 | `LMP_CLK_ADJ_REQ_0x7F_0x07` | LMP CLK ADJ REQ 0x7F 0x07 | high (decompiled+documented in region_0x80060000) |
| `0x80062f94` | 126 | `0x7F_LMP_CLK_ADJ(0x05)_ADJ_ACK(0x06)_ADJ_REQ(0x07)` | 0x7F LMP CLK ADJ(0x05) ADJ ACK(0x06) ADJ REQ(0x07) | high (decompiled+documented in region_0x80060000) |
| `0x80063458` | 96 | `LMP_CHANNEL_CLASSIFICATION_0x7F_11` | LMP CHANNEL CLASSIFICATION 0x7F 11 | high (decompiled+documented in region_0x80060000) |
| `0x800634c0` | 204 | `LMP_CHANNEL_CLASSIFICATION_REQ_0x7F_10` | LMP CHANNEL CLASSIFICATION REQ 0x7F 10 | high (decompiled+documented in region_0x80060000) |
| `0x80063cc4` | 438 | `LMP_SET_AFH_0x3C` | LMP SET AFH 0x3C | high (decompiled+documented in region_0x80060000) |
| `0x800656bc` | 256 | `LMP_CH__0x3ea__FUN_800656bc` | LMP CH  0x3ea  FUN 800656bc | high (decompiled+documented in region_0x80060000) |
| `0x80066e68` | 200 | `assoc_w_tLMP_CH` | assoc w tLMP CH | high (decompiled+documented in region_0x80060000) |
| `0x80067128` | 160 | `set_check_for_1_to_1` | set check for 1 to 1 | high (decompiled+documented in region_0x80060000) |
| `0x80067a2c` | 680 | `init_inquiry_page_state_from_config` | inquiry/page/discoverability state-block initializer; hardcodes GIAC LAP 0x9E8B33, copies feature/BD-config bytes from config blob | high (decompiled+documented in region_0x80060000, Pass 3) |
| `0x800683d8` | 40 | `calls_fptr` | calls fptr | high (decompiled+documented in region_0x80060000) |
| `0x80068400` | 40 | `c_by_LMP_ENCAPSULATED_PAYLOAD_0x3E_call_fptr` | c by LMP ENCAPSULATED PAYLOAD 0x3E call fptr | high (decompiled+documented in region_0x80060000) |
| `0x8006845c` | 78 | `call_if_encapsulated_payload_or_header_rejected` | call if encapsulated payload or header rejected | high (decompiled+documented in region_0x80060000) |
| `0x800684c8` | 36 | `LMP_NOT_ACCEPTED` | LMP NOT ACCEPTED | high (decompiled+documented in region_0x80060000) |
| `0x800684ec` | 192 | `LMP_ENCAPSULATED_PAYLOAD_0x3E` | LMP ENCAPSULATED PAYLOAD 0x3E | high (decompiled+documented in region_0x80060000) |
| `0x800685b4` | 110 | `LMP_ENCAPSULATED_HEADER_0x3D` | LMP ENCAPSULATED HEADER 0x3D | high (decompiled+documented in region_0x80060000) |
| `0x80068680` | 122 | `send_LMP_ENCAPSULATED_HEADER_reply` | send LMP ENCAPSULATED HEADER reply | high (decompiled+documented in region_0x80060000) |
| `0x800686fc` | 94 | `wraps_send_LMP_ENCAPSULATED_HEADER_reply2` | wraps send LMP ENCAPSULATED HEADER reply2 | high (decompiled+documented in region_0x80060000) |
| `0x80068764` | 76 | `wraps_send_LMP_ENCAPSULATED_HEADER_reply` | wraps send LMP ENCAPSULATED HEADER reply | high (decompiled+documented in region_0x80060000) |
| `0x800687b8` | 46 | `wraps_send_LMP_ENCAPSULATED_HEADER_reply_1_and_2` | wraps send LMP ENCAPSULATED HEADER reply 1 and 2 | high (decompiled+documented in region_0x80060000) |
| `0x800687e8` | 130 | `LMP_encapsulated_header_and_payload_0x3D_0x3E` | LMP encapsulated header and payload 0x3D 0x3E | high (decompiled+documented in region_0x80060000) |
| `0x800688f4` | 32 | `LMP_TIMING_ACCURACY_RES_0x30` | LMP TIMING ACCURACY RES 0x30 | high (decompiled+documented in region_0x80060000) |
| `0x80068918` | 28 | `LMP_MIN_POWER_0x22` | LMP MIN POWER 0x22 | high (decompiled+documented in region_0x80060000) |
| `0x80068938` | 28 | `LMP_MAX_POWER_0x21` | LMP MAX POWER 0x21 | high (decompiled+documented in region_0x80060000) |
| `0x80068a2c` | 180 | `LMP_TEST_ACTIVATE_0x38` | LMP TEST ACTIVATE 0x38 | high (decompiled+documented in region_0x80060000) |
| `0x80068aec` | 130 | `recv_LMP_VERSION_REQ_0x25_send_LMP_VERSION_RES_0x26` | recv LMP VERSION REQ 0x25 send LMP VERSION RES 0x26 | high (decompiled+documented in region_0x80060000) |
| `0x80068f74` | 108 | `LMP_SLOT_OFFSET_0x34` | LMP SLOT OFFSET 0x34 | high (decompiled+documented in region_0x80060000) |
| `0x80068fe4` | 62 | `send_LMP_FEATURES_REQ_or_RES` | send LMP FEATURES REQ or RES | high (decompiled+documented in region_0x80060000) |
| `0x80069028` | 52 | `LMP_FEATURES_REQ_0x27` | LMP FEATURES REQ 0x27 | high (decompiled+documented in region_0x80060000) |
| `0x80069060` | 116 | `LMP_TEST_CONTROL_0x39` | LMP TEST CONTROL 0x39 | high (decompiled+documented in region_0x80060000) |
| `0x8006943c` | 214 | `LMP_INCR_POWER_REQ_0x1f` | LMP INCR POWER REQ 0x1f | high (decompiled+documented in region_0x80060000) |
| `0x80069534` | 94 | `LMP_TIMING_ACCURACY_REQ_0x2F` | LMP TIMING ACCURACY REQ 0x2F | high (decompiled+documented in region_0x80060000) |
| `0x8006959c` | 84 | `LMP_CLKOFFSET_REQ_0x05` | LMP CLKOFFSET REQ 0x05 | high (decompiled+documented in region_0x80060000) |
| `0x800695f4` | 90 | `LMP_NAME_REQ_0x01` | LMP NAME REQ 0x01 | high (decompiled+documented in region_0x80060000) |
| `0x80069658` | 214 | `LMP_DECR_POWER_REQ_0x20` | LMP DECR POWER REQ 0x20 | high (decompiled+documented in region_0x80060000) |
| `0x80069750` | 62 | `LMP_PAGE_MODE_RES_0x36` | LMP PAGE MODE RES 0x36 | high (decompiled+documented in region_0x80060000) |
| `0x80069794` | 298 | `LMP_QUALITY_OF_SERVICE_REQ_0x2A` | LMP QUALITY OF SERVICE REQ 0x2A | high (decompiled+documented in region_0x80060000) |
| `0x800698c8` | 62 | `LMP_QUALITY_OF_SERVICE_0x29` | LMP QUALITY OF SERVICE 0x29 | high (decompiled+documented in region_0x80060000) |
| `0x8006990c` | 128 | `LMP_PAGE_MODE_REQ_0x35` | LMP PAGE MODE REQ 0x35 | high (decompiled+documented in region_0x80060000) |
| `0x80069998` | 168 | `LMP_START_ENCRYPTION_REQ_0x18` | LMP START ENCRYPTION REQ 0x18 | high (decompiled+documented in region_0x80060000) |
| `0x80069a4c` | 560 | `LMP_START_ENCRYPTION_REQ_0x17` | LMP START ENCRYPTION REQ 0x17 | high (decompiled+documented in region_0x80060000) |
| `0x80069c94` | 194 | `LMP_DETACH_0x07` | LMP DETACH 0x07 | high (decompiled+documented in region_0x80060000) |
| `0x80069d6c` | 42 | `LMP_AUTO_RATE_0x23` | LMP AUTO RATE 0x23 | high (decompiled+documented in region_0x80060000) |
| `0x80069d9c` | 154 | `LMP_PREFERRED_RATE_0x24` | LMP PREFERRED RATE 0x24 | high (decompiled+documented in region_0x80060000) |
| `0x80069e40` | 84 | `LMP_SETUP_COMPLETE_0x31` | LMP SETUP COMPLETE 0x31 | high (decompiled+documented in region_0x80060000) |
| `0x80069e98` | 310 | `LMP_HOST_CONNECTION_REQ_0x33` | LMP HOST CONNECTION REQ 0x33 | high (decompiled+documented in region_0x80060000) |
| `0x80069fe4` | 148 | `LMP_SUPERVISION_TIMEOUT_0x37` | LMP SUPERVISION TIMEOUT 0x37 | high (decompiled+documented in region_0x80060000) |
| `0x8006a084` | 74 | `LMP_CLKOFFSET_RES_0x06` | LMP CLKOFFSET RES 0x06 | high (decompiled+documented in region_0x80060000) |
| `0x8006a0d4` | 90 | `LMP_VERSION_RES_0x26` | LMP VERSION RES 0x26 | high (decompiled+documented in region_0x80060000) |
| `0x8006a134` | 626 | `LMP_SWITCH_REQ_0x13` | LMP SWITCH REQ 0x13 | high (decompiled+documented in region_0x80060000) |
| `0x8006a3dc` | 108 | `LMP_MAX_SLOT_0x2D` | LMP MAX SLOT 0x2D | high (decompiled+documented in region_0x80060000) |
| `0x8006a450` | 140 | `LMP_MAX_SLOT_REQ_0x2E` | LMP MAX SLOT REQ 0x2E | high (decompiled+documented in region_0x80060000) |
| `0x8006a4e8` | 296 | `LMP_NAME_RES_0x02` | LMP NAME RES 0x02 | high (decompiled+documented in region_0x80060000) |
| `0x8006a698` | 242 | `helper_function_send_reply_LMP_FEATURES_RES_0x28` | helper function send reply LMP FEATURES RES 0x28 | high (decompiled+documented in region_0x80060000) |
| `0x8006a794` | 238 | `LMP_FEATURES_RES_0x28` | LMP FEATURES RES 0x28 | high (decompiled+documented in region_0x80060000) |
| `0x8006aae4` | 122 | `LMP_NOT_ACCEPTED_0x04` | LMP NOT ACCEPTED 0x04 | high (decompiled+documented in region_0x80060000) |
| `0x8006ac9c` | 190 | `LMP_ACCEPTED_0x03` | LMP ACCEPTED 0x03 | high (decompiled+documented in region_0x80060000) |
| `0x8006b1e4` | 58 | `lookup_some_sort_of_connection_struct_index_by_connection_handle` | lookup some sort of connection struct index by connection handle | high (decompiled+documented in region_0x80060000) |
| `0x8006bcfc` | 164 | `LMP_REMOVE_SCO_LINK_REQ_0x2C` | LMP REMOVE SCO LINK REQ 0x2C | high (decompiled+documented in region_0x80060000) |
| `0x8006c6e0` | 168 | `LMP_SCO_LINK_REQ_0x2B` | LMP SCO LINK REQ 0x2B | high (decompiled+documented in region_0x80060000) |
| `0x8006c858` | 36 | `called_by_fHCI_Read_LMP_Handle_3` | called by fHCI Read LMP Handle 3 | high (decompiled+documented in region_0x80060000) |
| `0x8006eff0` | 208 | `LMP_REMOVE_eSCO_LINK_REQ_0x7F_0D` | LMP REMOVE eSCO LINK REQ 0x7F 0D | high (decompiled+documented in region_0x80060000) |
| `0x8006f0d0` | 1600 | `LMP_eSCO_LINK_REQ_0x7F_0C` | LMP eSCO LINK REQ 0x7F 0C | high (decompiled+documented in region_0x80060000) |
| `0x8006f870` | 106 | `some_case_0x37_1` | some case 0x37 1 | high (decompiled+documented in region_0x80060000) |
| `0x8006f8e8` | 96 | `path2_send_evt_0x14_HCI_Mode_Change` | path2 send evt 0x14 HCI Mode Change | high (decompiled+documented in region_0x80060000) |
| `0x8006ff00` | 40 | `some_case_0x13` | some case 0x13 | high (decompiled+documented in region_0x80060000) |
| `0x80070248` | 144 | `LMP__48A__FUN_80070248` | LMP opcode 0x48A handler; reads conn-record fields, conditional struct-write path, no distinguishing call signature found | medium (decompiled, batch pass 5 2026-06-23) |
| `0x800702e4` | 246 | `LMP_259_opcode_handler` | LMP opcode 0x259 handler; eSCO link negotiation or feature-specific opcode path | **high** (decompiled, batch pass 3b 2026-06-23) |
| `0x800703f0` | 68 | `HCI_Inquiry_Complete_finalizer` (was `LMP__600__FUN_800703f0`) | HCI Inquiry Complete/Cancel finalizer — checks EIR-state (val 2 or 3), calls `send_evt_HCI_Inquiry_Complete(0)` then `fHCI_Inquiry_Cancel_0x02_1()`; original "LMP__600" label was a mislabel (this is HCI inquiry layer, not an LMP opcode) | **high** (decompiled+renamed, batch pass 5 2026-06-23) |
| `0x80070454` | 272 | `possible_LMP_DETACH_handler` | LMP DETACH (0x07) handler variant or detach-path dispatcher; connection teardown | **high** (decompiled, batch pass 3b 2026-06-23) |
| `0x800707dc` | 164 | `HCI_EVT_0x500_FUN_800707dc` | HCI event 0x500-family sender/handler; conn-record gated, dispatches via shared event-send primitive (exact sub-case not cross-confirmed) | medium (decompiled, batch pass 5 2026-06-23) |
| `0x8007088c` | 48 | `LMP__25C_called3` | Thin wrapper: calls `LMP__25C_called2()` then `FUN_8006d80c(p1,p2)` and `FUN_8006ba88(p1,p2)`; confirms 3-call chain sibling of `LMP__25C_called2`, no new opcode info | medium (decompiled, batch pass 5 2026-06-23) |
| `0x8007095c` | 568 | `LMP__489__various_sub_cases` | Multi-case LMP opcode dispatcher for variant/extended paths (opcode 0x489 cluster) | high (decompiled, batch pass 3a 2026-06-23) |
| `0x80070ba4` | 92 | `LMP__25C__FUN_80070ba4` | LMP  25C  FUN 80070ba4 | low (named by Kovah, purpose unclear) |
| `0x80070c04` | 1306 | `LMP_480_standard_PDU_dispatcher` | Central LMP PDU dispatcher; routes opcodes 0x01–0x3D + extended paths (16+ case arms) | **high** (decompiled, batch pass 3b 2026-06-23) |
| `0x80070084` | 414 | `LMP_role_switch_completion_handler` (was unnamed `FUN_80070084`) | Toggles master/slave role bit (`bdaddr_random_ ^= 1`), fires `send_evt_HCI_Role_Change`, dispatches to `LMP__25C_called1`/`LMP__268__most_common_for_VSCs2_checks_fptr_patch`/`LMP__25B__most_common_for_VSCs1`/`VSC_0xfc95_called2` | **high** (decompiled+renamed, cold-triage pass 6 2026-06-23) |
| `0x80070574` | 582 | `connection_teardown_HCI_event_finalizer` (was unnamed `FUN_80070574`) | Connection-record teardown finalizer; dispatches `send_evt_HCI_Disconnection_Complete`/`send_evt_HCI_Connection_Complete`/`send_evt_HCI_Remote_Name_Request_Complete` based on `byte_0x203` connection-state; calls already-named `FUN_80041dac` | **high** (decompiled+renamed, cold-triage pass 6 2026-06-23) |
| `0x80071370` | 82 | `LMP__47F__FUN_80071370` | LMP  47F  FUN 80071370 | low (named by Kovah, purpose unclear) |
| `0x800713d4` | 182 | `send_LMP_FEATURES_REQ_page1_trigger` (was `LMP__47E__FUN_800713d4`) | Explicitly sends `send_LMP_FEATURES_REQ_or_RES(conn_idx, 0x27, 3)` (decompiler comment: "0x27 = LMP_FEATURES_REQ"); sets outstanding-PDU status bits for opcode 0x28 (LMP_FEATURES_RES) reply; gated on per-connection status byte == 0x02/0x05. Clear LMP page-1 features-negotiation trigger; original "47E" label unrelated | **high** (decompiled+renamed, batch pass 5 2026-06-23) |
| `0x800714a0` | 220 | `LMP__267__FUN_800714a0` | Connection-setup feature/timer finalizer: conditionally fires VSC 0xfc95 + `LMP__268__most_common_for_VSCs2_checks_fptr_patch` when feature-page bit 2 set; conditional role-switch-style call `FUN_80061538`; services a watchdog-style timer triple (`FUN_80009b1c`/`...9a6c`/`...9a04`); finishes with `FUN_80017d2c(conn_idx, byte_0xCC, 0xffff)` | medium-high (decompiled, batch pass 5 2026-06-23; behavior clear, exact LMP opcode tie not cross-confirmed) |
| `0x80071620` | 20 | `called_at_end_of_crypto_state_machine_update` | called at end of crypto state machine update | low (named by Kovah, purpose unclear) |
| `0x80071634` | 462 | `assoc_w_tLMP_ROM_original` | ROM original LMP handler; routes extended opcodes 0x259–0x26d (intercepted by patch) | **high** (decompiled, batch pass 3b 2026-06-23) |
| `0x80071b50` | 44 | `LMP__264__FUN_80071b50` | LMP  264  FUN 80071b50 | low (named by Kovah, purpose unclear) |
| `0x80071b84` | 26 | `set_bos[bosi].0xb2_index=arg2` | set bos[bosi].0xb2 index=arg2 | low (named by Kovah, purpose unclear) |
| `0x80071ba4` | 26 | `check_if_80122df0_is_non-zero_else_ret_0xff` | check if 80122df0 is non-zero else ret 0xff | low (named by Kovah, purpose unclear) |
| `0x80071d98` | 306 | `LMP_features_validator` | Feature-page negotiation validator; gate/accept logic for extended feature PDUs | high (decompiled, batch pass 3a 2026-06-23) |
| `0x80072404` | 54 | `send_LMP_NOT_ACCEPTED` | send LMP NOT ACCEPTED | low (named by Kovah, purpose unclear) |
| `0x8007243c` | 56 | `send_LMP_ACCEPTED` | send LMP ACCEPTED | low (named by Kovah, purpose unclear) |
| `0x80072648` | 70 | `LMP_unknown_else` | LMP unknown else | low (named by Kovah, purpose unclear) |
| `0x80072ff8` | 452 | `LMP_SCO_LINK_REQ_0x17_handler` (was unnamed `FUN_80072ff8`) | SCO link parameter negotiation handler; validates D_sco/T_sco-style packet params via vendor callbacks, accepts via `send_LMP_ACCEPTED(...,0x17,...)`, negotiates via `send_LMP_pkt` with PDU opcode byte `0x17`, finalizes via `get_status_bits_by_LMP_Opcode(0x17,0)` — triple-confirmed LMP_SCO_LINK_REQ (0x17); also calls the `0x80072924`/`0x80072bac` AFH/LAP-table pair with SCO-handle args (consulting channel table during SCO setup) | **high** (decompiled+renamed, cold-triage pass 7 2026-06-23) |
| `0x800734c4` | 466 | `LMP_power_control_RSSI_trigger` (was unnamed `FUN_800734c4`) | RSSI-driven power-control trigger; compares `return_RSSI_value()` against per-connection thresholds, gates on `field_0xdc` tri-state + `field_0x211` outstanding-request guard + config bit `_x7a_enable_LMP_POWER_REQ_RES_and_CLK_ADJ`, sends opcode `0x1f`/`0x20` (LMP_INCR_POWER_REQ/LMP_DECR_POWER_REQ) via `send_LMP_pkt` | **high** (decompiled+renamed, cold-triage pass 7 2026-06-23) |
| `0x8007276c` | 424 | `FUN_8007276c` | AFH/LAP channel-classification bitmap builder; iterates `_x142_LAP` table slots for a handle match, clears+rebuilds a 36-byte channel bitmap in windows derived from the matched slot, calls `FUN_80072694`; not renamed (own protocol role not cross-confirmed) but corroborates the `0x80072bac`/`0x80072924` pair's AFH/LAP-table identity | medium (decompiled, cold-triage pass 7 2026-06-23) |
| `0x80072bac` | 814 | `FUN_80072bac` | AFH/LAP-keyed table registration variant; operates on `PTR_struct_of_at_least_0x300_size_80072ee0->_x142_LAP[...]`; directly calls `FUN_8007276c` inline (closes the Pass 7 open question of who calls it) | medium-high (re-decompiled, cold-triage pass 9 2026-06-23) |
| `0x80072924` | 628 | `FUN_80072924` | Sibling of `0x80072bac`: same `0x300`-size struct, same field-offset pattern, same callee set (`FUN_80071a84`, `FUN_80072694`, `possible_logger_called_if_no_patch3`); also calls already-HIGH `possible_logging_function__var_args` — strongly confirms the sibling-pair hypothesis | medium-high (re-decompiled, cold-triage pass 9 2026-06-23) |
| `0x8007718c` | 524 | `FUN_8007718c` | Slot-instant/clock-window comparator: mod-1250 wraparound distance check against a threshold field, updates 3-bit status-enum fields, logs via `possible_logging_function__var_args` (event-class 0x71) on threshold-exceeded; exact LMP/HCI procedure not cross-confirmed | medium (decompiled, cold-triage pass 7 2026-06-23) |
| `0x800731bc` | 368 | `LMP_SCO_LINK_REQ_0x17_modify_handler` (was unnamed `FUN_800731bc`) | SCO slot-timing window validity check; accept path calls `send_LMP_ACCEPTED(...,0x17,...)`, negotiate path builds 11-byte PDU with first byte `0x17` via `send_LMP_pkt(...,0xb,...)` then `get_status_bits_by_LMP_Opcode(0x17,0)` — triple-confirmed LMP_SCO_LINK_REQ (0x17) modify/renegotiate-path sibling of `0x80072ff8` | **high** (decompiled+renamed, cold-triage pass 8 2026-06-23) |
| `0x80076a20` | 348 | `crypto_bignum_multiply_square_v1` (was unnamed `FUN_80076a20`) | Generic multi-word bignum multiply: Knuth/schoolbook multiply with 64-bit accumulation + carry propagation, followed by doubling pass and squaring pass; pure arithmetic, no LMP/HCI logic — likely ECDH P-192/P-256 SSP infrastructure | **high** (decompiled+renamed, cold-triage pass 8 2026-06-23) |
| `0x800767ec` | 278 | `crypto_bignum_multiply_variable_len` (was unnamed `FUN_800767ec`) | Variable-length schoolbook multiply: trims leading/trailing zero digits from both digit-array inputs, zeroes output, performs same 64-bit-accumulator carry-chain multiply as `0x80076a20` — same bignum-multiply family | **high** (decompiled+renamed, cold-triage pass 8 2026-06-23) |
| `0x80074940` | 672 | `FUN_80074940` | 5-case dispatch via fn-ptr tables keyed by a `1<<n` bitmask, calls `0x800747b0` for case 2, touches `0x1ac_struct_array` fields `+0x284`/`+0x28c`, calls `FUN_80058680` (CRC/checksum helper); reads as an LMP feature/parameter-negotiation response dispatcher; no opcode literal | medium-high (re-decompiled, cold-triage pass 9 2026-06-23) |
| `0x800747b0` | 390 | `FUN_800747b0` | TLV-style byte-stream parser (tag 1/5/0x11 fields), falls through to `possible_logging_function__var_args(2,0x3c,0x31d,...)` on overflow; resembles LMP extended-feature-page TLV parsing but no opcode literal/named caller | medium-high (decompiled, cold-triage pass 8 2026-06-23) |
| `0x800791d0` | 608 | `FUN_800791d0` | Populates a 0x2c-byte struct from a bit-packed page format (5-bit count + bitfields matching LMP feature-page octet layout); logs via `possible_logging_function__var_args(3,0x8e,...)`; fallback path (top 3 bits of size byte nonzero) dispatches through indirect fn-ptr `PTR_DAT_80079438` instead — likely the fast-path of a larger feature-page parser with slow-path fallback; structurally close to `LMP_features_validator` but unconfirmed | medium-high (re-decompiled, cold-triage pass 9 2026-06-23) |
| `0x80078fdc` | 344 | `FUN_80078fdc` | Low-level bit-packing setter on `0x1ac_struct_array` fields `+0x44..+0x49`; no opcode/caller evidence | medium (decompiled, cold-triage pass 8 2026-06-23) |
| `0x800796b8` | 336 | `FUN_800796b8` | Bit-stream serializer/feeder (calls `FUN_8007967c` + `possible_logging_function__var_args(3,0x8e,...)`); likely serialize counterpart to `0x800791d0`'s parse | medium (decompiled, cold-triage pass 8 2026-06-23) |
| `0x800745d8` | 308 | `FUN_800745d8` | Near-identical skeleton to `0x800747b0` (same loop/logging signature) but tag-matches against a name/string table via XOR-compare+memcmp rather than extracting feature fields — tag-matching sibling/variant of `0x800747b0` | medium-high (decompiled, cold-triage pass 8 2026-06-23) |
| `0x80071138` | 306 | `FUN_80071138` | Connection-slot allocation orchestration using named helpers (`look_for_non_matching_bdaddr_bos_index_i_e__free_connection_slot`, `set_check_for_1_to_1`, `set_bos_bosi__0xb2_index_arg2`, `HCI_EVT_0x500_FUN_800707dc`, `called_by_fHCI_Remote_Name_Request_6_nop_if_not_patched_`); clear remote-name-request/slot-allocator role via callees, no opcode literal of its own | medium-high (decompiled, cold-triage pass 8 2026-06-23) |
| `0x80077020` | 240 | `FUN_80077020` | HW register write sequence (offsets `0x26e`/`0x274` via indirect callback) consistent with SCO/eSCO link parameter programming; no opcode-literal confirmation | medium (decompiled, cold-triage pass 8 2026-06-23) |
| `0x80077508` | 230 | `FUN_80077508` | HW register init sequence (offsets `0x14,0x38,0x20,0x50,0x58,0x5c,0x60,0x64,0x68,0x6c,0x70` via `FUN_800773d8`); calls named `VSC_0xfca1_FUN_80077474`, confirming HCI VSC 0xFCA1-related hardware init | medium-high (decompiled, cold-triage pass 8 2026-06-23) |
| `0x800779d0` | 126 | `FUN_800779d0` | Pure 4-tap moving-average/FIR smoothing filter (`(p[i]+p[i+1]+p[i+2]+p[i+3])>>2`); generic signal-smoothing utility, not protocol-specific; decompiled while attempting to cross-confirm the `0x8007814c`/`0x80077bcc` quantizer pair — ruled out as an opcode-literal anchor | medium (decompiled, cold-triage pass 8 2026-06-23) |
| `0x80073348` | 362 | `crypto_state_machine_finalizer` | eSCO/encryption state-machine finalizer; post-processing for crypto handshake completion | high (decompiled, batch pass 3a 2026-06-23) |
| `0x80073b74` | 348 | `HCI_Disconnect_on_error` | Terminates connection on failure condition + cleanup chain | **high** (decompiled, batch pass 3b 2026-06-23) |
| `0x80074c8c` | 232 | `LMP_CH__0x3ed` | LMP channel sub-protocol (opcode 0x3ed) handler; link-layer negotiation | high (decompiled, batch pass 3a 2026-06-23) |
| `0x80074d84` | 14 | `set_two_global_ptrs` | set two global ptrs | low (named by Kovah, purpose unclear) |
| `0x80074dfc` | 42 | `called_by_unknown_fptr_indexA_2` | called by unknown fptr indexA 2 | low (named by Kovah, purpose unclear) |
| `0x80074e38` | 50 | `possible_logger_called_if_no_patch2` | possible logger called if no patch2 | low (named by Kovah, purpose unclear) |
| `0x80074e84` | 38 | `called_by_unknown_fptr_indexA_1` | called by unknown fptr indexA 1 | low (named by Kovah, purpose unclear) |
| `0x80074eb4` | 42 | `unknown_fptr_indexA` | unknown fptr indexA | low (named by Kovah, purpose unclear) |
| `0x80074ee0` | 64 | `function_that_uses_Logger_string` | function that uses Logger string | low (named by Kovah, purpose unclear) |
| `0x80074f38` | 94 | `possible_logger_called_if_no_patch1` | possible logger called if no patch1 | low (named by Kovah, purpose unclear) |
| `0x80074fa8` | 204 | `possible_logging_function?_var_args` | possible logging function? var args — see `conn_record_subsystem`, `interrupt_vectors` | high (decompiled+documented) |
| `0x80075084` | 402 | `struct_array_accessor_default` | Default-name struct accessor for config array; factory-defaults provider | **high** (decompiled, batch pass 3b 2026-06-23) |
| `0x80075324` | 224 | `func1_structs_at_0x80100000` | ROM struct accessor #1 for config base 0x80100000; reads/writes configuration fields | **high** (decompiled, batch pass 3b 2026-06-23) |
| `0x800754c4` | 22 | `struct_field_accessor_0x80100000` | Simple RAM/config-base struct field accessor (data-plane, low priority) | high (decompiled, batch pass 3a 2026-06-23) |
| `0x80075540` | 258 | `uninteresting_if_0x80100000_conditional` | Data-plane config validator; conditional on RAM 0x80100000 field (non-LMP, low priority) | **high** (decompiled, batch pass 3b 2026-06-23) |
| `0x80075650` | 102 | `func4_that_uses_structs_at_0x80100000` | func4 that uses structs at 0x80100000 | low (named by Kovah, purpose unclear) |
| `0x800756c0` | 62 | `func5_that_uses_structs_at_0x80100000` | func5 that uses structs at 0x80100000 | low (named by Kovah, purpose unclear) |
| `0x80075704` | 34 | `func6_that_uses_structs_at_0x80100000` | func6 that uses structs at 0x80100000 | low (named by Kovah, purpose unclear) |
| `0x8007572c` | 106 | `func7_that_uses_structs_at_0x80100000` | func7 that uses structs at 0x80100000 | low (named by Kovah, purpose unclear) |
| `0x8007579c` | 188 | `func8_that_uses_structs_at_0x80100000` | func8 that uses structs at 0x80100000 | low (named by Kovah, purpose unclear) |
| `0x80075948` | 258 | `memcpy_to_MMIO_for_packet_send` | Packet transmit helper; copies data to MMIO for frame transmission (peripheral write path) | **high** (decompiled, batch pass 3b 2026-06-23) |
| `0x80075e34` | 106 | `possible_logger_called_if_no_patch4_recursive_to_possible_logger` | possible logger called if no patch4 recursive to possible logger | low (named by Kovah, purpose unclear) |
| `0x800761f4` | 116 | `LMP__25B_meat` | LMP  25B meat | low (named by Kovah, purpose unclear) |
| `0x800762f4` | 852 | `crypto_state_machine_loop_handler` | Large do-while crypto state transitions; post-exchange validation + error recovery | **high** (decompiled, batch pass 3b 2026-06-23) |
| `0x8007666c` | 22 | `unknown_fptr_index1` | unknown fptr index1 | low (named by Kovah, purpose unclear) |
| `0x80076bd8` | 48 | `swap_byte_order` | swap byte order | low (named by Kovah, purpose unclear) |
| `0x80077474` | 130 | `VSC_0xfca1_FUN_80077474` | Vendor-specific command 0xfca1 handler; small struct-init + conditional dispatch (consistent with VSC param-parsing pattern, exact param semantics not cross-confirmed) | medium (decompiled, batch pass 5 2026-06-23) |
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
| `0x80004fd8` | 434 | `top_level_link_event_status_dispatcher_loop` | master status-word dispatcher loop — root of the whole packet-type/role-switch/eSCO/teardown supercluster — see `region_0x80000000` | high (decompiled+documented) |
| `0x800051d4` | 356 | `conn_state2_packet_type_reprogram_or_credit_dispatch` | state-2-gated per-connection packet-type reprogram or credit-scheduler dispatch — see `region_0x80000000` | high (decompiled+documented) |
| `0x80005368` | 360 | `bitfield_class_status_callback_dispatcher_5way` | 5-way bitfield-class-to-callback status dispatcher — see `region_0x80000000` | high (decompiled+documented) |
| `0x800054e8` | 226 | `conn_3slot_oneshot_config_apply_and_log` | per-connection one-shot config-apply-and-log sweep over 3 slots — see `region_0x80000000` | high (decompiled+documented) |
| `0x800055ec` | 312 | `sco_esco_param_pingpong_queue_rotator` | SCO/eSCO parameter ping-pong queue rotator (8-entry ring) — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000576c` | 524 | `conn_field_swap_and_notify_dispatcher_3_4` | per-connection field-swap-detect-and-notify dispatcher — see `region_0x80000000` | high (decompiled+documented) |
| `0x80005998` | 336 | `sco_esco_timing_field_diagnostic_logger` | conditional SCO/eSCO timing-field diagnostic logger — see `region_0x80000000` | high (decompiled+documented) |
| `0x80005b30` | 564 | `sco_esco_slot_timing_offset_calc_variant1` | complex-path SCO/eSCO slot-timing-offset calculator — see `region_0x80000000` | high (decompiled+documented) |
| `0x80005d80` | 412 | `sco_esco_slot_timing_offset_calc_variant2` | simple-path SCO/eSCO slot-timing-offset calculator — see `region_0x80000000` | high (decompiled+documented) |
| `0x80005f34` | 102 | `sco_esco_slot_timing_offset_dispatch_gate` | dispatch gate between the two slot-timing-offset calc variants — see `region_0x80000000` | high (decompiled+documented) |
| `0x80005fb8` | 906 | `conn_packet_type_apply_and_codec_table_sync` | packet-type-apply-plus-codec-table-sync routine — see `region_0x80000000` | high (decompiled+documented) |
| `0x80006384` | 758 | `conn_status_word_state_machine_dispatcher` | per-connection status-word-driven 4-state machine dispatcher — see `region_0x80000000` | high (decompiled+documented) |
| `0x80006cb8` | 78 | `batch_conn_status_word_sweep_3entry` | 3-slot batch dispatcher for `FUN_800066fc` — see `region_0x80000000` | high (decompiled+documented) |
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
| `0x80005b08` | 36 | `guarded_trampoline_to_FUN_800131e4` | guarded trampoline to fixed-arg call — see `region_0x80000000` | high (decompiled+documented) |
| `0x800066ac` | 76 | `bounded_retry_loop_dispatch_0x3` | bounded (3-iteration) fetch-and-process retry loop — see `region_0x80000000` | high (decompiled+documented) |
| `0x800066fc` | 1408 | `conn_class_mode_apply_and_log` | per-connection (slot,mode) class/mode-change apply-and-log routine — see `region_0x80000000` | high (decompiled+documented) |
| `0x80006d0c` | 1482 | `conn_class_mode_apply_and_log_variant2` | sibling of `conn_class_mode_apply_and_log` for packed-status-word calling convention — see `region_0x80000000` | high (decompiled+documented) |
| `0x80007330` | 624 | `conn_index_status_bit_apply_and_log` | single-connection-index status-bit validate-and-activate handler — see `region_0x80000000` | high (decompiled+documented) |
| `0x800075dc` | 110 | `bump_retry_or_timeout_counter_and_log` | directional retry-counter bump-or-early-log leaf — see `region_0x80000000` | high (decompiled+documented) |
| `0x80007654` | 848 | `conn_credit_or_counter_update_with_log` | per-connection credit/counter update-and-log routine — see `region_0x80000000` | high (decompiled+documented) |
| `0x800079f0` | 238 | `conn_rssi_quality_history_update` | per-connection RSSI/link-quality rolling-history updater — see `region_0x80000000` | high (decompiled+documented) |
| `0x80007af0` | 1978 | `ring_buffer_event_drain_dispatch_loop` | central ring-buffer-event consumer for per-connection event class — see `region_0x80000000` | high (decompiled+documented) |
| `0x80008328` | 176 | `conn_field_increment_and_cleanup_dispatch` | non-ring single-shot counterpart of `ring_buffer_event_drain_loop_variant2` — see `region_0x80000000` | high (decompiled+documented) |
| `0x800083ec` | 406 | `ring_buffer_event_drain_loop_variant2` | drains a per-connection-class quota ring and applies deltas — see `region_0x80000000` | high (decompiled+documented) |
| `0x800085a4` | 1138 | `top_level_link_event_status_dispatcher_loop2` | second master status-word dispatch loop, sibling of `top_level_link_event_status_dispatcher_loop` — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000e4bc` | 592 | `link_status_bitmask_event_dispatcher` | per-bit link/connection status-change dispatcher feeding AFH toggle + status report — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000e764` | 202 | `config_flag_gated_status_log_and_propagate` | flag-gated status log-and-propagate, feeds `feature_bit_status_word_propagator` — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000e8c0` | 202 | `optimized_memmove` | overlap-aware optimized memmove, sibling of `optimized_memcpy`/`memset` — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000ea1c` | 144 | `status_pair_ring_push_with_overflow_trap` | 2-word ring push with conditional fatal-trap halt — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000ead4` | 172 | `status_word_consume_and_dispatch_to_ring_or_download` | status-word consumer dispatching to download/callback/ring-push paths — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000eba0` | 80 | `indexed_register_rw_with_dead_sentinel` | generic indexed byte/halfword register accessor, `0xdead` rejection sentinel — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000ebfc` | 48 | `dual_fptr_dispatch_by_flag_wrapper` | flag-selected dual-callback dispatch wrapper — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000ec34` | 38 | `set_byte_and_masked_lsb_pair` | trivial 2-field byte/masked-lsb setter — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000ec64` | 60 | `conn_list_field_match_count_and_fallback_call` | list-scan-with-no-match-fallback helper — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000ed04` | 42 | `build_fixed_event_0x201e_and_send` | fixed-format 3-byte-payload event builder/sender for event 0x201e — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000ed30` | 98 | `register_rw_dispatch_with_log` | logged register read/write dispatch wrapper — see `region_0x80000000` | high (decompiled+documented) |
| `0x8000eda0` | 700 | `generic_status_field_get_set_dispatcher` | large opcode-routed multi-field get/set dispatcher — see `region_0x80000000` | high (decompiled+documented) |
| `0x8001ca94` | 60 | `send_evt_HCI_Inquiry_Response_Notification` | builds 3-byte LAP + RSSI-delta payload, sends HCI event 0x56 — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001cad4` | 36 | `send_evt_HCI_Connectionless_Peripheral_Broadcast_Channel_Map_Change` | 10-byte payload from fixed data ptr, HCI event 0x55 — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001cafc` | 20 | `send_evt_HCI_Peripheral_Page_Response_Timeout` | no-payload HCI event 0x54 — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001cb10` | 74 | `send_evt_HCI_Connectionless_Peripheral_Broadcast_Timeout` | dual-source 7-byte payload selected by bool param, HCI event 0x52 — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001cb68` | 24 | `send_evt_HCI_Synchronization_Train_Complete` | 1-byte status payload, HCI event 0x4f — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001cb80` | 70 | `send_evt_HCI_Triggered_Clock_Capture` | feature-bit-gated 9-byte clock/flag payload, HCI event 0x4e — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001cbcc` | 52 | `send_evt_HCI_Truncated_Page_Complete` | 6-byte BD_ADDR payload by bos index, HCI event 0x53 — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001cc04` | 110 | `send_evt_HCI_Synchronization_Train_Received` | 0x1d-byte BD_ADDR/clock/interval/map payload, HCI event 0x50 — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001cc80` | 226 | `send_evt_HCI_Connectionless_Peripheral_Broadcast_Receive` | guard-fn-gated variable-length payload or logger fallback, HCI event 0x51 — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001cd74` | 586 | `initialize_0x28_sized_struct` | per-connection LMP/QoS feature-capability struct initializer, gated by 5 config_base feature bits — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001cfc4` | 110 | `rssi_threshold_delta_for_bos_index` | RSSI-vs-threshold-pair signed delta lookup by bos index (renamed from `FUN_8001cfc4`) — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001d03c` | 48 | `log_hci_evt_0x1fc_if_no_patch3` | debug-logger wrapper tagging HCI event 0x1fc (renamed from `FUN_8001d03c`) — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001d1bc` | 24 | `send_evt_HCI_Hardware_Error` | 1-byte payload, HCI event 0x10 — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001d1d4` | 34 | `send_evt_HCI_Flush_Occurred` | 2-byte handle payload, HCI event 0x11 — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001d1f8` | 74 | `send_evt_HCI_Connection_Complete` | simple-path 11-byte payload, HCI event 0x03 (sibling of the role-switch-aware variant at 0x8001d844) — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001d244` | 58 | `send_evt_HCI_Loopback_Command` | variable-length (≤253B) payload, HCI event 0x19 — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001d280` | 130 | `send_evt_0x21_HCI_Flow_Specification_Complete` | 4-field QoS record byte-swapped via `copy_bytes_in_LSB_order`, HCI event 0x21 — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001d308` | 122 | `send_evt_0x0D_HCI_QoS_Setup_Complete` | 4-field QoS record (rate/peak-BW/latency/delay-variation), HCI event 0x0d — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001d388` | 70 | `send_evt_HCI_Role_Change` | bos-index-gated 8-byte payload, HCI event 0x12 — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001d3d4` | 36 | `send_evt_HCI_Max_Slots_Change` | 3-byte payload, HCI event 0x1b — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001d3f8` | 44 | `send_event_HCI_Connection_Packet_Type_Changed` | 5-byte payload, HCI event 0x1d — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001d424` | 76 | `called_by_fHCI_Read_LMP_Handle_send_evt_HCI_Command_Complete` | 0xb-byte Command Complete payload, num_cmd_pkts defaults to 1, HCI event 0x0e — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001d474` | 44 | `send_evt_HCI_Read_Clock_Offset_Complete` | 5-byte payload, HCI event 0x1c — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001d534` | 48 | `send_evt_HCI_Read_Remote_Supported_Features_Complete` | 11-byte payload (status+handle+8-byte features), HCI event 0x0b — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001d564` | 74 | `send_evt_HCI_Remote_Name_Request_Complete` | 0xff-byte payload (BD_ADDR + name buffer, possibly EIR/FHS dual-use), HCI event 0x07 — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001d5b4` | 68 | `send_evt_0x14_HCI_Mode_Change` | 6-byte payload, HCI event 0x14 — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001d5fc` | 460 | `send_evt_HCI_Disconnection_Complete` | full teardown orchestrator (codec/role/index bookkeeping) + conditional HCI event 0x05 — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001d804` | 64 | `send_evt_HCI_Connection_Request` | 10-byte payload (BD_ADDR+class+link_type), HCI event 0x04 — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001d844` | 178 | `send_evt_HCI_Connection_Complete` | role-switch-aware variant with bos-index bookkeeping, HCI event 0x03 (sibling of the simple variant at 0x8001d1f8) — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001d904` | 256 | `send_evt_HCI_Inquiry_Result_or_HCI_Inquiry_Result_with_RSSI` | multi-entry inquiry-result payload builder, HCI event 0x02 or 0x22 — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001da0c` | 40 | `send_evt_HCI_Inquiry_Complete` | 1-byte status payload + inquiry-session cleanup, HCI event 0x01 — see `region_0x80010000` | high (decompiled+documented) |
| `0x8001da3c` | 438 | `send_evt_HCI_Number_Of_Completed_Packets` | 4-source variable-length (handle,count) pair list, HCI event 0x13 — see `region_0x80010000` | high (decompiled+documented) |
| `0x80033794` | 578 | `FUN_80033794` | Complex power/connection validation gate — multi-tier state/capability checker; config+dd bit5 gate, per-connection capacity loop, nested bit checks on large2 fields; returns bool (allowed/blocked). See `region_0x80030000` Pass 7 | high (decompiled+documented, Pass 7 2026-06-25) |
| `0x8003229c` | 566 | `acl_packet_ring_buffer_manager` | Manages circular queue of ACL packets; mask-based ringbuf at 0x8012bxxx stride; writes global flags/counters; indirect calls @ 0x80120f80/0x80120f0c. See `region_0x80030000` Pass 6 | high (decompiled+documented, Pass 6 2026-06-25) |
| `0x8003d630` | 340 | `connection_state_manager` | Connection record state machine (pending/active transitions); per-connection stride 0x28 struct array; ROM pre-check; counter decrements; SCO type-2 path (VSC 0x260/0x27e, BB reg 0xe0 config); HCI evt 0xfa logging. See `region_0x80030000` Pass 7 | high (decompiled+documented, Pass 7 2026-06-25) |
| `0x80035b4c` | 352 | `param_dispatch_with_rom_calls` | Parameter-based dispatch to ROM handlers; state flags @ 0x8012303x/0x8012305x; ROM calls FUN_80033744/FUN_8003336f4/FUN_80034ccc/FUN_80034d88; gates on DAT_80120f80/DAT_80120cb0. See `region_0x80030000` Pass 6 | high (decompiled+documented, Pass 6 2026-06-25) |
| `0x8003c7cc` | 310 | `hw_register_config_with_timeout` | Baseband register configuration with timeout-based polling; config reads @ 0x8012xxfe/0xff; BB regs 0x6c/0xd8; ROM write via FUN_80009694 timeout wrapper; VSC 0xfd49 call; config bit 0x1d0 gate. See `region_0x80030000` Pass 6 | high (decompiled+documented, Pass 6 2026-06-25) |
| `0x80039f54` | 426 | `lmp_power_regulator` | TX power level + PHY configuration dispatcher; config_base+0x278 bit5 gate; param-based vs struct-based power level selection (param_1 < 2 or big_ol_struct); BB regs 0x49/0x72 via ROM r/w; returns 1 if config+0xdc bit3 set, else 0. See `region_0x80030000` Pass 7 | high (decompiled+documented, Pass 7 2026-06-25) |
| `0x800381fc` | 552 | `lmp_event_counter_dual_rate_limited_retry` | Per-connection event/ACK counter with periodic rate-limited retry-flag setter; validates conn index/role match, increments counters on bitmask-classified events (0x4410/0x8900), gates two independent modulo-counter retry triggers behind config flags. See `region_0x80030000` Pass 8 | high (decompiled+documented, Pass 8 2026-06-25) |
| `0x8003e760` | 504 | `connection_teardown_finalize_and_reset` | Connection-record cleanup/finalize handler over stride-0x88 table (shared with `0x8003e400`/`0x8003e294`); resets fields +0x70-0x82, conditionally calls FUN_8003e1d4+FUN_8002bb50+FUN_8003e648 cleanup chain, logs via possible_logger_called_if_no_patch3. See `region_0x80030000` Pass 8 | high (decompiled+documented, Pass 8 2026-06-25) |
| `0x8003894c` | 494 | `slot_timing_delta_calc_and_log` | Computes RX/TX slot timing delta from a tick-source fptr using config-dependent shift (field_0x1c/0x1e vs field_0x1a); dispatches recalc hook FUN_80043984; conditionally logs extended timing/connection-state snapshot (log codes 0x2ef/0x29f). See `region_0x80030000` Pass 8 | high (decompiled+documented, Pass 8 2026-06-25) |
| `0x800386d0` | 488 | `AFH_channel_map_table_builder` | Builds 79-entry (0x4f) Adaptive-Frequency-Hopping channel map from a circular per-channel classification table; per-channel value = (class*0x20 + cfg_field453*0x10 + signed_delta)*8; pushes table to hardware via FUN_800786dc. 79-entry size matches BT's 79 RF channels. See `region_0x80030000` Pass 8 | high (decompiled+documented, Pass 8 2026-06-25) |
| `0x8003a180` | 458 | `clock_trim_calibration_measure_apply` | Crystal/clock-trim calibration loop: averages 16 reads of status register 0x7f while toggling regs 0x5a/0x45/0x57/PTR_DAT_8003a354 mode-select; computes correction offset `((sum>>6)+0x80)&0xff` clamped to thresholds; applies via FUN_80038e24. See `region_0x80030000` Pass 8 | high (decompiled+documented, Pass 8 2026-06-25) |
| `0x80035454` | 456 | `link_mode_change_state_machine` | Core link-mode/role-switch procedure dispatcher: busy(0xf)/idle(0xff) status convention via FUN_80033a04/FUN_80033ae4; VSC_0xfc11 call; IRQ-disabled critical section; applies params via FUN_80033c98/FUN_80035214; cleanup via FUN_80034d88 on failure. See `region_0x80030000` Pass 8 | high (decompiled+documented, Pass 8 2026-06-25) |
| `0x800364c8` | 408 | `dual_slot_buffer_reassignment_on_role_switch` | Swaps/clears one of two per-role buffer slots (stride-0x84 table) when a logical link's owning role changes; tracks 0/1/2 idle/pending/done transition state; notifies via FUN_80017a04 + logs. See `region_0x80030000` Pass 8 | high (decompiled+documented, Pass 8 2026-06-25) |
| `0x8003a38c` | 394 | `int64_arith_op_and_signed_shift_right` | Generic 64-bit (as two 32-bit halves) add/sub/mul/div selector (param_4 0-3) + variable-width signed right-shift via FUN_80038c94 sign-extend helper; div-by-zero traps(7). MIPS16e 64-bit-op runtime helper. See `region_0x80030000` Pass 8 | high (decompiled+documented, Pass 8 2026-06-25) |
| `0x8003a824` | 388 | `AFH_channel_map_hw_register_programmer` | Unpacks 2-bit-per-channel classification codes (3-tap bit shifts 7/11/15) from a packed byte array and programs them into BB registers via FUN_8003bd94 + masked read-modify-write; companion to `AFH_channel_map_table_builder`. See `region_0x80030000` Pass 8 | high (decompiled+documented, Pass 8 2026-06-25) |
| `0x8003e400` | 382 | `ACL_fragment_dequeue_and_credit_consumer` | Dequeues up to 4 entries from a 12-entry ring buffer (stride-0x88 table, shared with `0x8003e760`/`0x8003e294`) tracking byte-credit consumption per entry; advances ring index on full-consume, marks last fragment with 0x80 flag, kicks via FUN_80014e40. See `region_0x80030000` Pass 8 | high (decompiled+documented, Pass 8 2026-06-25) |
| `0x80037460` | 372 | `LMP_link_supervision_tick_scheduler` | Periodic tick dispatcher for the LMP/link-supervision state machine; calls `LMP_CH__0x3ee__case2_else_2_FUN_80011d9c` and FUN_8003cb80 conditionally; 3-bit mode field (+0x164>>7) state machine branching to FUN_8003c94c/FUN_8003ca28; extensive timing-state debug log. See `region_0x80030000` Pass 8 | high (decompiled+documented, Pass 8 2026-06-25) |
| `0x80034ec4` | 370 | `LMP_power_and_clk_adj_procedure_orchestrator` | Orchestrates LMP_POWER_CONTROL_REQ/LMP_CLK_ADJ (re)triggering per-connection, gated by `config->_x7a_enable_LMP_POWER_REQ_RES_and_CLK_ADJ` bit1; start/cancel pattern via FUN_80055ddc/FUN_80055e50; calls FUN_80054b14 and FUN_8004f240 sibling paths. See `region_0x80030000` Pass 8 | high (decompiled+documented, Pass 8 2026-06-25) |
| `0x8003c41c` | 368 | `per_connection_hw_buffer_setup_with_patch_hook` | Configures TX/RX-path BB registers 0x69/0x6a/0x6f (buffer base/size) keyed by handle>>3; installs a function-pointer hook into DAT_8003c5a8; brackets writes with VSC reg 0x40 enable/disable; counterpart of `0x8003c2b4`. See `region_0x80030000` Pass 8 | high (decompiled+documented, Pass 8 2026-06-25) |
| `0x80032ec4` | 362 | `config_triplet_hw_register_init_with_power_gate` | Applies config-blob defaults (3 16-bit fields 0xcc-0xd1) when zero, writes them to BB regs 0x11c/0x11e/0x120; conditionally writes reg 0x21c when config+0xd8 bit5 (LMP-power-mode-enable, confirmed in `FUN_80033794`) is clear. See `region_0x80030000` Pass 8 | high (decompiled+documented, Pass 8 2026-06-25) |
| `0x80039de4` | 354 | `calibration_table_populate_via_lookup_fptr` | Populates a 17-entry calibration table (8x3 parallel arrays + 9 singles) via a shared indirect lookup/transform fptr indexed by table[2..0xd]; debug-logs last 7 entries when table[0x36] bit1 set. See `region_0x80030000` Pass 8 | high (decompiled+documented, Pass 8 2026-06-25) |
| `0x80035cd4` | 342 | `power_level_smoothing_filter_feeding_param_dispatch` | Computes a smoothed power/RSSI-like value from per-connection field_0x38 + antenna-path correction (field_0x2a0/0x2a1) averaged against field106_0x94; on calibration-mode + threshold-exceeded, calls already-confirmed `param_dispatch_with_rom_calls` (`0x80035b4c`) — resolves one of its 2 documented callers. See `region_0x80030000` Pass 8 | high (decompiled+documented, Pass 8 2026-06-25) |
| `0x80038fcc` | 330 | `packet_type_to_hw_code_translator_4link` | Unpacks a 16-bit value into 4 nibble packet-type codes (one per link/slot), clamps invalid code 7→6, programs BB regs 0x10/0x11/0x12 per-link then packs 4 derived sub-codes into reg(3,0x59,1) at bit offsets 0/3/6/9. See `region_0x80030000` Pass 8 | high (decompiled+documented, Pass 8 2026-06-25) |
| `0x8003e294` | 330 | `piconet_slot_collision_avoidance_scheduler` | Finds highest-priority active connection of type 0x101 among a stride-0x88 3-entry table (shared with `0x8003e760`/`0x8003e400`); computes BD_ADDR-derived slot offsets for current vs. conflicting connection; rounds the requested slot up to the next non-conflicting multiple of param_3. See `region_0x80030000` Pass 8 | high (decompiled+documented, Pass 8 2026-06-25) |
| `0x8003c2b4` | 324 | `hw_register_setup_with_patch_hook_variant2` | Near-twin of `0x8003c41c` configuring the counterpart register set (0x6b/0x6c/0x6d/0x6e/0x68) and installing a hook into DAT_8003c410; gated by config field285 bit0 + a derived flag bit5; calls FUN_8003c19c(3,3,0,0xf). See `region_0x80030000` Pass 8 | high (decompiled+documented, Pass 8 2026-06-25) |
| `0x8003fb5c` | 304 | `test_pattern_buffer_fill_or_hw_mode_select` | BT DUT/loopback test-pattern generator: fills a connection's TX buffer with one of {0x00,0xFF,0x55,0x0F} (all-zero/all-one/alternating-bit/alternating-nibble) via memset, or a bit-rotated PRBS-style merge for pattern mode 4; alternate branch selects an equivalent HW-native mode code (`sVar6<<9\|0x147`) when config[1].field7_0x7 is set. See `region_0x80030000` Pass 8 | high (decompiled+documented, Pass 8 2026-06-25) |

---

## Unnamed functions (2050, originally 2278)

The remaining 2059 functions in the `rom` block (2739 total − 679 named − 1
reclassified non-function) carry Ghidra's auto-generated `FUN_8000xxxx`
label and have not been individually triaged. Per Phase 9 scoping, giving
each of these a real name and purpose is explicitly out of scope for this
doc — it's the rest of Phase 9's ongoing, best-effort work. This section
satisfies "every function" at the index/coverage level via aggregate stats
instead of 2278 rows of "unknown."

**Confidence: unanalyzed** (for all 2059, as a single flag — no further
granularity is meaningful until individual triage happens).

### Address-range distribution (by 0x10000-aligned region)

| Address range | Unnamed function count | % of unnamed total |
|---|---|---|
| `0x80000000`–`0x8000ffff` | 88 (307 − 12 pass-1 − 19 pass-2 − 74 pass-3 − 27 pass-4 − 31 pass-5 − 13 pass-6 − 12 pass-7 − 12 pass-8 − 18 pass-9 − 1 reclassified non-function, 2026-06-22 — **region's gap-sweep now COMPLETE; the remaining 88 are outside this doc's two-gap scope, e.g. the excluded interrupt-vector sub-range**) | 4.3% |
| `0x80010000`–`0x8001ffff` | 257 (263 authoritative-recount baseline, 2026-06-22 region-0x80010000 pass 1 — supersedes the original 268 estimate, see that region doc's scope note — minus 2 pass-1 renames minus 4 pass-2 renames) | 12.5% |
| `0x80020000`–`0x8002ffff` | 321 | 15.6% |
| `0x80030000`–`0x8003ffff` | 270 (290 − 20 Pass-8 renames, 2026-06-25) | 14.1% (stale %, not recomputed against new 1790 total) |
| `0x80040000`–`0x8004ffff` | 301 (305 at Pass-3 recount − 2 Pass-3 renames − 1 Pass-3-continuation rename − 1 Pass-4 rename, 2026-06-23) | 14.7% |
| `0x80050000`–`0x8005ffff` | 345 (349 − 3 Pass-6 renames − 1 Pass-7 rename, 2026-06-23) | 16.9% |
| `0x80060000`–`0x8006ffff` | 0 (238 − 97 Pass-2-3 renames − 141 adjustment per phase-9 reassessment, 2026-06-24 — **region now COMPLETE, all 335+ functions accounted for, 97 named HIGH**) | 0.0% |
| `0x80070000`–`0x8007ffff` | 191 (193 − 2 Pass-6 renames, 2026-06-23) | 10.5% |
| **Total** | **1810** | **100%** |

(Note: the doc-wide "Unnamed (`FUN_*`) functions" summary metric above still
reads 2053 — derived as `2057 − 4` from the prior baseline's running total,
not yet reconciled against this table's fresh 2048 recount. The 5-function
gap is the same authoritative-recount drift described in
`reverse_engineering_region_0x80010000.md`'s scope section: this region's
*old* estimate (268) was never verified against a direct
`FunctionManager` enumeration until pass 1; the other 6 untouched
regions' counts are still old, unverified estimates and may carry similar
small drift once their own pass-1 authoritative recount happens. Treat
2048 (this table) as more trustworthy than 2053 (the summary metric) until
a future pass reconciles them properly via a fresh `RomCoverageStats.java`
run, per the periodic-recompute instruction in `work-in-progress.txt`.)

Distribution is fairly even across the whole ROM outside the
`0x80000000`-`0x8000ffff` region (9.4–17.2% per 64 KiB region), which has
now dropped to 4.3% of the unnamed total after Phase 9's region sweep
**fully completed** there (9 passes, 0 gap functions left unresolved); the
other 7 regions are unchanged from the 2026-06-21 baseline and still
interleave named/unnamed functions
throughout, consistent with how Phases 1–8 worked (tracing call chains and
protocol handlers rather than sweeping linearly through address space).

### Size statistics

**Stale baseline (2026-06-21), not re-run this pass** — these aggregate
byte-level stats (count/total-bytes/average) require a fresh
`RomCoverageStats.java`-style pass over the now-shrunk unnamed set to be
accurate; the per-region distribution table above (manually recomputed each
pass from the named-table deltas) is the authoritative up-to-date count
(2059, not 2278) until that re-run happens.

| Metric | Value |
|--------|-------|
| Count | 2278 (stale; see note above — true current count is 2059) |
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
