# ROM Region 0x80000000–0x8000ffff — Catch-All Triage

Part of Phase 9's exhaustive ROM-function sweep (see `work-in-progress.txt`,
"PHASE 9 CONTINUED — EXHAUSTIVE RE OF EVERY ROM FUNCTION"). This doc covers
functions in `0x80000000`–`0x8000ffff` that don't belong to a subsystem with
its own dedicated doc. Functions in this address range already covered
elsewhere are **not** repeated here:

- `0x80000000`–`~0x80000200` (32-bit MIPS boot preamble) — see
  `reverse_engineering_boot_reset_sequence.md`
- `0x80009000`–`~0x80009260` (interrupt/exception trampoline, MIPS16e-switch
  region) — see `reverse_engineering_interrupt_vectors.md`
- `0x80009d00`–`0x8000a474` (USB ring-buffer transport driver,
  `early_fptr1/5/6/7`) — see `reverse_engineering_usb_transport_hci_driver.md`

This doc covers the two remaining gaps: roughly `0x80000200`–`0x80009000`
and `0x8000a474`–`0x8000ffff`. Per the region's scoping in
`rom_function_index.md`, that's 307 unnamed (`FUN_8000xxxx`) + 27
thin-named functions, 334 total. **Pass 1 (2026-06-22 morning) resolved 12**
of those to a real Ghidra name + decompiled purpose, **pass 2
(2026-06-22, same day) resolved 19 more** (18 real functions + 1
4-byte degenerate stub) **plus reclassified one entry as a non-function**
(`0x8000046c`, see below), and **pass 3 (2026-06-22, this continuation)
resolved 74 more** — **105 resolved total** (104 real functions + the 1
non-function reclassification), with several clusters now fully decompiled
end-to-end. The region is NOT fully resolved — **129 of the original 220
gap functions remain** (113 still completely unnamed `FUN_8000xxxx`, 16
pre-existing thin-named-but-undecompiled, minus the 2 of those 16 —
`optimized_memcpy`/`0x8000e85c` and `lots_of_initialization`/`0x8000fb5c` —
that the index already rates "high confidence" via other docs, leaving 15
genuinely-thin) — see the shrunk continuation range filed in
`work-in-progress.txt`.

## Method

Listed every function in the two gaps via a one-off `ListRegion0x80000_Gaps.java`
script (220 functions total across both gaps — slightly fewer than the
documented 307+27=334 because some of that count falls inside the
already-covered boot/interrupt/USB sub-ranges this doc excludes). Searched for
defined strings in the `rom` block with `Region0x80000StringXrefs.java` —
found only 11 referenced strings total in the whole `rom` block, none inside
either gap (`tHCI_TD`/`tHCI_CMD`/etc. all referenced from `0x80021ab0`,
`tLogger`/`tISR_EXTENDED`/`tTimer` from `0x80074ee0`/`0x80075428`, both
outside this region) — confirms this part of ROM is structural/numeric glue
code, not string-driven, so string-xref correlation (effective in other Phase
9 regions) doesn't apply here. Resolved functions instead via direct
decompile + call-graph + literal-constant evidence (packet-type words
`0xc00`/`0x1c00`/`0xc000` matching the already-documented SCO/eSCO
type-dispatch constants; CP0 register names `Cause`/`EPC` recognized
directly by Ghidra's MIPS processor module; struct field names already
established in `conn_record_subsystem.md`/`lc_lmp_state_machine.md`).

Batch decompilation was done with a new generic script,
`BatchDecompileList.java` (comma-separated hex address list in
`script_args[0]`, decompiles each + lists direct callees) — written this turn
to avoid one-address-per-tool-call overhead. Renames were done with
`RenameBatch1.java` (semicolon-separated `0xADDR=name` pairs).

**Rename-persistence verification (per CLAUDE.md/ticket request):** renamed
`FUN_80009130`→`get_CP0_Cause_register` and `FUN_8000913c`→`get_CP0_EPC_register`,
then ran a *separate* `run_ghidra_headless` call (different script,
`BatchDecompileList.java`) against the same GZF with `use_saved_project=True`.
The new names appeared correctly in the fresh invocation's decompile output.
**Confirmed: renames persist across separate headless calls against the same
cached GZF project**, as the ticket's mandatory-verification step asked.
This should hold for all future region tickets using the same `use_saved_project=True`
pattern against this GZF.

