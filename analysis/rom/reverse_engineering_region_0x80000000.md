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
thin-named functions, 334 total. **This pass resolved 12 of those to a real
Ghidra name + decompiled purpose** (listed below) and decompiled ~25
additional functions that provided supporting context/evidence but were not
confidently nameable this turn (also listed, for the next worker's head
start). The region is NOT fully resolved — see the shrunk continuation range
filed in `work-in-progress.txt`.

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

## Resolved functions (12)

| Address | Old name | New name | Evidence / purpose |
|---------|----------|----------|---------------------|
| `0x80009130` | `FUN_80009130` | `get_CP0_Cause_register` | 12-byte function, body is exactly `return Cause;` — Ghidra's MIPS processor module recognizes the CP0 special register by name. Called from `isr_bottom_half_status_dispatcher` to read interrupt-cause bits at the top of the bottom-half handler. |
| `0x8000913c` | `FUN_8000913c` | `get_CP0_EPC_register` | 12-byte function, body is exactly `return EPC;` (exception program counter). Same caller as above; likely used for fault diagnostics/logging in the bottom-half path. |
| `0x80000d78` | `FUN_80000d78` | `isr_bottom_half_status_dispatcher` | 492B. Calls `get_CP0_Cause_register`/`get_CP0_EPC_register` first, builds a status word, then branches through ~8 status-bit tests (`0x7c`, `0x4000`, `0x400`, `0x1000`, `0x800`, `0x2000`, `0x20`) dispatching to `FUN_80013780` (likely UART/SCO), `FUN_80004fd8` (baseband), `usb_event_status_handler`, `connection_event_status_handler`, `uart_rx_tx_byte_fifo_handler`, and `FUN_800128e0`. Ends with an unrecoverable indirect jump (Ghidra: "Could not recover jumptable, too many branches" — likely a tail-call through a function-pointer table, not a true switch). This is the **ISR bottom-half / deferred-event dispatcher** invoked after the raw exception trampoline (`reverse_engineering_interrupt_vectors.md`) saves context — fills the gap that doc left open ("single-slot ISR dispatch" vs. what runs after). |
| `0x80000a5c` | `FUN_80000a5c` | `connection_event_status_handler` | 410B. Reads 4 status words from a fixed MMIO-like region (`DAT_80000bf8`/`bfc`/`c00`/`c04`), dispatches through an optional hook fptr, then through `unknown_referencing_default_name_1` (`0x800009c0`), `usb_event_status_handler_dup`, `0x800007d0`, calls `HCI_Disconnect_on_error` on a hardware-link-loss bit (`0x800000`), applies config-blob feature-mask bits (`field56_0x3e`, `field57_0x3f`, `field59_0x41`), then logs via `possible_logging_function__var_args` with all 4 status words as varargs. This is a **per-connection hardware status-change handler** (link loss / role bits / feature gating), called from the ISR bottom half. |
| `0x80000480` | `FUN_80000480` | `usb_event_status_handler` | 764B. Byte-identical structure to `0x8000046c` (see next row) — reads global status words, loops dispatching through up to 9 status bits (`0x1`,`0x2`,`0x4`,`0x8`,`0x10`,`0x20`,`0x40`,`0x80`,`0x100`,`0xc4000000`) to function-pointer-table slots at `PTR_DAT_800007a8+{0,4,8,...,0x24}`, then a separate "if disabled and idle" branch calling `FUN_8000e1b0`/`FUN_8000c988`/`send_evt_HCI_Hardware_Error`, then unconditionally calls `FUN_8000e4bc`/`FUN_8000dd00` (USB transport drain functions documented in `reverse_engineering_usb_transport_hci_driver.md`) when a mode byte is `6`. Named for the USB-transport-drain calls at the tail; the bit-dispatch loop itself looks like a generic "fire all set event bits through a vtable" pattern reused for several IRQ sources (see dup below). |
| `0x8000046c` | `FUN_8000046c` | `usb_event_status_handler_dup` | 20B *stub-shaped* — Ghidra's decompile of this address actually produces the *entire 764-byte body* of `0x80000480` (identical C output), meaning the two addresses currently alias the same function body for analysis purposes, OR `0x8000046c` is a 20-byte head fragment that Ghidra mis-bounded and the decompiler followed control flow straight into `0x80000480`'s body across the boundary. Not fully disambiguated this turn — flagged for a follow-up `DiagAddr.java` check on whether `0x8000046c`'s actual instruction bytes differ from `0x80000480`'s, or whether this is a genuine thunk/alias. Renamed with a `_dup` suffix rather than a confident distinct purpose to flag this for re-examination, not asserted as fully resolved. |
| `0x80000c24` | `FUN_80000c24` | `timer_callback_table_dispatcher_4entry` | 64B. Reads a status byte, loops over exactly 4 bits (`uVar7 < 4`), and for each set bit calls a function pointer from a 4-entry table at `PTR_DAT_80000c70 + (i*4)`. Classic small fixed-size callback-table dispatcher; called from `isr_bottom_half_status_dispatcher`'s `0x4000`-bit branch path twice (once directly, once via the `0x2000` path) suggesting it's the generic "fire timer-class callbacks" helper. |
| `0x80000c74` | `FUN_80000c74` | `uart_rx_tx_byte_fifo_handler` | 190B. `switch` on `status&0xf` with cases `0,4,0xc,6,7`; cases 4/0xc manage a circular byte buffer (`PTR_DAT_80000d60 + index`, wrapping at `0x10`/`0x100`), incrementing/wrapping an index byte and invoking a drain callback (`(**(code**)(buf+8))(...)`) when the buffer fills (index reaches `0x10`) — the single-byte-at-a-time vs. bulk-copy branching (based on a `0xc0`-mask test) is characteristic of a UART or similar byte-stream peripheral's RX/TX FIFO ISR handler, not a Bluetooth-protocol function. Renamed accordingly; the `tISR_EXTENDED`/`tTimer` strings referenced nearby (`0x8007ae78`/`0x8007ae88`, just outside this gap) support an interrupt/timer-adjacent peripheral driver classification. |
| `0x800012b8` | `FUN_800012b8` | `baseband_event_status_dispatcher_0xd` | 216B. `do/while` loop draining a status word bit-by-bit across **13** possible bits (`uVar8 < 0xd`), each clearing its bit in the global status word, and (for bits whose `0x200>>i` test passes) branching between `FUN_8002af24`/`FUN_8003e1d4` (their choice gated by a per-slot config byte `field+7 bit2`) — a per-source dispatch table over a 13-entry array at `PTR_DAT_80001394` (12 bytes/entry — matches a small connection/baseband-event record size). Falls through to a feature-gated call to `VSC_0xfca1_FUN_80077474` (already-named VSC handler) when a config bit (`field208_0xd8 & 0x4000`) is set. This looks like the **general baseband-controller event dispatcher** (interrupt-class events keyed by source ID 0-0xc), parallel in shape to `conn_type_dispatch_and_esco.md`'s `FUN_80050810` but for a different (smaller, 13-vs-4-entry) event-source table — not yet confirmed as the *same* table, flagged for follow-up. |
| `0x800011fc` | `FUN_800011fc` | `conn_record_pending_data_drain` | 188B. Walks a linked list of buffer nodes (`*(int*)(param_1+0x20)`, `node[3]` as `next`), subtracting/accumulating a requested byte count (`param_2`) against each node's stored length (`node+6` as `ushort`), unlinking fully-consumed nodes into a free list at `param_1+0x28/0x2c`, and (when `param_5` flag is set) optionally writing per-node length+pointer pairs into an output array before advancing — classic **linked-list byte-stream drain/dequeue primitive**. Matches the conn-record pool architecture in `reverse_engineering_conn_record_subsystem.md` (struct field layout: `+0x20` head, +0x28/0x2c free-list head/tail) — likely the actual dequeue primitive that pool uses for buffered TX/RX data, called from `FUN_80000fb8` (a packet/credit scheduler, decompiled but not yet named — see below). |
| `0x8000a780` | `FUN_8000a780` | `find_pool_index_by_addr_and_mark_dirty` | 318B (1 unreachable-block warning from Ghidra, benign — dead branches after a `goto`-heavy decompile). Given an address + a small "which-pool" selector (`param_2` 1/2/3/5), looks up which of 4 fixed-size record pools (entry sizes `0x418`, `0x104`, `0x104`, `0x108` — bytes, matching different connection-record struct sizes already seen in the conn-record-subsystem doc) the address falls within, computes the record's index via integer division by the entry size, disables interrupts (calls the already-documented `disable_interrupts_(clear_LSBit_of_CP0_Status_Register)`), sets a dirty/pending bit at a per-pool-class offset (`0x30`/`0x38`/`0x34`/`0x3c`) and bit position equal to the computed index, then re-enables interrupts. Generic "find-owning-record-and-flag-dirty" primitive shared across multiple pool types — extends `reverse_engineering_conn_record_subsystem.md`'s allocation/lookup story with the dirty-flagging side. |
| `0x8000a8e8` | `FUN_8000a8e8` | `conn_record_periodic_sweep_and_clear_dirty` | 308B. Reads a 32-bit status word, extracts a 3-bit class index (`>>0x1d`), tracks the previous class in a 1-byte history slot, and on class *change* resets three "in-use" flags, ANDs a config mask into a counter, then sweeps a 17-entry array (`uVar10 < 0x11`) clearing bit 0x80 in each entry's flags byte (`field+3`) — i.e., a **periodic dirty/in-use-flag-clearing sweep** over the same kind of fixed-size record array the previous function flags as dirty. Conditionally calls `FUN_8000a2ac` when a "done" flag is clear. Calling pair with `find_pool_index_by_addr_and_mark_dirty` (mark-dirty vs. clear-dirty-on-sweep) strongly suggests these two are the write-side and reclaim-side of the same record-pool dirty-tracking mechanism documented qualitatively (but not by these two function names) in `reverse_engineering_conn_record_subsystem.md`. |

