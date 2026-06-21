# Reverse Engineering: USB Transport Framing and the ROM-Side HCI RX Driver

**Region**: ROM `0x80009da8`–`0x8000a474` (~1.7 KB), plus the already-documented
top-level command router `assoc_w_tHCI_CMD` (`0x80020ee0`)
**Memory block**: ROM (fixed in chip silicon)
**Source**: Kovah `2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf`, decompiled via
Ghidra 12.1.2 headless, GZF process mode
**Tools used**: `DecompileAddr.java`, `DiagAddr.java`, `GlobalLayout.java`
(`script_args`, one address/symbol per call — no new script needed)
**Date**: 2026-06-21. Ticket: work-in-progress.txt "Document USB/UART
transport framing and the ROM-side HCI driver"

---

## Summary

This pass traces **backward** from `assoc_w_tHCI_CMD` (`0x80020ee0`, the
top-level standard-HCI command router documented in
`reverse_engineering_hci_command_router.md`) looking for its caller and the
USB-level framing that produces the command buffer it consumes. The direct
caller of `assoc_w_tHCI_CMD` was **not found** (see "What was not found"
below) — both available cross-reference tools (`mcp__wairz__xrefs_to`-style
tools and `FindXrefsTo.java`) are non-functional for this GZF/this address
(confirmed, not assumed — see "Tool note"). However, working backward through
the surrounding ROM region using Kovah's own (previously un-indexed) function
labels uncovered a substantial, previously-undocumented **multi-channel
ring-buffer USB transfer-completion driver** at ROM `0x80009da8`–`0x8000a474`,
which is almost certainly the actual transport-to-HCI bridge layer the ticket
asked about, even though the single missing link (the direct call into
`assoc_w_tHCI_CMD`) was not pinned down.

**Confidence: High** for the structure, control flow, and ring-buffer/queue
mechanics of the `0x80009da8`–`0x8000a474` region (all individually
decompiled, consistent shapes across 4 sibling functions). **High** that this
region is USB-transport-related specifically (not UART) based on: (a) the
chip is UB500, a USB-only dongle per CLAUDE.md; (b) the 3-channel
enable/disable/reset state machine (`FUN_8000a310`) and 3-channel register
packing (`FUN_80009dfc`) match a USB endpoint model (e.g. control + 2 bulk/int
pipes) far better than a single UART line; (c) one of the four drain loops
explicitly constructs an **HCI Event packet** (transport type-byte `0x04`)
and hands it to a function-pointer sink, which is the textbook last step of a
USB-side "transfer completed, deliver the received H4-framed bytes to the
HCI layer" handler. **Medium** on the *exact* identity of the missing
`assoc_w_tHCI_CMD` caller — not confirmed by direct disassembly, only
inferred from architecture (see Open Questions).

---

## What was confirmed: `assoc_w_tHCI_CMD`'s own signature and PDU struct

Already documented in `reverse_engineering_hci_command_router.md`, restated
here because it's the anchor this pass worked backward from:

```c
void assoc_w_tHCI_CMD(undefined4 *param_1)
{
  psVar12 = (short *)*param_1;              // *param_1 = pointer to raw HCI cmd bytes (opcode at [0], len at [2])
  ...
  sVar11 = *(short *)(param_1 + 2);          // byte offset 8 of param_1 = dispatch discriminator
  if (sVar11 != 300) { ... }                 // 0x300 = normal HCI command path
  ...
}
```

`param_1` is **not** the raw byte buffer — it's a small descriptor/wrapper
struct: `*param_1` is the pointer to the actual command bytes, and a field at
byte offset 8 (`+8`, a 16-bit value) is a **dispatch-type tag** with three
recognized values: `0x300` (normal command, leads into the OGF/OCF switch
documented in the companion doc), `0x456` and `0x1` (generic
indirect-call/`ret_wrapper` escape hatches, not real opcodes). This 3-way tag
strongly resembles a generic "received-PDU descriptor" shared by more than
one transport/queue type — i.e. whatever builds this descriptor for
`assoc_w_tHCI_CMD` is itself probably a thin wrapper around a lower-level
queue-item struct, not the queue/ring code itself.

---

## What was NOT found: the direct caller of `assoc_w_tHCI_CMD`

