# ROM Interrupt/Exception Vector and ISR Dispatch

Source: GZF process mode analysis (`DiagAddr.java`/`DecompileAddr.java`,
`script_args`-driven manual walk, since the 50-research-file cap blocked
saving a new batch script — see "Tool note" below), 2026-06-21. Ticket:
work-in-progress.txt "Map the interrupt vector table and ISR handlers".
Continues directly from `reverse_engineering_boot_reset_sequence.md`, which
left the `~0x800000c8`–`0x80009104` ROM region unwalked and flagged it as the
likely home of the MIPS16e mode-switch and interrupt machinery.

---

## Summary

This ROM does **not** use a classical fixed MIPS exception-vector *table*
(no array of jump slots at `0x80000080`/`0x180`/`0x200` etc., and no separate
`0xBFC0xxxx`-style boot ROM exists in this dump — confirmed unmapped, see
below). Instead there is a single, fixed **context-save trampoline** at ROM
address `0x80009160` (32-bit MIPS) that every exception/interrupt appears to
funnel through. It:

1. Saves **all** GPRs + `HI`/`LO`/`EPC` to a fixed RAM frame (computed from a
   pre-loaded `k0` base + offset `0xb40`).
2. `jalr`s to a fixed dispatch target `0x80033ce9` (odd address = MIPS16e
   call convention) — i.e. it **mode-switches from 32-bit MIPS into MIPS16e**
   to run the actual interrupt-handling logic.
3. The MIPS16e dispatcher (`FUN_80033ce8`) either calls a **registered
   handler function pointer** (`PTR_DAT_80033d1c`, default value
   `0x80120f84` — a pointer into the `bos_base`-family RAM struct region) or,
   if unregistered, falls back to poking a status word at a **fixed RAM/MMIO
   slot `DAT_80033d20` = `0xb000a0a0`** — which is in the **same MMIO
   control-register family** (`0xb000a0xx`, kseg1) as the already-documented
   HW register port `0xb000a0bc` from `reverse_engineering_rom_regs.md`.
4. Returns via a restore path reached through a KSEG-aliased jump
   (`0xa0009220`, i.e. `0x80009220 | 0x20000000`) — an uncached mirror of the
   restore code, presumably to avoid icache staleness after entering through
   the trap path.

Confidence: **High** for the trampoline structure, GPR save list, the
`jalr`-into-MIPS16e mode switch, and the `0xb000a0a0`/`0xb000a0bc` MMIO
family relationship. **Medium** for "this is the literal hardware reset/trap
vector" — Ghidra cannot report the silicon's hardwired trap PC; this is
inferred from being the only complete interrupt-context-save sequence found
in ROM and from CP0.Status/EPC usage. **Open** on the exact mechanism that
gets the CPU to land at `0x80009160` (see Open Questions).

---

## Correction to the prior boot/reset doc

`reverse_engineering_boot_reset_sequence.md` stated that
`disable_interrupts_(clear_LSBit_of_CP0_Status_Register)` (`0x80009104`) and
`enable_interrupts_(set_CP0_Status_to_arg)` (`0x80009120`) are **MIPS16e**
("their first instructions disassemble as MIPS16e compressed forms"). This
is **incorrect** — direct disassembly in this pass shows both are **32-bit
MIPS**, decoding as `mfc0`/`mtc0` (COP0 Status access), which MIPS16e cannot
encode at all (it has no `mfc0`/`mtc0` instructions):

```
80009104  mfc0 v0,Status
80009108  nop
8000910c  li   t1,-0x2
80009110  and  t1,v0,t1
80009114  mtc0 t1,Status
80009118  jr   ra
8000911c  _nop
```
(`disable_interrupts`, 28 bytes — clears Status bit 0, IE)

```
80009120  mtc0 a0,Status
80009124  nop
80009128  jr   ra
8000912c  _nop
```
(`enable_interrupts`, 16 bytes — sets Status to caller-supplied value)