## Decompiled but not yet confidently named (context for next worker)

These were decompiled this turn and have partial evidence, but were judged
not confident enough to commit a real name yet (purpose plausible but
under-verified, or part of a larger cluster better understood as a whole):

- **`0x800007d0`** (68B), **`0x80000820`** (364B) — both take a `uint*`
  status-word pointer, OR feature-enable bits into it, and call
  `FUN_8000c5c8`/`FUN_80011468`/`FUN_80011bf4`; look like the same
  "enable-feature-and-report" pattern as the resolved handlers above
  (called from `connection_event_status_handler`/`isr_bottom_half_status_dispatcher`
  respectively) but their specific feature (`DAT_80000814`/`818`/`81c` —
  3 escalating capability bits) wasn't pinned down.
- **`0x80000fb8`** (550B) / **`0x800011fc`** (now named, drain primitive,
  above) / **`0x800012b8`** (now named, above) — `0x80000fb8` itself looks
  like an **SCO/eSCO packet-credit scheduler**: walks `big_ol_struct` conn
  records, calls `FUN_8002addc`(conn lookup)/`FUN_8002bc88`(alloc?)/`FUN_8003d018`
  (credit/byte-budget calc) in a loop building/committing a packet-length
  table, finally calling `FUN_8002ba10`/`FUN_8002bae0`/`FUN_8002ae50`. Strong
  candidate for a new named function in `reverse_engineering_conn_feature_dispatch.md`'s
  hash-bucket-pool family, but the exact field semantics (`local_58` array,
  `0x7ff`-masked length field) need one more pass before committing a name.