**Pass 2 (this continuation) method:** started from this doc's own "decompiled
but not yet confidently named" section and "open questions" list rather than
cold-triaging new addresses, per the ticket's instructions. Batch-decompiled
the named clusters' direct callees (`0x80060708`, `0x80042db8`/`0x80042de8`,
`0x800147b0`, `0x80035068`, `0x80042a68`, `0x8002addc`, `0x8002bc88`,
`0x8003d018`, `0x800093d0`/`0x800093e4`, `0x80013dc4`, `0x80014c58`) via
`BatchDecompileList.java`, which supplied enough cross-function evidence
(shared literal constants, shared struct-field offsets, a consistent
"index→codec/role table row" calling convention) to commit real names for
the `0x8001483c`/`0x80014d50` hook-bit pair and the `0x80000fb8` SCO/eSCO
credit scheduler that the pass-1 doc had flagged as "needs one more pass."
Settled **open question 1** (the `0x8000046c` alias) with a single
`DiagAddr.java` raw-byte dump: `0x8000046c`'s instruction bytes are `00 00
00 00 ...` (all zero) and its sole "instruction" is `addiu s0,sp,0x0` — this
is **not a real function**, it is a zero-filled alignment/padding gap that
Ghidra's auto-analyzer mis-split into a 20-byte stub immediately before
`usb_event_status_handler` (`0x80000480`); the earlier "identical decompile
output" observation was the decompiler falling through the zero bytes
straight into the real function's body. Reclassified accordingly (see
table below) — `usb_event_status_handler_dup` is **removed as a function
name** in favor of a "not a real function" note, correcting pass 1's
tentative naming. Also re-ran `ListRegion0x80000_Gaps.java` to get the
exact current list of all 220 gap functions (unchanged set, used to pick
fresh untriaged addresses `0x8000b820`/`0x8000b858`/`0x8000b864` and to
confirm `0x8000f624` really is a standalone 4-byte function, not a fragment:
its sole instruction is `jr ra`, a degenerate "return immediately"
thunk/stub).

**Pass 3 (this continuation) method:** per the ticket's instruction, attacked
the explicitly-flagged "untouched gap B" stretch (`~0x8000b068` through
`0x8000ffff`) with `BatchDecompileList.java` in ~8-13-address batches, working
roughly address-ascending through the VSC_0xfc39/0xfc6c cluster
(`~0x8000bd00`–`0x8000bf00`), the RF/baseband-register bitfield-accessor
cluster (`0x8000c7cc`–`0x8000cc88`), the link-state/AFH/packet-type cluster
(`0x8000cec4`–`0x8000d8b0`), and the eSCO quality/retransmission-recovery
cluster (`0x8000dcd4`–`0x8000e470`); also closed out the pass-2-flagged
encryption-teardown/role-confirmation triplet (`0x80002974`/`0x80002a8c`/
`0x80002b60`) and the `0x8000a4ac` pool-init cluster. Evidence per cluster:
shared call targets to already-named leaf primitives (`disable_interrupts_*`/
`enable_interrupts_*`, `FUN_80011510`/`FUN_80011608` baseband-register R/W,
`sco_esco_timing_ratio_calculator` itself resolved mid-pass and then reused
as evidence for its callers), a near-identical `(*pbVar & 0x1e) == 6` /
`(*pbVar2 & 1) != 0` link-active-state gating idiom repeated verbatim across
~10 functions (named `link_state6_*`/`conn_state2_*` accordingly), and single-bitfield
read/set/clear/toggle bodies with no branching beyond the boolean parameter
(named generically as `set_bitN_of_global`/`toggle_bitN_of_global`/etc. since
the specific hardware register they front could not be pinned to a named
peripheral this pass). Confirmed rename-persistence again via the post-batch
`ListRegion0x80000_Gaps.java` re-run showing all 74 new names correctly
reflected, and via later `BatchDecompileList.java` calls in the *same* pass
showing earlier-renamed callees (e.g. `sco_esco_timing_ratio_calculator`,
`baseband_feature_pool_init_and_reset`, `role_dependent_bit_toggle_pair`)
resolved by name in later batches' CALLEES lines — no regression.
**Result: 74 functions resolved this pass** (vs. 12 in pass 1 and 19 in pass
2 — the largest single-pass yield so far, attributable to gap B's higher
density of small, structurally-similar register-accessor functions that
batch-decompile and pattern-match quickly once 2-3 exemplars are understood).

## Resolved functions — pass 1 (12)

| Address | Old name | New name | Evidence / purpose |
|---------|----------|----------|---------------------|
| `0x80009130` | `FUN_80009130` | `get_CP0_Cause_register` | 12-byte function, body is exactly `return Cause;` — Ghidra's MIPS processor module recognizes the CP0 special register by name. Called from `isr_bottom_half_status_dispatcher` to read interrupt-cause bits at the top of the bottom-half handler. |
| `0x8000913c` | `FUN_8000913c` | `get_CP0_EPC_register` | 12-byte function, body is exactly `return EPC;` (exception program counter). Same caller as above; likely used for fault diagnostics/logging in the bottom-half path. |
| `0x80000d78` | `FUN_80000d78` | `isr_bottom_half_status_dispatcher` | 492B. Calls `get_CP0_Cause_register`/`get_CP0_EPC_register` first, builds a status word, then branches through ~8 status-bit tests (`0x7c`, `0x4000`, `0x400`, `0x1000`, `0x800`, `0x2000`, `0x20`) dispatching to `FUN_80013780` (likely UART/SCO), `FUN_80004fd8` (baseband), `usb_event_status_handler`, `connection_event_status_handler`, `uart_rx_tx_byte_fifo_handler`, and `FUN_800128e0`. Ends with an unrecoverable indirect jump (Ghidra: "Could not recover jumptable, too many branches" — likely a tail-call through a function-pointer table, not a true switch). This is the **ISR bottom-half / deferred-event dispatcher** invoked after the raw exception trampoline (`reverse_engineering_interrupt_vectors.md`) saves context — fills the gap that doc left open ("single-slot ISR dispatch" vs. what runs after). |
| `0x80000a5c` | `FUN_80000a5c` | `connection_event_status_handler` | 410B. Reads 4 status words from a fixed MMIO-like region (`DAT_80000bf8`/`bfc`/`c00`/`c04`), dispatches through an optional hook fptr, then through `unknown_referencing_default_name_1` (`0x800009c0`), `usb_event_status_handler_dup`, `0x800007d0`, calls `HCI_Disconnect_on_error` on a hardware-link-loss bit (`0x800000`), applies config-blob feature-mask bits (`field56_0x3e`, `field57_0x3f`, `field59_0x41`), then logs via `possible_logging_function__var_args` with all 4 status words as varargs. This is a **per-connection hardware status-change handler** (link loss / role bits / feature gating), called from the ISR bottom half. |
| `0x80000480` | `FUN_80000480` | `usb_event_status_handler` | 764B. Byte-identical structure to `0x8000046c` (see next row) — reads global status words, loops dispatching through up to 9 status bits (`0x1`,`0x2`,`0x4`,`0x8`,`0x10`,`0x20`,`0x40`,`0x80`,`0x100`,`0xc4000000`) to function-pointer-table slots at `PTR_DAT_800007a8+{0,4,8,...,0x24}`, then a separate "if disabled and idle" branch calling `FUN_8000e1b0`/`FUN_8000c988`/`send_evt_HCI_Hardware_Error`, then unconditionally calls `FUN_8000e4bc`/`FUN_8000dd00` (USB transport drain functions documented in `reverse_engineering_usb_transport_hci_driver.md`) when a mode byte is `6`. Named for the USB-transport-drain calls at the tail; the bit-dispatch loop itself looks like a generic "fire all set event bits through a vtable" pattern reused for several IRQ sources (see dup below). |
| `0x8000046c` | `FUN_8000046c` (was briefly `usb_event_status_handler_dup` in pass 1) | **NOT A REAL FUNCTION** — see pass-2 correction below | **Corrected in pass 2.** Raw bytes at `0x8000046c` are 20 zero bytes (`00 00 00 00 ...`); Ghidra's disassembler reads the all-zero bytes as `addiu s0,sp,0x0` and mis-split a spurious 20-byte "function" here. It is zero-filled alignment padding immediately before `usb_event_status_handler` (`0x80000480`) — pass 1's "identical decompile output" observation was the decompiler falling through the padding into the real function. No purpose to document; flagged in the index as a non-function/analyzer artifact, not renamed further. |
| `0x80000c24` | `FUN_80000c24` | `timer_callback_table_dispatcher_4entry` | 64B. Reads a status byte, loops over exactly 4 bits (`uVar7 < 4`), and for each set bit calls a function pointer from a 4-entry table at `PTR_DAT_80000c70 + (i*4)`. Classic small fixed-size callback-table dispatcher; called from `isr_bottom_half_status_dispatcher`'s `0x4000`-bit branch path twice (once directly, once via the `0x2000` path) suggesting it's the generic "fire timer-class callbacks" helper. |
| `0x80000c74` | `FUN_80000c74` | `uart_rx_tx_byte_fifo_handler` | 190B. `switch` on `status&0xf` with cases `0,4,0xc,6,7`; cases 4/0xc manage a circular byte buffer (`PTR_DAT_80000d60 + index`, wrapping at `0x10`/`0x100`), incrementing/wrapping an index byte and invoking a drain callback (`(**(code**)(buf+8))(...)`) when the buffer fills (index reaches `0x10`) — the single-byte-at-a-time vs. bulk-copy branching (based on a `0xc0`-mask test) is characteristic of a UART or similar byte-stream peripheral's RX/TX FIFO ISR handler, not a Bluetooth-protocol function. Renamed accordingly; the `tISR_EXTENDED`/`tTimer` strings referenced nearby (`0x8007ae78`/`0x8007ae88`, just outside this gap) support an interrupt/timer-adjacent peripheral driver classification. |
| `0x800012b8` | `FUN_800012b8` | `baseband_event_status_dispatcher_0xd` | 216B. `do/while` loop draining a status word bit-by-bit across **13** possible bits (`uVar8 < 0xd`), each clearing its bit in the global status word, and (for bits whose `0x200>>i` test passes) branching between `FUN_8002af24`/`FUN_8003e1d4` (their choice gated by a per-slot config byte `field+7 bit2`) — a per-source dispatch table over a 13-entry array at `PTR_DAT_80001394` (12 bytes/entry — matches a small connection/baseband-event record size). Falls through to a feature-gated call to `VSC_0xfca1_FUN_80077474` (already-named VSC handler) when a config bit (`field208_0xd8 & 0x4000`) is set. This looks like the **general baseband-controller event dispatcher** (interrupt-class events keyed by source ID 0-0xc), parallel in shape to `conn_type_dispatch_and_esco.md`'s `FUN_80050810` but for a different (smaller, 13-vs-4-entry) event-source table — not yet confirmed as the *same* table, flagged for follow-up. |
| `0x800011fc` | `FUN_800011fc` | `conn_record_pending_data_drain` | 188B. Walks a linked list of buffer nodes (`*(int*)(param_1+0x20)`, `node[3]` as `next`), subtracting/accumulating a requested byte count (`param_2`) against each node's stored length (`node+6` as `ushort`), unlinking fully-consumed nodes into a free list at `param_1+0x28/0x2c`, and (when `param_5` flag is set) optionally writing per-node length+pointer pairs into an output array before advancing — classic **linked-list byte-stream drain/dequeue primitive**. Matches the conn-record pool architecture in `reverse_engineering_conn_record_subsystem.md` (struct field layout: `+0x20` head, +0x28/0x2c free-list head/tail) — likely the actual dequeue primitive that pool uses for buffered TX/RX data, called from `FUN_80000fb8` (a packet/credit scheduler, decompiled but not yet named — see below). |
| `0x8000a780` | `FUN_8000a780` | `find_pool_index_by_addr_and_mark_dirty` | 318B (1 unreachable-block warning from Ghidra, benign — dead branches after a `goto`-heavy decompile). Given an address + a small "which-pool" selector (`param_2` 1/2/3/5), looks up which of 4 fixed-size record pools (entry sizes `0x418`, `0x104`, `0x104`, `0x108` — bytes, matching different connection-record struct sizes already seen in the conn-record-subsystem doc) the address falls within, computes the record's index via integer division by the entry size, disables interrupts (calls the already-documented `disable_interrupts_(clear_LSBit_of_CP0_Status_Register)`), sets a dirty/pending bit at a per-pool-class offset (`0x30`/`0x38`/`0x34`/`0x3c`) and bit position equal to the computed index, then re-enables interrupts. Generic "find-owning-record-and-flag-dirty" primitive shared across multiple pool types — extends `reverse_engineering_conn_record_subsystem.md`'s allocation/lookup story with the dirty-flagging side. |
| `0x8000a8e8` | `FUN_8000a8e8` | `conn_record_periodic_sweep_and_clear_dirty` | 308B. Reads a 32-bit status word, extracts a 3-bit class index (`>>0x1d`), tracks the previous class in a 1-byte history slot, and on class *change* resets three "in-use" flags, ANDs a config mask into a counter, then sweeps a 17-entry array (`uVar10 < 0x11`) clearing bit 0x80 in each entry's flags byte (`field+3`) — i.e., a **periodic dirty/in-use-flag-clearing sweep** over the same kind of fixed-size record array the previous function flags as dirty. Conditionally calls `FUN_8000a2ac` when a "done" flag is clear. Calling pair with `find_pool_index_by_addr_and_mark_dirty` (mark-dirty vs. clear-dirty-on-sweep) strongly suggests these two are the write-side and reclaim-side of the same record-pool dirty-tracking mechanism documented qualitatively (but not by these two function names) in `reverse_engineering_conn_record_subsystem.md`. |

## Resolved functions — pass 2, this continuation (19, + 1 non-function reclassification)

| Address | Old name | New name | Evidence / purpose |
|---------|----------|----------|---------------------|
| `0x8001483c` | `FUN_8001483c` | `set_bos_e4_role_switch_hook_bit` | 104B. Looks up a role/type byte via `lookup_codec_or_role_type_table_7x4`, calls `remap_role_index_to_esco_slot_if_pending`, calls `lmp_25c_procedure_completion_waiter`, disables interrupts, then ORs `1 << index` into the 16-bit value at the **`0xe4` register offset** — this is `bos_base+0xe4`, the hw-write hook slot from CLAUDE.md's key-address table (`0x801212e4` at runtime). This is the **set** half of a per-connection-index bitmask stored at that hook slot. Paired with `clear_bos_e4_role_switch_hook_bit` below (same offset, `~(1<<index) AND`). |
| `0x80014d50` | `FUN_80014d50` | `clear_bos_e4_role_switch_hook_bit` | 84B. Same shape as the set function above but clears the bit (`~(1 << index) & current`) at the same `0xe4` register offset. Set/clear pair confirms `bos_base+0xe4` carries a **per-connection-index bitmask**, not a single hook-fn-pointer slot as previously assumed elsewhere — refines (does not contradict) the existing `+0xe4` struct-offset note in CLAUDE.md, which describes the slot's *use* as a hardware-write hook dispatch target; this pair shows *one* of the things written there is an index bitmask, likely gating which connections currently have a role-switch/eSCO hook active. |
| `0x80060708` | `FUN_80060708` | `lookup_codec_or_role_type_table_7x4` | 50B. Given a 1-based role index (1-7) and a sub-index (0-3), looks up a byte from a 7×4 byte-pair table (`PTR_DAT_8006073c`); returns the byte via out-param if it's `<10` (a small enum, e.g. SCO/eSCO codec or LMP role-switch sub-state) else returns 0xff. Generic small-table lookup helper used throughout the eSCO/role-switch cluster — called by `0x8001483c`/`0x80014d50`/`0x80001c4c`/`0x800022e4`. |
| `0x80042db8` | `FUN_80042db8` | `remap_role_index_to_esco_slot_if_pending` | 42B. If a per-index flags byte (`PTR_DAT_80042de4[idx+4]`) has bit1 set and bit0 clear ("eSCO pending, not yet active"), remaps the role/connection index to `idx+8` (the eSCO-handle range, offset by 8 from the SCO range); otherwise passes the index through unchanged. Confirms the chip uses a flat index space where SCO connections occupy 0-3 and their eSCO counterparts occupy 8-11 (matching the credit-scheduler and codec-table addressing seen elsewhere in this cluster). |
| `0x80042de8` | `FUN_80042de8` | `remap_role_index_to_esco_slot_unconditional` | 36B. Same remap (`idx+8`) but gated only on bit0 being clear (no "pending" check) — the unconditional/already-committed variant of the function above. |
| `0x800147b0` | `FUN_800147b0` | `write_baseband_codec_param_triplet` | 140B. Computes a base table-row index from a role/type lookup (`FUN_80042a68`, see below) scaled by 7, then writes 3 consecutive codec-table rows via `write_codec_table_entry_and_wait_ack`, packing bytes from the caller's 10-byte parameter block into each write's 32-bit value. This is the **codec-parameter programming routine** — writes a 3-row (96-bit) codec configuration block for a given connection/role into the chip's "table 0x238/0x23a/0x236"-style indexed register interface (see `write_codec_table_entry_and_wait_ack`). |
| `0x80035068` | `LMP__25C_called2` | `lmp_25c_procedure_completion_waiter` | 138B. Two-state synchronous waiter keyed on a global state int: state `1` immediately writes a masked baseband register value (offset `0x6c`) and clears the state to 0 (fast path, no actual wait needed); state `2` logs entry, calls `set_global_status_bit_0x400`, then spins a `2000`-iteration busy-wait loop (each iteration itself spinning `0x266` times) polling the same state variable for it to become 0 (set by an ISR/other context), logs the final value, then calls `clear_global_status_bit_0x400`. This is a **bounded busy-wait barrier for an in-flight LMP procedure** (name retained from Kovah's `LMP__25C` prefix, `_called2` suffix dropped in favor of a description of what it actually does) — the `0x400` status bit it sets/clears around the wait is most plausibly a "do not sleep / busy CPU" flag for the scheduler, consistent with a synchronous wait for hardware to finish a register write. |
| `0x80042a68` | `FUN_80042a68` | `conn_record_role_to_esco_slot_index` | 62B. Reads a connection record's `bos_connection__array_index` field and `byte_0xCC` field; if `byte_0xCC` is a pending-eSCO-flagged value `<4` per the same flags-byte test as `remap_role_index_to_esco_slot_if_pending`, returns `byte_0xCC+8` (eSCO slot range) instead of the raw array index. The "given a connection record, get its current credit-scheduler/codec-table slot index" accessor used by `write_baseband_codec_param_triplet` and the rest of this cluster. |
| `0x80000fb8` | `FUN_80000fb8` | `sco_esco_packet_credit_scheduler` | 550B. Per CLAUDE.md's struct-offset doc and the conn-record-subsystem doc: walks the connection record looked up via `lookup_conn_record_by_lt_addr`, computes a byte-budget/credit value per iteration via `wrapping_subtract_masked_by_shift` (a generic masked-subtract-with-wraparound helper, also renamed this pass), drains pending TX data through `conn_record_pending_data_drain` (already named in pass 1) when credits allow, and on success commits a packet-length table into a credit-scheduler slot obtained from `alloc_credit_scheduler_slot_0xd` (a 13-slot, i.e. `0xd`, allocator — same "13" cardinality as `baseband_event_status_dispatcher_0xd`, supporting but not proving open-question-2's hypothesis that they share a table). This is the **SCO/eSCO outbound packet-credit scheduler**: for each connection, decide how many bytes/packets can go out this baseband slot and build the hardware packet-length descriptor table accordingly. |
| `0x8002addc` | `FUN_8002addc` | `lookup_conn_record_by_lt_addr` | 52B. Given a 16-bit LT_ADDR-like parameter, checks it against 3 fixed connection-record slots' stored addresses (`+0x18`, `+0x4c`, `+0x80` within a small fixed-base struct) and returns a pointer to the matching slot's base (`base + index*0x34`), or NULL if none match. A small fixed-capacity (3-entry) connection lookup table, distinct from the larger `big_ol_struct` connection-record pool documented elsewhere — likely scoped to SCO/eSCO-capable connections only (small max count is consistent with the chip's eSCO connection limit). |
| `0x8002bc88` | `FUN_8002bc88` | `alloc_credit_scheduler_slot_0xd` | 116B. Disables interrupts, and if the requested slot index is `<0xd` (13) and its bit is still set in a 13-bit free-mask (`*(ushort*)(base+0x9c)`), clears that bit (marks allocated), sets a per-slot "in use" flag (`base+slot*0xc+5 |= 1`), re-enables interrupts, and returns the slot index — or returns `0xd` (sentinel "none free") and logs a warning if the requested slot was already taken or out of range. Classic **fixed-size (13-slot) bitmask allocator** for the credit-scheduler table that `sco_esco_packet_credit_scheduler` commits packet descriptors into. |
| `0x8003d018` | `FUN_8003d018` | `wrapping_subtract_masked_by_shift` | 26B. `(a - b) [+ wraparound constant if a<b], masked by a second constant, both shifted right by the 3rd parameter`. A generic modular/wrapping subtraction primitive (e.g. for circular time/credit counters) — used by the credit scheduler to compute elapsed-time-since-last-service per connection. |
| `0x800093d0` | `FUN_800093d0` | `set_global_status_bit_0x400` | 14B. `*global |= 0x400`. Trivial bit-set helper, called from `lmp_25c_procedure_completion_waiter` around its busy-wait loop. |
| `0x800093e4` | `FUN_800093e4` | `clear_global_status_bit_0x400` | 16B. `*global &= ~0x400`. Pairs with the set function above. |
| `0x80013dc4` | `FUN_80013dc4` | `write_codec_table_entry_and_wait_ack` | 96B. Writes a 32-bit value as two 16-bit halves to fixed indexed registers `0x238`/`0x23a`, then writes the row index (masked to 9 bits, OR'd with `0x200` "go" bit) to register `0x236`, then busy-polls a status register until bit `0x200` clears (hardware ack). This is the **indexed codec/parameter table write primitive** — the low-level "write one table row and wait for hardware to consume it" routine that `write_baseband_codec_param_triplet` and `clear_codec_table_entries_for_role` call repeatedly. |
| `0x80014c58` | `FUN_80014c58` | `clear_codec_table_entries_for_role` | 152B. Given a role/connection index, computes its codec-table base row (via `conn_record_role_to_esco_slot_index`-style indexing) and zeroes 12 consecutive table rows via `write_codec_table_entry_and_wait_ack`, then zeroes the same 3-row block `write_baseband_codec_param_triplet` writes, logging the operation. The **teardown counterpart** of `write_baseband_codec_param_triplet` — clears a connection's codec configuration on disconnect/role-switch-out. |
| `0x8000b820` | `FUN_8000b820` | `program_rf_freq_reg_and_start_poll` | 56B. Writes a 10-bit value (likely an RF channel/frequency word) split across two fixed byte registers (low byte + masked high 2 bits), writes `0xf` ("start"/"go") to a third status byte, then immediately calls the poll-with-timeout function below. Shape matches an **RF synthesizer channel-set-and-lock-wait** primitive (program frequency word, kick off PLL lock, wait for lock indication) — fits the chip's baseband/RF register-init theme already documented for nearby functions (`program FUN_80109980` etc. in CLAUDE.md), though this is a ROM (not patch) implementation operating on different register addresses. |
| `0x8000b864` | `FUN_8000b864` | `poll_status_sign_bit_with_timeout_0x65` | 26B. Busy-polls a status byte's sign bit (`< 0`, i.e. bit 7) up to `0x65` (101) times; on success copies a captured byte to the output param and returns 1, on timeout writes `0xff` and returns 0. Classic **bounded hardware-ready poll**. |
| `0x8000b858` | `FUN_8000b858` | `poll_status_sign_bit_with_timeout_0x65_variant` | 12B. Same poll logic as the function above but structured as a `while`-then-`do` with the loop-counter check first — almost certainly the same source routine compiled/laid out slightly differently (possibly an artifact of how Ghidra split a shared tail), not a functionally distinct routine. Kept as a separate name (rather than merged) since Ghidra treats them as two distinct function objects with distinct entry points and at least one real caller difference was not verified either way this pass. |
| `0x8000f624` | `FUN_8000f624` | `trivial_jr_ra_stub` | 4B, **real function, not padding** (raw bytes `20 e8 00 65`, single instruction `jr ra`). A degenerate "return immediately" stub — likely a function-pointer-table slot that needs to exist at a fixed address for an indirect-call site even though it does nothing (a no-op hook target), or a thunk left over from inlining. Verified via `DiagAddr.java` raw-byte dump that this is genuine code (a real `jr ra` opcode), not a zero-filled gap like `0x8000046c` — contrast deliberately documented since both are "tiny default-named functions in the gap" but only one is a real artifact. |

**Reclassified as non-function:** `0x8000046c` (was tentatively
`usb_event_status_handler_dup` in pass 1) — see the corrected row in the
pass-1 table above. This is zero-filled padding, not a function; it does
not count toward either the "resolved" or "remaining" totals going forward
(the region's effective function count is 1 less than previously assumed).

## Resolved functions — pass 3, this continuation (74)

Grouped by cluster (address-ascending within each); see the Method section
above for the shared evidence pattern per cluster.

**Encryption-teardown / role-confirmation triplet (pass-2 carryover, 3):**

| Address | New name | Evidence / purpose |
|---------|----------|---------------------|
| `0x80002974` | `encryption_key_teardown_notifier` | 246B. Clears a connection's crypto "key valid" flag (`_x58_crypto_struct...[0x214]`-adjacent field, sets `[0x215]=0`), logs twice around `FUN_80037804`/`FUN_8003fcc8` calls, calls `possible_logger_called_if_no_patch3`. Encryption-key/connection-teardown notification path. |
| `0x80002a8c` | `conditional_debug_logger_0x2be` | 198B. Feature-bit-gated conditional check (several link-mode bits) that decides whether to log a `0x2be`-tagged debug event via `possible_logger_called_if_no_patch3`; no other side effects. Debug instrumentation wrapper, not a protocol-significant function. |
| `0x80002b60` | `role_switch_confirmation_matcher` | 318B. Reads a 16-byte parameter block's role-confirmation byte, compares/rewrites it against a per-slot stored value (table at `PTR_DAT_80002ca4`, 0x10-byte rows), calls `FUN_80042e10`/`FUN_800384ac`. Confirmed as a **role-switch confirmation/ack matcher** against a small per-connection table; exact LMP PDU still not pinned (see open questions). |

**Pool-init cluster (pass-2 carryover, 3) — pool identity now confirmed:**

| Address | New name | Evidence / purpose |
|---------|----------|---------------------|
| `0x8000a4ac` | `conn_pool_quick_reset_and_log` | 62B. Sets a global to `0x100`, and if a flag byte is set, calls `FUN_8000a310(5)`/`FUN_80009f00(2)` then logs. Lightweight pool-state reset/log helper, paired with the two below. |
| `0x8000a4f8` | `pool_row_register_and_optional_callback_copy` | 98B. Registers N output-buffer rows at a computed base (`puVar3 + index*8` or `*4`, branch on `param_3`) into `*param_4`, and when `param_3` is set, optionally invokes a per-row callback (`call_fptr_if_set_with_2_args_possibly_allocates_buf_at_arg2_`) to populate each row. Generic "carve N fixed-size rows out of a pool and optionally fill them" primitive. |
| `0x8000a570` | `conn_record_7pool_init_and_clear_dirty` | 454B. Calls `pool_row_register_and_optional_callback_copy` **7 times** with counts `6, 0xc(12), 4, 8, 0x11(17), 0x11(17), 9` against 4 distinct pool-base pointers, then zero-initializes and `\|=0x40`-flags the last 3 (17/17/9-entry) pools. **Corrects pass 2's "3 distinct 17/17/9-entry pools" claim**: there are 7 pool-register calls total (not 3), of which only the last 3 are the zero-init+flag-marked ones pass 2 observed; the first 4 (6/12/4/8-entry) are registered but not zero-initialized in this function. Still not individually cross-referenced to specific structs in `conn_record_subsystem.md` — flagged as a follow-up, not blocking. |

**VSC_0xfc39/0xfc6c + RF-calibration cluster (~0x8000b068–0x8000c350, 23):**

| Address | New name | Evidence / purpose |
|---------|----------|---------------------|
| `0x8000b068` | `usb_or_conn_status_dispatch_loop_variant1` | 180B. Drains a status word bit-by-bit (5-bit then 8-bit mask), looks up a per-bit record via a fixed-size table, conditionally logs + calls `HCI_Hardware_Error` on a config-gated path, then dispatches via one of two function-pointer slots depending on a flag byte. Structurally a sibling of pass-1's `usb_event_status_handler`/`connection_event_status_handler` bit-drain-and-dispatch pattern, operating on a different status word/table. |
| `0x8000b138` | `usb_or_conn_status_dispatch_loop_variant2` | 192B. Near-identical to the above (5-bit drain, same logging/HW-error gate) but additionally stores a computed pointer into the dispatched record (`*(iVar8+0x40c) = table_row`) before the indirect call — a binding/registration step the variant1 sibling lacks. |
| `0x8000b218` | `feature_bit_status_reconfig_handler` | 266B. Logs a status-word change, conditionally resets it via a baseband-register read (`FUN_80011468`), then re-logs and reconfigures a different global based on 3 escalating feature-config bits (`field224_0xeb >> 6` role values 1/2/3), with a special case for a `0x80`-valued short field. Feature/role-driven HW status-register reconfiguration. |
| `0x8000b348` | `baseband_feature_pool_init_and_reset` | 1026B. Large init/reset routine, branches on a mode byte (0/1/3); mode 3 performs the same 17/17/9-row pool-zero-and-flag pattern as `conn_record_7pool_init_and_clear_dirty` (confirming the two functions share the same pool family), then unconditionally re-registers 6 pool rows (counts include `0x18`-sized rows), programs several feature-config-driven register words from `config_struct` fields (`field224_0xeb` role bits, `_x7a_enable_LMP_POWER_REQ_RES_and_CLK_ADJ`), and ends by setting a "ready" flag (`\|= 0x40000`). The **top-level baseband feature/pool (re)initialization entry point** — likely called once at connection-state-machine reset and again on full re-init (mode 3). |
| `0x8000b890` | `config_table_byte_stream_loader` | 570B. Loads a config-table byte stream via the same `0xf`-"go"-bit + sign-bit-poll pattern as `program_rf_freq_reg_and_start_poll`, writing 16-bit values into `pcVar1->configed_bdaddr + offset*2 - 0x30` — i.e. populates a config/BD_ADDR-adjacent table from a byte-serial hardware source (likely an OTP/EEPROM-style config read path feeding `copies_config_bdaddr`'s data). |
| `0x8000bae0` | `rf_calibration_kickoff_and_poll` | 98B. Sets 4 fixed registers to calibration-start values (`0x40`, `0`, clear low 2 bits, `0xf` go-bit), polls up to 101 times for a sign-bit-set status, and on success or special status `-1`/`0x80` re-triggers via `rf_calibration_status_check_or_kickoff` (mutual pair). RF calibration sequencer. |
| `0x8000bb58` | `rf_calibration_status_check_or_kickoff` | 50B. Zeroes a 3-field record, reads baseband register `0x2` via `FUN_80011510`, and either marks the record "valid" (byte 3 = 1) or calls `rf_calibration_kickoff_and_poll` depending on a config-masked bit test — the decision half of the calibration pair above. |
| `0x8000bb94` | `baseband_reg_0x34_init_and_latch` | 66B. Writes baseband register `0xcf` to `0x69`, reads-modifies-writes register `0x34` (OR in a fixed bit pattern), and initializes a 3-byte status record from the read value's high bits. One-time register-0x34 init/latch routine. |
| `0x8000bbe0` | `baseband_reg_0x34_role_index_setter` | 82B. For role index `<3`, lazily calls `baseband_reg_0x34_init_and_latch` on first use, then conditionally rewrites register `0x34`'s role-index field (bits `8-9`) and updates the cached status record when the index changes. The **role-index write half** of the register-0x34 pair. |
| `0x8000bc38` | `indexed_register_rw_poll_primitive` | 188B. General-purpose "write index N's value, poll for hardware ack (≤100 iters), read back" primitive parameterized by a bit-width flag (7-bit vs 9-bit range) and read/write direction; on failure or out-of-range logs via `possible_logging_function__var_args`. The shared low-level indexed-register R/W routine underlying several of this cluster's higher-level functions (parallel in role to `write_codec_table_entry_and_wait_ack` but for a different register bank). |
| `0x8000bd34` | `indexed_register_write_1byte_wrapper` | 32B. Thin wrapper packing a single byte into a 12-byte local buffer and calling `indexed_register_rw_poll_primitive` in write mode. |
| `0x8000bd54` | `check_status_bit_0x11_of_global` | 14B. `return (*global >> 0x11) & 1;` — trivial single-bit status accessor. |
| `0x8000bd68` | `check_status_bit_0x2_of_global` | 12B. `return (*global >> 2) & 1;` — sibling single-bit accessor, different global/bit. |
| `0x8000bd78` | `link_active_or_config_flag_check` | 48B. If an optional function pointer is set, calls it; otherwise reads a global bit (`>>1 & 1`) and inverts it unless a config feature bit (`field57_0x3f & 8`) is set. Link-active-or-config-override status check, used by the indexed-register cluster to decide write-enable. |
| `0x8000bdc4` | `or_bits_into_global_flag_word` | 10B. `*global \|= param_1;` — trivial OR-bits-in helper. |
| `0x8000bdd4` | `scrambled_bdaddr_field_writer_pair1` | 72B. XOR-based field-merge into two adjacent globals using shifted/masked combinations of two parameters — a bit-scrambled write pattern consistent with obfuscated/packed BD_ADDR or key-material field storage (paired with the function below). |
| `0x8000be28` | `check_bit_in_shifted_global_field` | 16B. `return (*global >> 10) >> (param_1 & 0x1f) & 1;` — indexed single-bit check into a shifted field. |
| `0x8000be3c` | `scrambled_bdaddr_field_writer_pair2` | 62B. Same XOR-merge shape as `scrambled_bdaddr_field_writer_pair1` but writing the complementary bit position (shifted by 10 instead of 0x15/0xb) — the two together look like a split-field bitwise writer for a multi-word packed value. |
| `0x8000beac` | `poll_status_and_invoke_optional_fptr` | 72B. Reads a global status; if its low byte is nonzero, logs it, then either invokes an optional function pointer (passing the status) or — if the pointer is null or returns 0 — writes the status back to the global. Generic "consume pending status, optionally let an installed hook veto the consume" pattern. |
| `0x8000bf04` | `set_bit_in_8entry_bitmask_pair_v1` | 60B. IRQ-disabled: if index `<8`, sets bit `index` in one global if not already set, then unconditionally XOR-toggles the same bit in a second global (net effect: copies the bit). Re-enables IRQ. |
| `0x8000bf48` | `set_bit_in_8entry_bitmask_pair_v2` | 84B. Same IRQ-disabled 8-entry-bitmask shape but sets the bit (if clear) independently in *two* separate globals rather than copying between them. |
| `0x8000bfa4` | `toggle_bit_in_8entry_bitmask_pair` | 84B. Same shape again but the second global's bit is *cleared* (if set) rather than set — the clear-side sibling of `set_bit_in_8entry_bitmask_pair_v2`. |
| `0x8000c000` | `irq_safe_4state_flag_setter` | 142B. IRQ-disabled 3-state flag setter (set/clear/toggle selected by `param_2` 0/1/2) over a 4-valued sub-range (`(param_1&0xff)-4`, range check `<4`), with an out-of-range path that logs via `possible_logging_function__var_args` instead of writing. Generalizes the 8-entry-bitmask pattern above to a smaller, range-checked 4-entry one. |

**RF/baseband register bitfield-accessor cluster (~0x8000c7cc–0x8000cc88, 23):**

| Address | New name | Evidence / purpose |
|---------|----------|---------------------|
| `0x8000c7cc` | `sco_esco_timing_ratio_calculator` | 116B. Computes `(table_constant * 11) / (packet_field * weighted_popcount_sum)` with a `trap(7)` divide-by-zero guard, selecting one of two table constants based on a config bit. Used extensively by the packet-type/timing cluster below (`program_packet_type_and_timing_ratio`, `recompute_and_apply_esco_interval`, etc.) — an **SCO/eSCO timing-interval/ratio calculation primitive**. |
| `0x8000c848` | `set_sign_bit_from_bool_param` | 30B. `*global = (*global & mask) \| (-(param_1!=0) & 0x80000000)` — sets/clears bit 31 from a boolean. |
| `0x8000c870` | `set_bit15_from_bool_param` | 34B. Same shape, bit 15. |
| `0x8000c898` | `set_bits24_29_from_param` | 22B. `*global = (*global & mask) \| ((param_1 & 0x3f) << 0x18)` — writes a 6-bit field at bit-offset 24. |
| `0x8000c8b8` | `program_3field_indexed_register` | 92B. Writes 3 separate fields (an 8-bit value split across 2 registers, a 4-bit nibble shifted by 4, and an 11-bit field shifted by 16) via the `0xff`-go-bit-then-clear idiom seen elsewhere in this region — a multi-field indexed register program routine, likely RF-channel or codec-parameter related given the bit widths. |
| `0x8000c92c` | `check_inverted_bit4_of_global` | 14B. `return (*global>>4 ^ 1) & 1;` — inverted single-bit check. |
| `0x8000c940` | `toggle_bit5_of_global_v1` | 30B. Boolean-param-driven set/clear of bit 5 (`0x20`). |
| `0x8000c964` | `toggle_bit1_of_global_inverted` | 30B. Boolean-param-driven set/clear of bit 1 (`2`), with set/clear branches swapped relative to the "normal" idiom (param `0` sets, param nonzero clears) — an inverted-logic sibling. |
| `0x8000c988` | `toggle_bit2_of_global` | 30B. Boolean-param-driven set/clear of bit 2 (`4`). |
| `0x8000c9ac` | `toggle_bit7_of_global` | 32B. Boolean-param-driven set/clear of bit 7 (`0x80`). |
| `0x8000c9d0` | `set_bit7_from_lsb_param` | 22B. `*global = (*global & ~0x80) \| ((param_1&1)<<7)` — direct (non-branching) bit-7 set from the parameter's LSB. |
| `0x8000c9ec` | `conditional_field_extract_into_global` | 36B. Masks a global, and if a flag byte's bit 0 is set, ORs in a 10-bit field extracted (shifted right 4) from a status word — conditional field-merge helper. |
| `0x8000ca1c` | `scrambled_status_field_unpacker` | 164B. Unpacks several non-contiguous bitfields from a status byte/word pair (bit-reversal-like shuffling of bits 1,2,3,4,5 into a differently-ordered output) into 3 destination globals — a **status-register bit-descrambling routine**, likely translating a packed hardware status format into a more naturally-ordered software representation (parallels `scrambled_bdaddr_field_writer_pair1/2`'s scrambled-field theme but for reads). |
| `0x8000cad4` | `adaptive_threshold_register_programmer` | 238B. Computes an adaptive threshold/gain value from an input parameter via a table lookup + leading-zero-count-style bit scan, clamps it against 2 config-derived bounds, and programs an 11-bit two-field register (`bits 8-10` and `bits 11-13`). Used as the final step of `recompute_and_apply_esco_interval`'s quality-recovery path — an **RF gain/threshold auto-adjustment register programmer**. |
| `0x8000cbd8` | `role_dependent_bit_toggle_pair` | 74B. Sets bits 3 and 4 of one register from two parameter bits, then — unless a "locked" config bit is set — conditionally sets/clears bit 5 of a second register based on bit 2 of the first parameter. Used by `program_packet_type_and_timing_ratio`. |
| `0x8000cc34` | `set_bit1_from_lsb_param` | 28B. `*global = (*global & ~2) \| ((param_1&1)<<1)` — direct bit-1 set from parameter LSB. |
| `0x8000cc54` | `masked_or_into_global` | 18B. `*global = (*global & mask1) \| (mask2 & param_1)` — generic masked-OR merge. |
| `0x8000cc74` | `masked_global_read` | 10B. `return (*global & mask);` — generic masked read. |
| `0x8000cc88` | `conn_state2_role_dependent_hw_reconfig` | 238B. Gated on an optional callback returning 0, then on connection sub-state byte (`*pbVar5 & 2`); if state byte indicates role `2` ("central"/role value 2) and an "0x80" or "0x40" feature bit is set, marks a status byte dirty; clears two related status flags afterward if either of two trigger bits fires. Connection-role-dependent HW-status reconfiguration, parallel in shape to `feature_bit_status_reconfig_handler`. |
| `0x8000cd90` | `conn_state2_codec_or_role_field_programmer` | 188B. Similarly gated by an optional callback + a "param==0 or bit1 set" condition; programs a 4-bit field (bits 26-29) from a status byte, then — branching on the same role-2 test as the function above — either ORs in a fixed pointer value or, in the "else" (non-role-2) branch, ORs it in *twice* plus sets an additional status bit, and resets several output-record bytes when a "link not yet up" condition (`bit16==0`) is detected. Sibling of the function above, programming codec/role fields instead of generic status. |
| `0x8000ce78` | `load_u32_little_endian_from_bytes` | 26B. `b[0] \| b[1]<<8 \| b[2]<<16 \| b[3]<<24` — trivial little-endian 4-byte load helper. |
| `0x8000cec4` | `link_state6_clear_bit15_if_feature_set` | 84B. Gated on link sub-state `== 6` (the same `(*pbVar1 & 0x1e) == 6` idiom used throughout this region — read as "link is in active/connected sub-state"); if a feature-config bit is set, clears register bit 15. First of 3 siblings sharing the identical state-6 gate. |
| `0x8000cf28` | `link_state6_set_bit15_and_mark_done` | 90B. Same state-6 gate; sets bit 15 (rather than clearing) when the feature bit is set, then unconditionally sets a "done" bit (bit 1) in a status byte. |
| `0x8000cf94` | `link_state6_set_bit3_if_active` | 44B. Same state-6 gate, simplest sibling: if a status-active bit is set, ORs bit 3 into a register and returns success; otherwise returns the `0xff` "not applicable" sentinel used throughout this trio. |

**Link-state/AFH/packet-type cluster (~0x8000d01c–0x8000d8b0, 17):**

| Address | New name | Evidence / purpose |
|---------|----------|---------------------|
| `0x8000d01c` | `get_2bit_link_state_field` | 14B. `return (*global >> 0x16) & 3;` — 2-bit link-state field accessor, distinct from (but related to) the `0x1e`-masked state-6 idiom used elsewhere. |
| `0x8000d030` | `rf_divider_ratio_search_and_program` | 244B. Iterative search over a small range of candidate divider values (stepping by a power-of-2 stride from a table-derived start), computing `(candidate>>1 + target)/candidate` and tracking the candidate minimizing the rounding error against `target`, then programs the winning candidate into a 12-bit register field (with special-casing for divider ranges `<0x28` vs `<0xa8` vs `>=0xa8`). An **RF frequency-synthesizer N-divider search-and-program routine** — same family as `rf_divider_ratio_table_lookup_or_search` below but the from-scratch search variant. |
| `0x8000d138` | `rf_divider_ratio_table_lookup_or_search` | 222B. Scans a 12-entry table for the closest pre-computed divider match to a scaled target (multiplying by 1 or 3 depending on a feature bit), and either writes the matched table entry directly or falls back to calling `rf_divider_ratio_search_and_program` when no good match exists (a "locked" config bit gates which path). The **table-lookup-first, search-as-fallback** entry point for the divider-programming pair. |
| `0x8000d228` | `link_state6_afh_or_channel_feature_toggle1` | 142B. State-6-gated (same idiom); if a feature flag (bit 3 of a status byte) and a specific 3-byte parameter match (`0x10,0x01,stored_value`) are both true, clears a feature bit and calls `VSC_0xfc95_called2`/`LMP__268__most_common_for_VSCs2_checks_fptr_patch` with a computed delay. First of 3 closely-related state-6 AFH/channel-feature toggle siblings. |
| `0x8000d2d0` | `link_state6_afh_or_channel_feature_toggle2` | 108B. Same state-6 gate and `VSC_0xfc95_called2`/`LMP__268` call pattern as toggle1, but gated on a single register bit (`&0x40`) rather than a 3-byte parameter match, and ORs in a fixed `0x800000`/`0x200000`-style flag rather than clearing one. |
| `0x8000d3c0` | `link_state6_afh_or_channel_feature_toggle3` | 138B. Third sibling: gated on a different 2-bit field (`>>0x16 & 3 == 3`, i.e. `get_2bit_link_state_field`'s value) and a register bit, calls the same `LMP__25B`/`VSC_0xfc95_called2`/`LMP__268` triad as `0x8000d228`. |
| `0x8000d460` | `afh_feature_toggle_dispatcher` | 98B. Dispatches by a mode byte (0-3) to either an optional override callback, a state-6-gated direct register OR, or one of `link_state6_afh_or_channel_feature_toggle2`/`toggle3` — the **unifying dispatcher** for the 3-sibling toggle cluster above, confirming they are alternate code paths of one logical "AFH/channel feature control" operation rather than unrelated functions. |
| `0x8000d4d4` | `afh_feature_toggle_autotrigger` | 74B. Three-condition gate (state-6, a sub-bit, and a second sub-bit) that, when all true and a 2-bit mode field is nonzero, calls `afh_feature_toggle_dispatcher` with that mode — an automatic/conditional trigger wrapping the manual dispatcher. |
| `0x8000d5fc` | `program_packet_type_and_timing_ratio` | 292B. Clears/sets several status bits based on parameter flags, calls `role_dependent_bit_toggle_pair`, programs a 3-field indexed register (index/value/nibble, same shape as `program_3field_indexed_register`), calls `sco_esco_timing_ratio_calculator`, conditionally logs, and stores the packet-type parameter into a record field (`+0xc`). The **central packet-type-and-timing programming routine** this whole sub-cluster builds toward. |
| `0x8000d750` | `read_current_packet_type_word` | 56B. Either calls an optional override callback, or reads the stored packet-type word (record `+0xc`) and re-derives it via `program_packet_type_and_timing_ratio(...,1)` (the "read-only"/refresh mode) — the **accessor** counterpart of the programmer above. |
| `0x8000d790` | `packet_type_change_and_threshold_update` | 140B. State-6-gated; reads the current and a candidate packet-type word via `program_packet_type_and_timing_ratio`, and on success optionally updates a masked-OR global and — if a "locked" feature bit and quality bit are both set — recomputes the timing ratio and feeds it to `adaptive_threshold_register_programmer`. Ties the packet-type and RF-threshold sub-clusters together. |
| `0x8000d83c` | `vsc_0xfc17_packet_type_change_handler` | 98B. Checks the HCI command opcode field for `0xfc17` and a sub-opcode byte `0xe`; on match, clears a feature bit, sets a 2-field status record, and calls `packet_type_change_and_threshold_update`. **Confirmed VSC handler for opcode `0xfc17`** — a vendor-specific "change packet type" command. |
| `0x8000d8b0` | `program_default_packet_type_and_status` | 62B. Calls `program_packet_type_and_timing_ratio` with a fixed default packet-type constant (`DAT_00006004`), then sets a status byte to `0x80` and writes two fixed values (`0x60`, then `0x68`) to a status register — a default/reset-state packet-type programming helper. |

**eSCO quality/retransmission-recovery cluster (~0x8000dcd4–0x8000e470, 8):**

| Address | New name | Evidence / purpose |
|---------|----------|---------------------|
| `0x8000dcd4` | `trigger_callback_on_high_retry_count` | 40B. If a 3-bit retry-count field (bits 5-7 of a status byte) is nonzero, invokes a registered callback with `1 << (retry_count-1)` — escalating-backoff-style callback trigger. |
| `0x8000ded8` | `link_state6_quality_recovery_poll_loop` | 158B. State-6-gated; clears 2 status bits, sets a third, then polls (≤10 iterations) a status register's bit 8, and on success (and a config-gated condition) calls `FUN_8000c77c` (an existing-but-unnamed RF register accessor) — an eSCO **quality-recovery polling loop**. |
| `0x8000df94` | `check_link_state3_and_set_recovery_flags` | 90B. IRQ-disabled; if the 2-bit link-state field (`get_2bit_link_state_field`'s value) equals 3, sets 2 recovery-related status bits (and a 3rd if a feature bit is set), returning success; otherwise returns the `0xff` sentinel. Gate-and-flag-set primitive used by the interval-recompute function below. |
| `0x8000e004` | `recompute_and_apply_esco_interval` | 122B. Calls `check_link_state3_and_set_recovery_flags`; if it succeeds and a "0x40" feature bit is set, computes a new eSCO interval as `13000 / sco_esco_timing_ratio_calculator(...)` (with a `trap(7)` divide-guard) and applies it via up to 12 retries of `FUN_80009680`, bailing early if a quality bit clears. The **eSCO retransmission-interval recompute-and-apply** routine flagged as a hypothesis in pass 2's "quartet" notes — now confirmed and fully decompiled. |
| `0x8000e088` | `link_state6_quality_bit7_recovery_trigger` | 68B. State-6-gated (plus 2 more sub-bit gates, the second checking bit 7 of a 2nd status byte); on all 3 conditions, calls `recompute_and_apply_esco_interval`. The **trigger/entry-point** wrapping the interval-recompute function in the full state+quality gate chain. |
| `0x8000e0dc` | `program_rf_freq_word_from_status_or_table` | 178B. IRQ-disabled; branches on a "use table" flag — either programs a 13-bit frequency-word field directly from a status register, or looks it up from a 2-byte table entry — and updates a derived single-bit flag + (if a "locked" feature bit is set) a mirrored status byte either way. RF frequency-word programming with table-or-live-status source selection. |
| `0x8000e380` | `vsc_0xfc56_set_3word_params_and_packet_type` | 222B. Checks HCI command opcode `0xfc56`; on match, unpacks 3 little-endian 32-bit words from the command parameters into 3 globals, conditionally reads the current packet type (`FUN_8000c688`), conditionally re-inits the baseband feature pool (`baseband_feature_pool_init_and_reset(2)`), calls `FUN_8000e1b0` with the unpacked words, and conditionally restores the packet type via `program_packet_type_and_timing_ratio`. **Confirmed VSC handler for opcode `0xfc56`** — a vendor-specific "set 3 parameter words" command, likely RF/AFH-table-related given the pool-reinit call. |
| `0x8000e470` | `build_and_send_default_status_report` | 68B. Invokes an optional callback to obtain a buffer; on success, fills a fixed 6-byte status-report template (`0xf, 4, 0, role+1, 0, 0`) and sends it via `FUN_8002f220`. Generic default-status-report builder/sender. |

## Decompiled but not yet confidently named (context for next worker)

Carried over from pass 1 where still accurate, with pass-2 updates noted.
Several pass-1 entries were resolved this pass and removed from this list
(see the pass-2 table above); the remainder, plus newly-decompiled
functions from pass 2, are listed here:

- **`0x800007d0`** (68B), **`0x80000820`** (364B) — both take a `uint*`
  status-word pointer, OR feature-enable bits into it, and call
  `FUN_8000c5c8`/`FUN_80011468`/`FUN_80011bf4`; look like the same
  "enable-feature-and-report" pattern as the resolved handlers above
  (called from `connection_event_status_handler`/`isr_bottom_half_status_dispatcher`
  respectively) but their specific feature (`DAT_80000814`/`818`/`81c` —
  3 escalating capability bits) wasn't pinned down. Unchanged from pass 1.
- **`0x80001648`** (728B, pass-2 re-examined) and its near neighbors
  **`0x80001944`** (58B), **`0x80001990`** (292B), **`0x80001ad8`** (86B),
  **`0x80001b3c`** (238B) — all manipulate `big_ol_struct[i].field_0x219`/
  packet-type constants `0xc000`/`0x1c00`/`0xc00`, and now confirmed to call
  the newly-named `set_bos_e4_role_switch_hook_bit`/
  `clear_bos_e4_role_switch_hook_bit` (formerly `FUN_8001483c`/`FUN_80014d50`)
  plus `possible_logger_called_if_no_patch3`. Pass 2 fully decompiled
  `0x80001648`'s 4 callees down to leaf functions (now named — see table
  above) but `0x80001648` itself, plus `0x80001944`/`0x80001990`/
  `0x80001ad8`/`0x80001b3c`, still mix several distinct per-codec-type
  branches (1/2/4/8 in a switch-like byte test) whose individual HCI/LMP
  procedure identity (e.g. which one is "enter SCO," which is "enter eSCO,"
  which is "role-switch-initiated codec reconfigure") was not confidently
  separated out. Strong candidate cluster for the *next* pass — most of the
  hard work (callee decompilation) is now done.
- **`0x80001c4c`** (700B, pass-2 re-examined), **`0x80001f34`** (248B),
  **`0x80002048`** (334B), **`0x800021c0`** (254B), **`0x800022e4`** (384B)
  — all reference `big_ol_struct[i].field_0xb7`/`field_0x179`/
  `field200_0x206` connection states, call `set_bos[bosi].0xb2_index=arg2`
  and `send_evt_HCI_Truncated_Page_Complete` (both already-named), and
  program baseband registers with the same `0xc00`/`0x1c00` packet-type
  words. Pass 2 confirmed `0x80001c4c` calls the newly-named
  `lookup_codec_or_role_type_table_7x4`,
  `remap_role_index_to_esco_slot_if_pending`,
  `clear_codec_table_entries_for_role`, and
  `check_if_80122df0_is_non_zero_else_ret_0xff` (already named) — it is a
  **page-timeout/role-switch-failure-recovery handler**: branches on a
  `the_0x300` struct's `int_0x10` state field between "tear down as
  initiator" (state 2) and "tear down as responder/other" paths, both of
  which reprogram the connection's packet type back to a default and clean
  up the codec table. Still short of a confident specific-procedure name
  (candidate: "page/role-switch abort handler" but the exact HCI event this
  corresponds to wasn't nailed down), so left un-renamed; the other 4 in
  this cluster (`0x80001f34`/`0x80002048`/`0x800021c0`/`0x800022e4`) are
  siblings with individual variation not yet fully separated.
- **`0x800013a4`** (174B), **`0x80001470`** (146B), **`0x8000151c`** (64B),
  **`0x80001564`** (220B) — newly decompiled this pass. `0x800013a4` picks
  a packet-type word (`0xc00`/`0xc000`/`0x1c00`) based on `bdaddr_random_`
  and a "secure connection"-shaped config-blob bit
  (`field208_0xd8 & 0x20`), then programs it via the standard indexed-write
  callback — a **packet-type selection policy function**, sibling to the
  `0x80001648` cluster. `0x80001470` reprograms a connection's packet type
  to `0xc000` (max-rate SCO) when a watchdog/timer budget check
  (`ptVar2->uint_0x08 - now < 0xe`) passes, decrementing a retry counter —
  looks like an **eSCO retransmission-window/quality recovery** check.
  `0x8000151c` scans up to 10 `big_ol_struct` entries for the
  minimum `unknown4_0x3C` value among valid+active ones and stores it in a
  global "next deadline" field — a **scheduler tick/next-timeout
  finder**. `0x80001564` is a **feature/capability-bit-driven packet-type
  policy chooser** returning one of several fixed codes
  (`0x7f`/`0x80`/`0x82`/`0x83`/`0x84`) based on link-mode bits, calling
  `lookup_codec_or_role_type_table_7x4` and an unnamed `FUN_80063fc4`.
  All four are plausibly part of the same "decide what packet type/policy
  applies to this connection right now" theme as the rest of this gap but
  need one more pass each to commit specific names.
- **`0x80002974`** (246B), **`0x80002a8c`** (198B), **`0x80002b60`** (318B)
  — newly decompiled this pass. `0x80002974` clears a connection's crypto
  "key valid" flag (`_x58_crypto_struct...[0x214]`-adjacent field) and logs
  twice around a `FUN_80037804`/`FUN_8003fcc8` call pair — looks like an
  **encryption-key/connection teardown notifier**. `0x80002a8c` is a
  feature-bit-gated conditional logger (decides whether to log a
  "0x2be"-tagged event based on several link-mode bits) — likely a debug
  instrumentation wrapper, low value to name precisely. `0x80002b60` reads
  and conditionally rewrites a 16-byte parameter block's role-confirmation
  byte against a per-slot stored value, calling `FUN_80042e10`/
  `FUN_800384ac` — shape matches a **role-switch confirmation/ack matcher**
  but wasn't pinned down to a specific LMP PDU this pass.
- **`0x8000a4ac`**, **`0x8000a4f8`**, **`0x8000a570`** — **pass-2
  confirms** the pass-1 hypothesis: `0x8000a4f8` is confirmed as a
  "register N output-buffer rows + optional per-row callback copy" helper,
  and `0x8000a570` calls it exactly 6 times with counts `6, 0xc, 4, 8,
  0x11(=17), 0x11(=17), 9` against what are now clearly **3 distinct
  17/17/9-entry pools** (each subsequently zero-initialized and flagged
  `|=0x40` in a follow-up loop in the same function) — this is **one-time
  pool-initialization code**, consistent with (and likely literally part
  of) the same pool family `find_pool_index_by_addr_and_mark_dirty`/
  `conn_record_periodic_sweep_and_clear_dirty` (pass 1) operate on. Still
  not renamed because the *specific* pool identities (which of the 3 maps
  to which struct documented in `reverse_engineering_conn_record_subsystem.md`)
  weren't individually confirmed — straightforward next step.

## Open questions for the continuation ticket

1. ~~Disambiguate `0x8000046c` vs `0x80000480`~~ — **RESOLVED this pass**:
   `0x8000046c` is zero-filled padding, not a function. See the
   reclassification note above.
2. Confirm whether `baseband_event_status_dispatcher_0xd`'s 13-entry table
   and `conn_type_dispatch_and_esco.md`'s 4-entry `FUN_80050810` table are
   the same underlying table at different sizes, or genuinely separate
   tables. **Still open** — pass 2 found a second independent "13" (`0xd`)
   cardinality in `alloc_credit_scheduler_slot_0xd`'s free-mask, which is
   *consistent with* but does not *prove* a shared table; matters for
   cross-doc cross-referencing.
3. ~~Pin down the SCO/eSCO credit-scheduler reading at `0x80000fb8`~~ —
   **RESOLVED this pass**: named `sco_esco_packet_credit_scheduler`, see
   table above. Still TODO: fold a cross-reference into
   `reverse_engineering_conn_feature_dispatch.md` (not done this pass —
   left for whichever future ticket next touches that doc, to avoid
   scope creep on this region-sweep ticket).
4. The 5-function role-switch/truncated-page cluster (`0x80001c4c` family)
   and the 5-function packet-type cluster (`0x80001648` family) **remain
   the single highest-value targets** — pass 2 fully resolved their shared
   leaf-level callees (the `0x8001483c`/`0x80014d50`/`0x80060708`/
   `0x80042db8`/`0x80042de8`/`0x800147b0`/`0x80035068`/`0x80042a68` group),
   which should make the next pass over the 10 cluster-head functions
   themselves significantly cheaper than cold triage.
5. **New in pass 2, still open:** confirm whether `poll_status_sign_bit_with_timeout_0x65`
   (`0x8000b864`) and its near-duplicate `0x8000b858` are truly two call
   sites of conceptually "the same" poll routine (just compiled/laid out
   differently) or have a real behavioral difference — not verified either
   way; low priority (both are now named and documented, just not merged).
6. **New in pass 2, still open:** pin down the specific HCI/LMP procedure identity for
   the `0x80001c4c` cluster (candidate: page-timeout or role-switch-failure
   abort handler) and its 4 siblings — see the "Decompiled but not yet
   confidently named" entry above for what's already established. **Still
   the single highest-value target for the next pass** — pass 3 did not
   touch this cluster (focused on gap B per the ticket's instructions
   instead), so it remains exactly as pass 2 left it.
7. **New this pass:** several of pass 3's generic bitfield-accessor names
   (`set_bitN_of_global`, `toggle_bitN_of_global`, `masked_or_into_global`,
   etc.) were named by their *mechanical* behavior (which bit/field they
   touch) rather than by the *peripheral* they front, because the target
   global/register addresses (`DAT_8000xxxx`) don't carry symbolic names in
   the GZF and weren't cross-referenced to a specific named hardware block
   this pass. A future pass that resolves the underlying `DAT_*` addresses'
   identities (e.g. by checking what runtime/patch code also touches the
   same addresses) could upgrade many of these from "what it does" to "what
   it's for" without re-decompiling.
8. **New this pass:** the `link_state6_*` naming convention (10 functions
   this pass share the `(*pbVar & 0x1e) == 6` gate) assumes "state 6" means
   "link active/connected" by analogy with similar gates already documented
   elsewhere (e.g. `conn_record_subsystem.md`'s state-machine notes) — this
   specific numeric mapping (state value 6 ⇒ active) was not independently
   re-derived from first principles this pass, just reused as a working
   hypothesis consistent with the gated functions' behavior (RF/AFH/quality
   reconfiguration, which only makes sense on an active link). Flagged for
   anyone cross-referencing against `lc_lmp_state_machine.md`'s state-value
   table.

## Remaining scope

After pass 1 (12 resolved), pass 2 (19 more resolved + 1 non-function
reclassification), and **pass 3 (74 more resolved this continuation)**,
**105 of the original 220 gap functions are resolved** (104 real functions
+ 1 reclassified non-function), leaving **129 genuinely unresolved**:
113 still completely unnamed (`FUN_8000xxxx`, never triaged) and 16
pre-existing thin-named-but-undecompiled Kovah names that fall in this
region's gaps but weren't touched by any Phase-9 pass yet (of those 16, 2 —
`optimized_memcpy`/`0x8000e85c` and `lots_of_initialization`/`0x8000fb5c` —
are already rated "high confidence" in `rom_function_index.md` via other
docs and don't need further work here; the other 14 are genuinely open:
`func2_that_uses_structs_at_0x80100000`/`0x80003d10`,
`log_many_2_0x72_0x121-0x14e`/`0x80008d18`,
`VSC_0xfc6c_FUN_8000bd04`/`0x8000bd04`,
`VSC_common_used_in_0xfc39_FUN_8000bdb4`/`0x8000bdb4`,
`VSC_0xfc39_1_FUN_8000be84`/`0x8000be84`,
`unknown_referencing_default_name_3/4/5`/`0x8000c09c`/`0x8000c198`/`0x8000c390`,
`memset`/`0x8000e98c`, `references_patch_download_mem2`/`0x8000e9cc`,
`called_by_unknown_fptr_index9_1`/`0x8000f0a4`, `unknown_fptr_index9`/`0x8000f41c`,
`wraps_multi_VSC_called_if_no_patch3`/`0x8000f53c`,
`VSC_0xfc39_wrapper_FUN_8000fae8`/`0x8000fae8`, `copies_config_bdaddr`/`0x8000fd38`).
Recount verified directly: `ListRegion0x80000_Gaps.java` re-run at the end
of this pass shows all 220 original entries with pass 1+2+3's 105 renamed
names correctly reflected (confirms no rename-persistence regression and
no count drift since pass 2).

**Untouched/largely-untouched sub-ranges for the next pass** (the
`0x80001648`/`0x80001c4c` cluster heads remain the highest-value named
targets; everything else below is cold, never-triaged `FUN_8000xxxx`):

- `0x80001648`–`0x800022e4`: the two 5-function clusters pass 2 fully
  decompiled the leaf callees for but did not name the heads (packet-type
  cluster `0x80001648`/`0x80001944`/`0x80001990`/`0x80001ad8`/`0x80001b3c`;
  role-switch/truncated-page cluster `0x80001c4c`/`0x80001f34`/`0x80002048`/
  `0x800021c0`/`0x800022e4`), plus the packet-type-policy quartet
  `0x800013a4`/`0x80001470`/`0x8000151c`/`0x80001564` — **still untouched by
  pass 3**, carried forward unchanged from pass 2.
- `0x80002488`–`0x80008f04`: a large, entirely cold stretch (~35 functions,
  sizes ranging 16B–2044B) that no pass has touched yet — includes
  `func2_that_uses_structs_at_0x80100000` (2044B, thin-named) and a run of
  tiny (16-64B) functions around `0x80008a7c`–`0x80008cd8` that look like
  another bitfield-accessor cluster similar to pass 3's `0x8000c7cc`–`0x8000cc88`
  one (worth checking first for cheap pattern-matched wins).
- `0x8000aa64`: a single large (1398B) untriaged function between the
  pool-init cluster and gap B's start — not yet decompiled.
- `0x8000c09c`–`0x8000c77c`: the remaining thin-named-but-undecompiled
  Kovah names (`unknown_referencing_default_name_3/4/5`) plus a handful of
  cold `FUN_*` (`0x8000c350`, `0x8000c3f4`, `0x8000c5c8`, `0x8000c664`,
  `0x8000c688`, `0x8000c738`, `0x8000c77c`) that sit *between* pass 3's
  VSC_0xfc39 cluster and its register-bitfield cluster — pass 3 decompiled
  several of these as callees (e.g. `FUN_8000c688` is called by
  `vsc_0xfc56_set_3word_params_and_packet_type` and
  `packet_type_change_and_threshold_update`) but did not commit names for
  them since they weren't the direct target of this pass's batches — cheap
  win for the next pass since they're already decompiled in this doc's
  prose, just not tabulated/renamed.
- `0x8000cfcc`–`0x8000d01c`: 3 tiny untouched functions immediately before
  the link-state/AFH/packet-type cluster pass 3 did resolve.
- `0x8000d8f8`–`0x8000dcd4`: untouched stretch immediately before pass 3's
  eSCO quality-recovery cluster (`0x8000d8f8`, `0x8000dbdc`) — likely related
  given proximity, worth checking first.
- `0x8000e1b0`–`0x8000e380`: `0x8000e1b0` (430B) is called by pass 3's
  `vsc_0xfc56_set_3word_params_and_packet_type` but not itself decompiled —
  cheap next step.
- `0x8000e4bc`–`0x8000ffff`: the largest remaining untouched stretch
  (~40 functions) — includes the `unknown_fptr_index9` cluster
  (`0x8000f0a4`/`0x8000f41c`, thin-named, 806B+108B) and a run of small
  functions around `0x8000f4a0`–`0x8000fb20` that likely relate to the
  `VSC_0xfc39_wrapper_FUN_8000fae8` thin-named function nearby. Also
  contains the already-high-confidence `optimized_memcpy`/`memset`/
  `copies_config_bdaddr`/`lots_of_initialization` (no work needed).

See `work-in-progress.txt` for the shrunk continuation ticket's exact
address sub-range.