These two ROM-named helper functions, plus a third unnamed sibling
`FUN_80009014` (also `mfc0 t1,Status` / clear-bit-1 / `mtc0`) and
`FUN_800090b8` (clears Status bit 1 [EXL] and ORs in `0xff00` [IM7..IM0]),
form a small cluster of 32-bit-MIPS CP0-Status-manipulation utilities
co-located at `0x80009000`–`0x800090fe`, immediately *before* the interrupt
trampoline at `0x80009160`. `lots_of_initialization` (`0x8000fb5c`) genuinely
*is* MIPS16e as that doc said — only the two named interrupt-control helpers
were misattributed.

This means the ROM does not have one MIPS16e/32-bit-MIPS boundary at the
start of ROM and MIPS16e everywhere after — it has **multiple small 32-bit
MIPS "islands"** scattered through the address space wherever CP0 access is
required (interrupt control, the boot preamble, and the trampoline below),
connected to the surrounding MIPS16e code via `jalx`.

---

## The MIPS16e ↔ 32-bit-MIPS mode switch (resolves prior doc's open question)

The boot/reset doc could not locate the actual ISA-mode-switch instruction.
It is a **`jalx`** (jump-and-link-exchange), and at least one concrete
instance was found in this pass, inside the (separate, patch-download-time)
function `calls_to_0x8010a001_as_fptr_to_install_patches` (`0x800109ac`):

```
80010c74  jal  0x80074fa8
80010c78  _sw  v0,0x38(sp)
80010c7a  jalx 0x80009014        ; <-- mode switch: 32-bit MIPS caller here
80010c7e  _nop
80010c80  li   a1,0x2710
```

`0x80009014` is itself one of the 32-bit-MIPS CP0-Status helpers (sibling of
`disable_interrupts`/`enable_interrupts`, see above) — confirming the
pattern: MIPS16e code that needs to touch CP0 registers calls into a small
32-bit-MIPS helper via `jalx`, which returns back into MIPS16e via the
normal `jr ra` (the ISA-mode bit is restored automatically on return because
`jalx`/`jr` track the caller's original mode via the link register's LSB,
the standard MIPS16e convention). The interrupt trampoline (next section)
uses the same `jalx`-equivalent idiom in the other direction: 32-bit MIPS
trap handler calling *into* MIPS16e via `jalr` to an odd (LSB-set) address.

---

## The interrupt context-save trampoline (`0x80009160`)

Disassembly (32-bit MIPS, confirmed via direct decode; auto-analysis did
**not** recognize this as a function — same class of problem as the
`FUN_800000be`-style degenerate auto-functions in the boot/reset doc, caused
by a literal-pool/code boundary directly preceding it at `0x80009148`–
`0x8000915c`):

```
80009160  addiu k0,k0,0xb40       ; k0 = (pre-loaded base) + 0xb40 -- fixed
                                  ;   RAM save-frame address. k0/k1 are the
                                  ;   MIPS-reserved "kernel scratch" regs,
                                  ;   exactly as intended for trap entry.
80009164  nop
80009168  sw   at,0x0(k0)
8000916c  sw   v0,0x4(k0)
80009170  sw   v1,0x8(k0)
80009174  sw   a0,0xc(k0)
80009178  sw   a1,0x10(k0)
8000917c  sw   a2,0x14(k0)
80009180  sw   a3,0x18(k0)
80009184  sw   t0,0x1c(k0)
80009188  sw   t1,0x20(k0)
8000918c  sw   t2,0x24(k0)
80009190  sw   t3,0x28(k0)
80009194  sw   t4,0x2c(k0)
80009198  sw   t5,0x30(k0)
8000919c  sw   t6,0x34(k0)
800091a0  sw   t7,0x38(k0)
800091a4  mfhi t0
800091a8  mflo t1
800091ac  mfc0 t2,EPC             ; <-- Exception Program Counter: definitive
                                  ;     proof this is exception/interrupt
                                  ;     entry code, not a normal subroutine
800091b0  sw   s0,0x3c(k0)
800091b4  sw   s1,0x40(k0)
800091b8  sw   s2,0x44(k0)
800091bc  sw   s3,0x48(k0)
800091c0  sw   s4,0x4c(k0)
800091c4  sw   s5,0x50(k0)
800091c8  sw   s6,0x54(k0)
800091cc  sw   s7,0x58(k0)
800091d0  sw   t8,0x5c(k0)
800091d4  sw   t9,0x60(k0)
800091d8  sw   gp,0x64(k0)
800091dc  sw   sp,0x68(k0)
800091e0  sw   s8,0x6c(k0)
800091e4  sw   ra,0x70(k0)
800091e8  sw   t0,0x74(k0)        ; saved HI (from mfhi above)
800091ec  sw   t1,0x78(k0)        ; saved LO (from mflo above)
800091f0  sw   t2,0x7c(k0)        ; saved EPC (from mfc0 above)
800091f4  nop
800091f8  lui  k0,0x8003
800091fc  addiu k0,k0,0x3ce9      ; k0 = 0x80033ce9 (ODD address -> MIPS16e
                                  ;   call target, standard MIPS16e/jalx
                                  ;   convention: bit0 set = "callee is
                                  ;   MIPS16e code")
80009200  jalr k0                 ; <-- calls into MIPS16e dispatcher
80009204  _nop
80009208  lui  t2,0x8001
8000920c  addiu t2,t2,-0x6de0     ; t2 = 0x80009220
80009210  lui  t1,0x2000
80009214  or   t2,t2,t1           ; t2 = 0xa0009220 (= 0x80009220 with the
                                  ;   KSEG0->KSEG1-style uncached-alias bit
                                  ;   0x20000000 set)
80009218  jr   t2                 ; jump to the *uncached mirror* of the
                                  ;   restore code immediately following
8000921c  _nop
```