- **`0x80001648`** (728B) and its near neighbors **`0x80001944`** (58B),
  **`0x80001990`** (292B), **`0x80001ad8`** (86B), **`0x80001b3c`** (238B) —
  all manipulate `big_ol_struct[i].field_0x219`/packet-type constants
  `0xc000`/`0x1c00`/`0xc00` already established as SCO/eSCO/role-switch
  packet-type codes in `reverse_engineering_conn_type_dispatch_and_esco.md`,
  and call `FUN_8001483c`/`FUN_80014d50` (role/mode setters, not yet named)
  plus `possible_logger_called_if_no_patch3`. Strong candidate cluster for
  "packet-type/role-switch transition handlers" but the specific HCI/LMP
  procedure each one implements (5 candidates, 1 dispatcher + 4 variants)
  wasn't individually pinned down this turn.
- **`0x80001c4c`** (700B), **`0x80001f34`** (248B), **`0x80002048`** (334B),
  **`0x800021c0`** (254B), **`0x800022e4`** (384B) — all reference
  `big_ol_struct[i].field_0xb7`/`field_0x179`/`field200_0x206` connection
  states, call `set_bos[bosi].0xb2_index=arg2` and
  `send_evt_HCI_Truncated_Page_Complete` (both already-named), and program
  baseband registers with the same `0xc00`/`0x1c00` packet-type words. This
  looks like a **contiguous run of role-switch / truncated-page / link-policy
  event handlers**, structurally similar to the already-documented LC/LMP
  state machine chain in `reverse_engineering_lc_lmp_state_machine.md`, but
  spans 5 functions with enough individual variation (different status-bit
  masks, different struct fields touched) that distinguishing "which HCI/LMP
  procedure is this" needs a slower per-function pass rather than a batch
  guess.
