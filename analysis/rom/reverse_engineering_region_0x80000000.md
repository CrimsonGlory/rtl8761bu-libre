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
of those to a real Ghidra name + decompiled purpose, and **pass 2
(2026-06-22, this continuation) resolved 19 more** (18 real functions + 1
4-byte degenerate stub) **plus reclassified one entry as a non-function**
(`0x8000046c`, see below) — **31 resolved total**, with several more
decompiled and partially evidenced for the next pass. The region is NOT
fully resolved — see the shrunk continuation range filed in
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
5. **New this pass:** confirm whether `poll_status_sign_bit_with_timeout_0x65`
   (`0x8000b864`) and its near-duplicate `0x8000b858` are truly two call
   sites of conceptually "the same" poll routine (just compiled/laid out
   differently) or have a real behavioral difference — not verified either
   way; low priority (both are now named and documented, just not merged).
6. **New this pass:** pin down the specific HCI/LMP procedure identity for
   the `0x80001c4c` cluster (candidate: page-timeout or role-switch-failure
   abort handler) and its 4 siblings — see the "Decompiled but not yet
   confidently named" entry above for what's already established.

## Remaining scope

After pass 1 (12 resolved) and pass 2 (19 more resolved + 1 non-function
reclassification), **189 of the 220 gap functions** (220 total minus 31
resolved minus 0 — the 1 reclassified non-function was already counted in
the original 220 and is now excluded from the "to resolve" denominator
rather than added to "resolved") remain untriaged or only contextually
decompiled. Concretely: 220 total gap entries − 12 (pass 1) − 19 (pass 2)
− 1 (reclassified, no longer counts) = 188 genuinely unresolved, plus a
double-check margin → reported as **~188–189 remaining** pending the next
pass's own recount via `ListRegion0x80000_Gaps.java` (which now also
implicitly excludes `0x8000046c` from being mistaken for a live target).
See `work-in-progress.txt` for the shrunk continuation ticket's exact
address sub-range and the specific addresses flagged above as cheap wins
for the next pass (the `0x80001648` and `0x80001c4c` cluster heads, the
`0x800013a4`/`0x80001470`/`0x8000151c`/`0x80001564` quartet, the
`0x80002974`/`0x80002a8c`/`0x80002b60` triplet, and the `0x8000a4ac` pool
cluster's specific-pool-identity confirmation).