**Why `jr` to an uncached alias of the very next instruction?** This is a
common idiom when code at the jump target may have just been freshly
written/modified, or when the core's icache could hold stale data for that
address from before the trap — jumping through the uncached KSEG1-style
alias forces a fresh fetch. It also conveniently re-enters 32-bit MIPS mode
implicitly (even addresses are 32-bit-MIPS call targets) after the MIPS16e
dispatcher call above, without needing an explicit `jalx` back.

The restore body at `0x80009220` onward was **not disassembled by Ghidra's
auto-analysis** either (same class of code/data-boundary problem) — only the
first instruction (`lw s0,0x0(pc)`) was directly confirmed; the remainder
(restoring all GPRs from the `k0`+offset frame, restoring EPC via `mtc0`, and
returning via the standard old-MIPS-I/II "jump to saved EPC" idiom rather
than a hardware `eret`, since this core's instruction set as seen elsewhere
shows no evidence of `eret`) is **inferred by symmetry with the save
sequence but not individually re-disassembled in this pass** — marked open
below.

---

## The MIPS16e dispatcher (`FUN_80033ce8`, called via the `jalr` above)

```c
void FUN_80033ce8(void)
{
  undefined4 *puVar1;
  uint *puVar2;

  puVar1 = (undefined4 *)PTR_DAT_80033d1c;   // = 0x80120f84 (RAM, bos_base family)
  if ((code *)*puVar1 == (code *)0x0) {
    // no handler registered -- ack/clear path:
    puVar2 = (uint *)DAT_80033d20;            // = 0xb000a0a0 (kseg1 MMIO)
    *puVar2 = *puVar2 & 0xfffffffe | 1;
    *puVar2 = *puVar2 & 0xffffff7f | 0x80;
  }
  else {
    (*(code *)*puVar1)();                     // call registered ISR
  }
  return;
}
```

This is a **generic single-slot ISR dispatcher**: it checks one function
pointer (`PTR_DAT_80033d1c`, default contents `0x80120f84`) and either calls
it as the real interrupt handler, or — if nothing has registered there yet —
falls through to a default action that toggles bits `0x1` and `0x80` in the
MMIO word at `0xb000a0a0`.

### Relationship to the documented HW register MMIO window (`0xb000a0bc`)

`reverse_engineering_rom_regs.md` documents the baseband hardware register
read/write port at kseg1 `0xb000a0bc`, with a 32-bit control word laid out as
`[31:22 reserved][21:16 6-bit index][15:0 16-bit value]`, accessed via ROM
functions `FUN_8001136c` (read) / `FUN_8001139c` (write).