- **`0x8000a4ac`**, **`0x8000a4f8`**, **`0x8000a570`** — a small cluster:
  `0x8000a4f8` is a generic "allocate N table-rows with an optional per-row
  callback" helper; `0x8000a570` calls it 6 times with different fixed
  counts (6, 0xc, 4, 8, 0x11, 0x11, 9) against 3 distinct pool base pointers
  — looks like **one-time pool-initialization code** (sized exactly like the
  17-entry/9-entry tables seen in the resolved sweep function above), called
  once at boot, not a recurring event handler. Plausible to fold into
  `reverse_engineering_conn_record_subsystem.md` as "the pool init
  routine" once the 3 pool identities are confirmed against that doc's
  existing struct-size table.

## Open questions for the continuation ticket

1. Disambiguate `0x8000046c` vs `0x80000480` (genuine alias/thunk or a
   Ghidra function-boundary bug) — a `DiagAddr.java` raw-byte dump at both
   addresses would settle it in one call.
2. Confirm whether `baseband_event_status_dispatcher_0xd`'s 13-entry table
   and `conn_type_dispatch_and_esco.md`'s 4-entry `FUN_80050810` table are
   the same underlying table at different sizes, or genuinely separate
   tables — matters for cross-doc cross-referencing.
3. Pin down the SCO/eSCO credit-scheduler reading at `0x80000fb8` enough to
   give it a real name and fold it into
   `reverse_engineering_conn_feature_dispatch.md`.
4. The 5-function role-switch/truncated-page cluster (`0x80001c4c` family)
   and the 5-function packet-type cluster (`0x80001648` family) are the
   single highest-value remaining targets in the gap — they're dense,
   already mostly decompiled (see above), and sit right next to material
   `lc_lmp_state_machine.md`/`conn_type_dispatch_and_esco.md` already cover,
   so finishing them is likely to be cheap relative to cold, unexplored
   addresses elsewhere in the gap.

## Remaining scope

~208 of the 220 gap functions (220 total in the two gaps minus the 12
resolved this turn) remain untriaged or only contextually decompiled. See
`work-in-progress.txt` for the shrunk continuation ticket's exact address
sub-range.