Both available tools for finding incoming references were checked and ruled
out, as instructed by the ticket (don't trust stale output silently):

1. **`FindXrefsTo.java`** — confirmed running **stale content**. Invoking it
   against this GZF printed `Target: 801212e4` with two xrefs from
   `FUN_8004f824`/`8004f890` — this is the *old* hardcoded target from a
   previous ticket (`reverse_engineering_interrupt_vectors.md`'s "Tool note"
   already documented this exact bug: `save_ghidra_script` overwrites of an
   existing filename don't propagate to `run_ghidra_headless` execution).
   This output has nothing to do with `assoc_w_tHCI_CMD` and was discarded
   per the ticket's explicit instruction.
2. **`DiagAddr.java`'s "Symbols@target" xref section** — returns empty for
   `assoc_w_tHCI_CMD` (`0x80020ee0`). To rule out "this just means zero
   callers" vs. "the tool doesn't expose xref data at all," the same check
   was run against `HCI_CMD_OGF_01__Link_Control` (`0x80020814`), a function
   we **know** is called directly via a `jal` instruction inside
   `assoc_w_tHCI_CMD` itself (confirmed in the disassembly: `800210d8: jal
   0x80020814`). `DiagAddr.java` returns the same empty xref section for that
   address too. **This proves the tool's "Symbols@target" listing does not
   surface call-graph/reference data at all** (for direct `jal` calls, let
   alone indirect ones) — it is not evidence either way about
   `assoc_w_tHCI_CMD`'s callers, just a confirmed tool limitation.
3. **`GlobalLayout.java`** on `assoc_w_tHCI_CMD` shows only the surrounding
   literal-pool/data layout (pointers used *by* the function), not references
   *to* it.

No batch/new script could be written to do a manual byte-pattern scan for
`jal`-encoded calls to `0x80020ee0` across all of ROM — both wairz script
pools are full (per CLAUDE.md's already-known constraint, re-confirmed by
`reverse_engineering_interrupt_vectors.md`'s "Tool note": 50/50 research
files, 50/50 scripts directory, no delete/evict tool). A handful of
plausible direct-caller candidates were checked individually by decompiling
them and grepping their disassembly for a literal `jal 0x80020ee0` —
`calls_to_0x8010a001_as_fptr_to_install_patches` (`0x800109ac`),
`lots_of_initialization` (`0x8000fb5c`), `assoc_w_tHCI_EVT` (`0x80020bec`),
`called_on_every_HCI_CMD_via_fptr` (`0x80014180`),
`called_at_end_of_every_HCI_CMD_via_fptr` (`0x80009f68`), and all four
ring-drain functions documented below — **none contain a direct call to
`0x80020ee0`**. This means the call is either indirect (through a function
pointer not resolved by static analysis, consistent with this ROM's pervasive
hook-table architecture) or made by a ROM function not checked in this pass
(the ROM has 2700+ functions per `rom/rom_coverage_baseline.md`; an
exhaustive sweep was out of scope/budget for this pass).

---

## What WAS found: a previously-undocumented USB ring-buffer transfer-completion driver

Working backward from `called_at_end_of_every_HCI_CMD_via_fptr` (`0x80009f68`,
already Kovah-named, already known from `kovah_function_list.md`'s "HCI
Infrastructure" table) by inspecting the **surrounding, previously
un-indexed** code in the same `0x80009d00`–`0x8000a474` region turned up a
tight cluster of related functions. Kovah himself had already labeled four of
them as members of a numbered family — `early_fptr1`, `early_fptr5`,
`early_fptr6`, `early_fptr7` (visible via `GlobalLayout.java`, **not** present
in `kovah_function_list.md`, which predates this discovery) — strongly
suggesting these are slots in some `early_fptr[1..N]` table Kovah recognized
but never wrote up. (`early_fptr2`/`3`/`4` were not located in this pass —
see Open Questions.)

### The four drain functions

All four share an identical skeleton: walk a circular queue of fixed-size
slots (8, 16, 16, or 32 slots respectively), check a per-slot status byte for
a "done"/ready bit (`& 0x80`), and if set, dispatch the completed item either
through a registered function pointer or through the shared
`called_at_end_of_every_HCI_CMD_via_fptr` fallback — then advance the read
index modulo the slot count and loop (capped per-call, so each call drains at
most N+1 entries, never blocking indefinitely).

| Function | Kovah label | Slots | Index field | Dispatch on completion |
|----------|-------------|-------|--------------|------------------------|
| `FUN_8000a130` | `early_fptr5` | 8 | `ring[1]`, mod 7 (`&7`, bug or intentional 7-deep) | `PTR_DAT_8000a1bc`(ptr) **or** `called_at_end_of_every_HCI_CMD_via_fptr(ring[idx], 1)` |
| `FUN_8000a074` | `early_fptr6` | 16 | `ring[3]`, mod 0xf | builds arg from `DAT_8000a120`/`8000a124`/`8000a128` (mode 2 vs 5 selection) → `PTR_DAT_8000a12c`(ptr) **or** `called_at_end_of_every_HCI_CMD_via_fptr(ring[idx], mode)` |
| `FUN_80009fd8` | `early_fptr7` | 16 | `ring[5]`, mod 0xf | `PTR_DAT_8000a070`(ptr, arg, 0, **3**) **or** `called_at_end_of_every_HCI_CMD_via_fptr(ring[idx], 3)` |
| `FUN_8000a1c0` | `early_fptr1` | 32 | `ring[8]` (computed from `param_1>>0x18`), mod 0x1f | see below — **builds an HCI Event packet (type byte `0x04`)** |

`called_at_end_of_every_HCI_CMD_via_fptr` (`0x80009f68`, already documented
in `kovah_function_list.md`) takes `(slot_index, mode)` and writes
`1<<slot_index` into one of four different 16-bit MMIO-style words selected
by `mode` (`1`/`2`/`3`/`5` seen as call-site args across these four drain
loops) — i.e. it's a generic **"acknowledge/free this ring slot"** helper,
parameterized by which of (at least) four hardware channels owns the slot.
This is the same function CLAUDE.md/`kovah_function_list.md` already named
as being invoked "at the end of every HCI CMD" — this pass confirms *why*:
every one of these four ring-drain loops calls it as their default
slot-release path, and `assoc_w_tHCI_CMD`'s own epilogue (documented in the
companion doc) ends by clearing its command-in-progress pointer in the same
style. This function is the **common ring-slot release point shared by the
transport-RX drain loops and the HCI command processing epilogue** — strong
structural evidence (though not a direct call-graph edge) that these drain
loops feed `assoc_w_tHCI_CMD`.

### `FUN_8000a1c0` (`early_fptr1`) — the clearest transport→HCI bridge point

```c
void FUN_8000a1c0(uint param_1)
{
  for (param_1 = param_1 >> 0x18; (param_1 &= 0x1f) != 0; param_1--) {
    uVar8 = *DAT_8000a28c;                                 // ring read-index word
    psVar9 = *(short **)(*PTR_DAT_8000a290 + (uVar8&0xf)*4);// slot -> PDU buffer pointer
    if (*psVar9 == -0x3ee /* 0xfc12 as signed i16 */) {
      *PTR_DAT_8000a294 = psVar9[3] & 1;
      called_at_end_of_every_HCI_CMD_via_fptr(uVar8 & 0xf, 1);
    } else if (*PTR_DAT_8000a294 == 0 ||
               (*psVar9 == 0x1802 && psVar9[3] == 0)) {
      (*(code *)*PTR_DAT_8000a29c)(1);                      // generic ack callback
    } else {
      *(byte*)((int)psVar9 + 1) = 0xfc;                      // <-- writes 0xFC into byte[1] of the PDU
      (*(code *)*PTR_DAT_8000a298)
          (4, (byte*)((int)psVar9 + 1), psVar9[1]_lowbyte + 2); // <-- type=4 (HCI Event), ptr, length
    }
  }
  ...
}
```

The third branch is the important one: it patches a byte in the buffer to
`0xFC` (consistent with building/forwarding a Realtek **vendor-specific HCI
event** — events with event-code `0xFF` carrying a `0xFC`-style sub-code are
Realtek's standard vendor-event wrapper) and then calls a function pointer
with **literal argument `4`** as the first parameter. In every standard
Bluetooth UART/USB HCI transport (H4 framing, used by `btrtl`/Linux `hci_usb`
and `hci_uart` alike), the four packet-type values are: `1`=Command,
`2`=ACL data, `3`=SCO data, `4`=Event. **A literal `4` passed as the first
argument to a generic packet-send function, immediately after writing event
bytes into a buffer, is exactly the call signature expected for "transmit
this buffer as an HCI Event packet over the transport."** This is the
strongest direct evidence found in this pass of the **transport packet-type
byte** the ticket asked about, on the **outbound** (event) side. (The
analogous **inbound** packet-type-byte check — i.e. "is `0xfc12`/`0x1802`
compared against the buffer's first two bytes an *opcode* check (already
parsed) or a *raw transport header* check (still framed)?" — could not be
disambiguated with certainty in this pass; see Open Questions. `0xfc12` and
`0x1802` read naturally as 16-bit HCI opcodes (OGF 0x3F OCF 0x12, and OGF 6
OCF 2 respectively) rather than transport type/length bytes, which would
mean this function operates on **already-opcode-parsed** command buffers,
i.e. it sits at the same level as or just below `assoc_w_tHCI_CMD`, not above
the H4 framing layer itself.)

### `FUN_8000a310` — 3-channel enable/disable/reset state machine

A `switch(param_1)` over values 1–5 that enables, flushes, resets, disables,
or reconfigures **three** independent mask-register pairs
(`DAT_8000a47c`/`DAT_8000a488`/`DAT_8000a4a4`, each gated by the same disable
mask `DAT_8000a480`). Case 1 and case 2 both call
`disable_interrupts_(clear_LSBit_of_CP0_Status_Register)` /
`enable_interrupts_(set_CP0_Status_to_arg)` around the register writes
(critical-section protected) and call through two separate function-pointer
slots (`PTR_DAT_8000a48c`, `PTR_DAT_8000a490`) 6 and 12 times respectively —
i.e. flushing/draining 6+12=18 pending callback slots, suggestively close to
the 8+16+16+32 = 72 total ring slots managed by the four drain loops above
(not an exact match, but the same order of magnitude and the same "drain a
fixed small count of pending completions" idiom). **Three independently
controllable channels** is a strong structural match for a USB device
controller with (at minimum) a control endpoint plus IN/OUT bulk or
interrupt endpoints — far more naturally a USB endpoint model than a
single-wire UART, which has no concept of multiple independent channels at
this layer.

### `FUN_80009dfc` — 3-channel register field packer

Takes `(channel, mode, value, extra)` with `channel < 3` enforced (logs and
bails otherwise), and for each of the three channels packs `value` into a
different bit-field of one of two 32-bit control words
(`DAT_80009ee8`/`DAT_80009eec`), using different bit-widths/shifts per
channel (`0x1000`/`0x2000` bit-toggle for channel 0/1, `<<0xd` 3-bit field
for channel 2, gated by a config-blob byte `*PTR_DAT_80009efc & 0x1e == 2`).
This reads as endpoint-specific control-register configuration (e.g. max
packet size, NAK/stall bits, or buffer-depth selection per endpoint) — same
3-channel theme as `FUN_8000a310`.

### Critical-section / interrupt-disable pattern confirms ISR-adjacency

`FUN_8000a310`'s cases 1 and 2 both bracket their register writes with
`jalx 0x80009104` (`disable_interrupts`) / `jalx 0x80009120`
(`enable_interrupts`) — the same 32-bit-MIPS CP0-Status helper pair
documented in `reverse_engineering_interrupt_vectors.md`. This is the
expected pattern for code that races with an ISR writing to the same ring
buffers/status bits — i.e. these drain/control functions are very likely
called from **task-context "ISR bottom half" code** that runs shortly after
(but not inside) the actual hardware interrupt, consistent with the
single-slot ISR dispatcher documented in
`reverse_engineering_interrupt_vectors.md` (`FUN_80033ce8` at the MIPS16e
end of the trampoline, calling through `PTR_DAT_80033d1c` =
`0x80120f84` by default).

---

## Relationship to the already-documented single-slot ISR dispatcher

`reverse_engineering_interrupt_vectors.md` found that **all** hardware
interrupts funnel through one fixed trampoline (`0x80009160`) into one
generic dispatcher (`FUN_80033ce8`) that calls **one** registered handler
slot (`PTR_DAT_80033d1c`, default `0x80120f84`) or falls back to acking bits
in MMIO word `0xb000a0a0`. That doc left open "is there a separate per-source
table, or does every interrupt source (UART, USB, baseband, timer) share that
one slot, swapped in/out by whichever subsystem currently owns it?"

This pass's finding is consistent with **the latter** (shared single slot,
not a hardware vector table) but adds a concrete, ROM-resident answer for
*what the registered handler does once invoked, specifically for the
USB/transport source*: it is (or calls into) something that updates the ring
buffers consumed by the four drain functions documented above. The drain
functions themselves are **not** the ISR handler — they read already-updated
ring state (status byte `& 0x80` "done" bit) — so the actual ISR-context code
that fills the rings and flips that bit was **not located** in this pass (it
may be the contents of `0x80120f84` at runtime, which is RAM-resident and
populated by ROM init or the patch, not a fixed ROM function visible to
static analysis — same limitation already flagged in the interrupt-vectors
doc). The drain functions are the **task-context consumer side**; CLAUDE.md's
ticket question "is there a USB-specific ISR slot, separate from the generic
single-slot dispatcher" is therefore: **no separate hardware vector found,
but yes, there is USB-specific *bottom-half* logic (these four drain loops +
the three-channel control state machine) clearly distinct from, and one layer
below, the single generic ISR slot** — the single slot's *registered handler
contents* is what would actually be USB-specific at runtime, not the
dispatch mechanism itself.

---

## RAM control-block region (new finding, distinct from `bos_base`/`config_base`)

All of the pointers referenced by this region's functions
(`0x801216e4`–`0x80121878`, `0x80120bd0`–`0x80120bda`, `0x80120220`,
`0x8012b7ac`, `0x80120dd0`, `0x80122518`–`0x80122564`) live in RAM regions
**distinct from and lower than** the already-documented `bos_base`
(`~0x80121200`–`0x8012dc50`, BT connection-state structs) and `config_base`
(`0x80120070`, the config blob). This confirms the USB transport layer has
its **own independent RAM control-block family** around
`0x80120200`–`0x80122564`, not reusing the BT-protocol-level hook slots
(`bos_base+0xd8`/`+0xe0`/`+0xe4`) documented in CLAUDE.md's "Key Struct
Offsets" table. This is new information: prior docs only mapped the
BT-protocol-level hook architecture; this pass shows the USB/transport layer
has a structurally similar but physically separate hook/control-block
architecture.

---

## Packet-type-byte dispatch: partial answer

The ticket asked specifically for code that "reads a packet-type byte and
branches 4 ways (cmd/ACL/SCO/event)." This pass found:

- **Outbound**: `FUN_8000a1c0` explicitly constructs a type-`4` (Event)
  packet and a generic ack/callback path (type implied `1`, given the
  `0xfc12`/loopback-style context) — i.e. half of a 4-way encoder, on the
  send side, confirmed by direct decompilation.
- **Inbound**: **not found**. No single function was located that reads an
  incoming byte, tests it against `{1,2,3,4}`, and branches to
  cmd/ACL/SCO/event handlers respectively. `assoc_w_tHCI_CMD` itself starts
  *after* this split has already happened (it only ever sees command
  buffers). The analogous "ACL" and "SCO" inbound paths were not located in
  this pass at all — only the command-buffer and event-buffer sides were
  touched by the functions this pass reached backward from
  `called_at_end_of_every_HCI_CMD_via_fptr`.

This is left as explicitly open (see below) rather than guessed at.

---

## Open Questions / Future Work

1. **Direct caller of `assoc_w_tHCI_CMD` still not found.** Most likely an
   indirect call through a function pointer (this ROM's dominant pattern
   everywhere else), populated by whichever code processes a completed
   command-ring entry. The natural next step is finding what writes into
   the ring read by `FUN_8000a1c0`/`FUN_8000a130`/etc. and following *that*
   forward, rather than continuing to search backward from
   `assoc_w_tHCI_CMD` — but this needs either a working xrefs tool or a new
   batch script (both blocked — see Tool note).
2. **`early_fptr2`, `early_fptr3`, `early_fptr4` not located.** Only
   `early_fptr1`, `5`, `6`, `7` were found via `GlobalLayout.java` lookups on
   functions reached organically from `called_at_end_of_every_HCI_CMD_via_fptr`'s
   call sites. If a true `early_fptr[1..7+]` table exists, slots 2–4 likely
   sit in the address gap between `0x8000a474` (end of `FUN_8000a310`) and
   `0x80009da8` (start of this region) or just below `0x80009da8` — not
   probed in this pass.
3. **The actual ISR-context producer that fills these rings and sets the
   `&0x80` done bit was not located** — same limitation flagged in
   `reverse_engineering_interrupt_vectors.md` (RAM-resident handler slot
   `0x80120f84`, contents not visible to static GZF analysis).
4. **Inbound ACL/SCO transport paths not found at all.** Only command and
   event sides were touched. A full picture of "raw USB bytes in" → "ACL/SCO
   data path" remains entirely open.
5. **Exact USB controller register addresses/protocol not determined.** This
   pass found the **software-side ring/queue management** (RAM descriptor
   arrays, status bits, channel enable/disable state machine) but did not
   identify which `0xb000a0xx`-family MMIO addresses (if any — see
   `reverse_engineering_rom_regs.md`/`reverse_engineering_interrupt_vectors.md`
   for the one confirmed family, baseband-register-focused) correspond to USB
   endpoint FIFOs, if the USB controller's registers live in that same MMIO
   block or a separate one. Not probed in this pass.
6. **`0xfc12` / `0x1802` comparisons' true layer not disambiguated** — read
   naturally as already-parsed 16-bit HCI opcodes (OGF 0x3F/OCF 0x12 and
   OGF 6/OCF 2), which would place `FUN_8000a1c0` *below* opcode parsing, not
   at the raw H4-framing boundary. If so, the true byte-level H4
   packet-type-byte split happens somewhere between the (unlocated) USB ISR
   and this ring-drain layer, not inside it. Flagged as the most likely
   place a future pass should focus.
7. **UART path not addressed at all** — CLAUDE.md and the chip's marketing
   name (UB500) indicate this is a USB-only part; no UART-specific code was
   sought or found, consistent with the ticket's framing ("Look for
   USB endpoint/descriptor handling code...").

None of the above block real progress: this pass establishes with high
confidence that ROM `0x80009da8`–`0x8000a474` is a previously-undocumented,
multi-channel (3-endpoint-shaped) USB ring-buffer transfer-completion driver
sitting structurally between the (already-documented) single-slot ISR
dispatcher and the (already-documented) `assoc_w_tHCI_CMD` HCI command
router, with one drain path (`FUN_8000a1c0`) explicitly building and sending
HCI Event packets (transport type-byte `4`) — direct evidence of the
transport-to-HCI bridge the ticket asked about, even though the single
missing call-graph edge (what literally calls `assoc_w_tHCI_CMD`) remains
open.

---

## Tool note

Per CLAUDE.md's "wairz modifications" rule and the ticket's explicit
instructions, the following were verified rather than assumed:

- `FindXrefsTo.java` confirmed still running stale content (prints
  `Target: 801212e4`, the old hardcoded target from a prior ticket) —
  consistent with the already-documented `save_ghidra_script`
  overwrite-doesn't-propagate bug
  (`reverse_engineering_boot_reset_sequence.md`,
  `reverse_engineering_interrupt_vectors.md`). Not re-filed as a new bug,
  just re-confirmed.
- `DiagAddr.java`'s "Symbols@target" xref listing was proven to return empty
  even for a function with a **known, disassembly-confirmed** direct caller
  (`HCI_CMD_OGF_01__Link_Control` at `0x80020814`, called via `jal` from
  inside `assoc_w_tHCI_CMD` itself) — i.e. it does not expose Ghidra's
  reference database at all, for direct or indirect calls. This is a tool
  gap worth fixing in wairz (a working `xrefs_to` against GZF-process-mode
  binaries would have resolved this ticket's core question directly) but is
  not a new blocker beyond what CLAUDE.md already documents for this GZF.
- No new Ghidra script was attempted (both script-storage pools already
  confirmed full as of `reverse_engineering_interrupt_vectors.md`, not
  re-checked this pass since no save attempt was made).
- `DecompileAddr.java`/`DiagAddr.java`/`GlobalLayout.java` via `script_args`
  (one address/symbol per call, `0x`-prefixed per the already-documented
  convention) were sufficient for everything in this doc.

---

## Related documents

| File | Content |
|------|---------|
| `rom/reverse_engineering_hci_command_router.md` | `assoc_w_tHCI_CMD` (`0x80020ee0`) full decompile, OGF dispatch table — the function this doc traces backward from |
| `rom/reverse_engineering_vsc_dispatcher.md` | `FUN_80047c50`, LMP VSC leaf handler; `hci_event_sender` (`0x8001d070`) — the *outbound* PDU-send primitive at the LMP/VSC layer, sibling to this doc's `FUN_8000a1c0` outbound event path at the transport layer |
| `rom/reverse_engineering_interrupt_vectors.md` | Single-slot ISR dispatcher (`FUN_80033ce8`, `PTR_DAT_80033d1c`=`0x80120f84`) and the `0xb000a0a0`/`0xb000a0bc` MMIO family this doc's ring-drain functions are the likely task-context consumers of |
| `kovah_function_list.md` | `called_at_end_of_every_HCI_CMD_via_fptr` (`0x80009f68`) and `called_on_every_HCI_CMD_via_fptr` (`0x80014180`) — pre-existing names this doc builds on; does NOT yet list the `early_fptr1/5/6/7` family found in this pass (follow-up: add them) |