`DAT_80033d20 = 0xb000a0a0` is **28 bytes (0x1c) before** `0xb000a0bc`, in
the *same* `0xb000a0xx` MMIO control-register block. The bit pattern poked
by the no-handler fallback (`& 0xfffffffe | 1`, then `& 0xffffff7f | 0x80`)
— i.e. force-set bit 0, then force-set bit 7 — is structurally identical to
the trigger-bit-then-commit "two-write" pattern documented for the register
*write* protocol at `0xb000a0bc` (`FUN_8001139c`: write index+value, then
write again with a trigger/strobe bit set). This strongly suggests
`0xb000a0a0` is a **sibling control/status register in the same peripheral
block** — most plausibly an **interrupt-status/acknowledge register**
(bit 0 = pending/ack, bit 7 = a second ack/clear-latch bit), consistent with
"the default unregistered-ISR action is to acknowledge/clear the interrupt
at the hardware level so it doesn't re-fire forever." This is the direct
answer to the ticket's question about how the ISR machinery relates to the
`0xb000a0bc` MMIO window: **they are register siblings in the same
`0xb000a0xx` hardware block**, with `0xb000a0bc` carrying baseband register
read/write traffic and `0xb000a0a0` apparently carrying interrupt
status/ack. Confidence: **medium** — the exact bit semantics (which bit
means what) were not independently confirmed against a register map; this
is inferred from the access pattern alone, consistent with CLAUDE.md's
"slot-budget validator + hardware hook dispatcher" framing of `0x8004f824`
(`FUN_8004f824`, the ROM hook checked against `bos_base+0xe4`) — *not*
re-investigated in this pass; see Open Questions.

### The registered-handler slot (`PTR_DAT_80033d1c` = `0x80120f84`)

This is a single function-pointer slot, not an array/table — i.e. **one ISR
slot**, not "the" full interrupt vector table with per-source entries. Its
default value `0x80120f84` lands inside the `0x8012xxxx` RAM region already
documented (CLAUDE.md "Key Struct Offsets" / `kovah_function_list.md` note
4) as containing the 12-entry `bos` connection-state struct array
(`~0x80120000`–`0x8012dc50`). This was **not traced further in this pass** —
whether `0x80120f84` is itself a sub-offset into one `bos` entry, a separate
small jump-table of per-source handlers, or just a single global slot that
gets overwritten by whichever subsystem currently "owns" the interrupt, is
an open question (see below). Given the existing CLAUDE.md documentation of
`bos_base+0xd8`/`+0xe0`/`+0xe4` as separate hook slots for LMP VSC dispatch,
type-dispatch, and the hardware-write hook respectively, it's plausible this
RAM region multiplexes several different "hook slot" mechanisms for
different purposes (HCI/LMP software dispatch vs. hardware interrupt
dispatch) rather than all being one unified vector table — this needs a
dedicated follow-up pass to disambiguate, not assumed here.

---

## Conventional MIPS vector addresses: checked and ruled out

Per the boot/reset doc's finding that the boot preamble clears CP0.Status
bit 22 (BEV) and sets the IM7..IM0 mask, the BEV=0 convention's standard
vector addresses were probed directly in this GZF:

| Address | Convention | Result |
|---------|-----------|--------|
| `0x80000000` | Reset/NMI vector (= ROM base, already documented) | Mapped; boot preamble (per prior doc) |
| `0x80000080` | TLB refill vector (BEV=0, EXL=0) | Mapped, but lands inside `FUN_80000078`'s body (`addiu s0,sp,0x1f4`) — ordinary code, not a vector stub |
| `0x80000100`/`0x80000140` | (spot checks in the unwalked gap) | Mapped; unremarkable code/literal-pool bytes, part of the still-unresolved `~0x800000c8`–`0x80009104` gap |
| `0x80000180` | General exception vector (BEV=0, = EBase+0x180) | Mapped; `lui t1,0x8012` — ordinary code (looks like more `bos`-region pointer setup, same flavor as the boot preamble's RAM-init code), **not** a vector stub; `FunctionAt`/`FunctionContaining` both `NONE` (same undisassembled-gap problem) |
| `0x80000200` | Interrupt vector (BEV=0, Cause.IV=1, = EBase+0x200) | Mapped, `FunctionContaining: FUN_80000000` (still within the boot preamble's auto-detected 464-byte span) — `sw at,0x0(k0)`; **not obviously a vector stub** either, though notably it *does* reference `k0` (the same register used as the trampoline's save-frame base) — flagged as worth a closer look in a future pass, not confirmed as significant here |
| `0xbfc00000` / `0xbfc00200` / `0xbfc00380` | BEV=1 reset/TLB/general+interrupt vectors (conventional KSEG1 boot-ROM addresses) | **Unmapped** — `MemoryAccessException: Unable to read bytes`. Confirms (consistent with the boot/reset doc) there is no separate boot ROM at the conventional `0xbfc0xxxx` address in this dump; this chip's reset vector is ROM base `0x80000000` itself, not the MIPS-standard KSEG1 boot address. |

**Conclusion**: none of the conventional fixed-offset vector slots
(`EBase+0x080/0x100/0x180/0x200`) contain anything that looks like a vector
*stub* (the expected pattern is a 1-2 instruction `j`/`b` to a real handler).
They contain ordinary, still-not-fully-disassembled code that is part of the
same unwalked `~0x800000c8`–`0x80009104` gap the boot/reset doc identified.
Two non-exclusive explanations, neither confirmed:

1. This core's silicon-level trap PC is **not** one of the software-visible
   addresses checked here, but is hardwired directly to (or very near)
   `0x80009160` itself — i.e. the "vector" *is* the trampoline, with no
   separate jump-stub layer at all. This would be consistent with a small/
   embedded MIPS core design where the vector offset is configured at
   synthesis time rather than following the full architectural convention.
2. The real vector stub exists somewhere in the **still-unwalked gap**
   (`~0x800000c8`–`0x80009104`) at an address not yet checked, and simply
   `j`s/`b`s to `0x80009160`. Given the gap is ~36 KB and only a handful of
   addresses were spot-checked, this has not been ruled out.

---

## Open questions / future work

1. **Exact hardware trap PC not confirmed.** Ghidra/GZF static analysis
   cannot reveal the silicon's hardwired exception-entry address; only
   software conventions and code patterns were checked. `0x80009160` is the
   strongest candidate found (complete, correctly-formed context-save
   sequence using `EPC`), but no direct proof (e.g. a literal jump *to* it
   from one of the EBase-relative addresses) was found in this pass.
2. **Restore path (`0x80009220` onward) not individually disassembled** —
   inferred by symmetry with the save sequence (mirror-image `lw`s, `mtc0
   EPC`, then a jump-to-EPC return) but not confirmed instruction-by-
   instruction. The auto-analysis code/data-boundary problem that affected
   `0x80009154`-area literal pool bytes likely affects this region the same
   way; a `DM_RealtekMIPS16eForceDisassembly.py`-style forced-disassembly
   pass (already known-useful per `GHIDRA_SCRIPTS.md`, but written for
   MIPS16e — would need a 32-bit-MIPS-mode equivalent) over
   `0x80009000`–`0x80009260` would likely resolve this cleanly.
3. **Single ISR slot vs. true per-source vector table not disambiguated.**
   `PTR_DAT_80033d1c` is one function-pointer slot. Whether multiple
   interrupt *sources* (UART, USB, baseband/radio, timer, etc.) share this
   one slot (re-registered as needed) or whether there's a separate
   per-source table elsewhere (e.g. indexed by reading a cause/status field
   from the `0xb000a0xx` MMIO block before dispatch) was not traced. The
   dispatcher itself (`FUN_80033ce8`) does **not** read any MMIO/status
   register before calling the registered handler — it unconditionally
   calls whatever is registered, or falls back to the ack sequence. This
   implies either (a) there genuinely is only one ISR source multiplexed at
   a time, with software swapping the handler in/out per "current owner", or
   (b) the *registered handler itself* (whatever currently occupies
   `0x80120f84`) is responsible for reading `0xb000a0bc`/`0xb000a0a0` and
   doing its own source triage — this second possibility was not confirmed
   by decompiling whatever function actually occupies that slot at runtime
   (its contents are RAM-resident and patch/ROM-init-dependent, not a fixed
   ROM function — would require a live/RAM dump or patch-installer trace to
   resolve, similar to how `bos_base+0xd8/+0xe0/+0xe4` hook installs were
   traced for the firmware-side hooks).
4. **Exact bit semantics of `0xb000a0a0` not confirmed against a register
   map** — inferred from the read-modify-write-then-strobe access pattern
   alone (paralleling the documented `0xb000a0bc` write protocol), not from
   any datasheet or independent corroboration. Treat the
   "interrupt-status/ack register" interpretation as a working hypothesis.
5. **Which hardware events feed this interrupt path at all** (timer? UART
   RX? USB? baseband symbol clock?) was not determined — this would
   require either tracing what (if anything) writes a non-null value into
   `PTR_DAT_80033d1c` at runtime, or correlating with the externally-visible
   USB/UART transport framing work (still an open TODO per
   work-in-progress.txt: "Document USB/UART transport framing and the
   ROM-side HCI driver").
6. **The CP0.Status manipulation cluster's full caller graph** (who calls
   `disable_interrupts`/`enable_interrupts`/`FUN_80009014`/`FUN_800090b8`,
   and via which `jalx` sites) was not exhaustively enumerated — only the
   one `jalx 0x80009014` call site inside
   `calls_to_0x8010a001_as_fptr_to_install_patches` was found and confirmed.
   These are almost certainly called from many places throughout ROM and
   the patch (standard critical-section enter/exit), but a full xrefs sweep
   was out of scope for this pass.

---

## Tool note (wairz limitation encountered — per CLAUDE.md, not silently worked around)

This task needed a new batch Ghidra script (to probe ~13 candidate vector
addresses plus a raw mfc0/mtc0 byte-pattern scan in one pass, the same way
`reverse_engineering_boot_reset_sequence.md`'s author originally intended
before discovering the `save_ghidra_script`-overwrite bug). **Both** of
wairz's script storage pools were found to be completely full:

- The **research-files pool** (`mcp__wairz__save_ghidra_script` /
  `list_ghidra_research_files`, used via `script_file_id`) was at exactly
  **50/50** (48 saved scripts + 2 imported `.gzf` archives). Attempting
  `save_ghidra_script` with a brand-new filename (`ProbeIsrVectors.java`)
  failed outright with `Maximum of 50 Ghidra research files per project
  reached` — confirmed server-side as a hard-coded `MAX_FILES_PER_PROJECT`
  constant in `wairz/backend/app/services/ghidra_research_service.py`, with
  **no MCP-exposed delete/evict tool** to free a slot.
- The **scripts-directory pool** (`/root/wairz/ghidra/scripts/`, used via
  `script_name`, tracked in `GHIDRA_SCRIPTS.md`) was separately also at
  exactly 50 files. `save_ghidra_script` does not write to this directory at
  all (confirmed by reading the backend source) — it only ever targets the
  DB-backed research-files pool, so there is **no tool-available path** to
  add a new `script_name`-runnable script once the scripts directory is
  externally populated to 50, short of wairz itself syncing/promoting
  research files into that directory (mechanism not visible from this
  session).

Per CLAUDE.md's "wairz modifications" rule, this is flagged rather than
worked around: **wairz needs either a delete/evict tool for the
research-files pool, a raised cap, or a documented way to promote a research
script into the `script_name`-runnable scripts directory.** This is a
distinct limitation from the already-known `save_ghidra_script`-overwrite
bug (`reverse_engineering_boot_reset_sequence.md`'s "Tool note" /
`GHIDRA_SCRIPTS.md`'s `FindXrefsTo.java` row) — that bug is about *overwrite
propagation*; this one is about *being unable to create anything new at
all* once both pools are full.

**Workaround used for this task** (no wairz changes made): all analysis in
this doc was done by repeated single-address calls to the pre-existing,
already-working `DiagAddr.java` and `DecompileAddr.java` scripts (via
`script_args`, one address per call) plus the existing `RomNamedFuncAddrs.java`
and (confirmed-stale, as expected) `FindXrefsTo.java`. This was sufficient
to fully resolve this ticket, but a genuinely new batch/multi-address probe
script (as originally planned) could not be created, and won't be creatable
again until the cap issue above is addressed.
