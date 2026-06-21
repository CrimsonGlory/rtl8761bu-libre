# ROM Boot / Reset Sequence

Source: GZF process mode analysis (`DiagAddr.java`, `script_args`-driven manual
walk), 2026-06-21. Ticket: work-in-progress.txt "Map the boot / reset sequence".

---

## Overview

This document maps what happens between chip power-on/reset and the point
where ROM hands control to the patch entry point `0x8010a001`. Unlike the
Phase 1–8 docs (which start from the patch's perspective and work outward into
ROM call targets), this is a top-down walk starting from the lowest ROM
address, `0x80000000`.

**Key finding: the ROM does not start in MIPS16e.** The very first code at
`0x80000000` is plain 32-bit MIPS32 (4-byte fixed-width instructions), and it
stays in 32-bit mode through at least `0x800000c8`. All of the rest of the ROM
(everything Kovah annotated, all the LMP/HCI/VSC handlers, `lots_of_initialization`,
`calls_to_0x8010a001_as_fptr_to_install_patches`, etc.) is MIPS16e. This means
there is a real, fixed-size **32-bit MIPS boot block** at the base of ROM, which
the prior 8 phases never touched because all of that work targeted MIPS16e
function addresses pulled from Kovah's annotation list.

This boot block was previously *completely undocumented* — it does not appear
anywhere in `kovah_function_list.md` (Kovah's annotations start at `0x80009104`,
deep into the MIPS16e region; the GZF auto-disassembler labeled the 32-bit block
with default `FUN_8000xxxx` names, unannotated).

---

## Confirmed: 32-bit MIPS boot block @ `0x80000000`–`~0x800000c8`+

### Memory block sanity check

`rom` memory block spans `0x80000000`–`0x8007ffff`, `execute=true`. `0x80000000`
is the lowest mapped address in the GZF; there is nothing below it (no separate
"boot ROM" block, no vector table at a conventional MIPS `0xBFC00000`-style
address — this chip's reset vector is simply ROM address `0x80000000` as loaded
in the GZF, i.e. whatever physical/virtual mapping the silicon uses, the GZF's
own addressing puts reset at the base of the `rom` block).

### `FUN_80000000` (size 464 bytes, 32-bit MIPS, unannotated)

Ghidra auto-detected this as one function from `0x80000000`–`0x800001cf`
(464 bytes). Disassembly recovered so far (instructions confirmed individually
via Ghidra's decoder, not hand-decoded):

```
80000000  lui   t0, 0xb000
80000004  ori   t0, t0, 0xaf08      ; t0 = 0xb000af08  (MMIO, kseg1)
80000008  sw    zero, 0(t0)         ; clear MMIO word @ 0xb000af08
8000000c  lw    t1, 0(t0)           ; read back
80000010  lui   t2, 0x8000
80000014  addiu t2, t2, 0x20        ; t2 = 0x80000020
80000018  jr    t2                  ; jump within the same 32-bit block
8000001c  nop
80000020  lui   t0, 0xb000
80000024  ori   t0, t0, 0xa000      ; t0 = 0xb000a000  (MMIO control word base;
                                    ;   c.f. 0xb000a0bc HW-reg port in
                                    ;   rom/reverse_engineering_rom_regs.md)
80000028  lui   t1, 0x3cf8 / ...    ; (literal pool / constant load into t1,
                                    ;   exact constant not fully isolated)
...
80000030  sw    t1, 0(t0)           ; write MMIO control word
80000034  mfc0  k0, Cause           ; read CP0 Cause register
80000038-3c  ...                   ; (branch/test on Cause, not fully traced)
80000040  nop  (gap)
80000044  mfc0  s0, Status          ; read CP0 Status register
80000048-4c  lui/ori building 0x80000125 into a1 (literal)
80000050  ori   s0, s0, 0xff00
80000054  lui   at, 0xffbf
80000058  ori   at, at, 0xffff      ; at = 0xffbfffff
8000005c  and   s0, s0, at          ; s0 = (Status | 0xff00) & 0xffbfffff
                                    ;   -> clears bit 22 (BEV?) and sets IM7..IM0
                                    ;   mask bits; classic CP0 Status reset-time
                                    ;   normalization (clear bootstrap-exception-
                                    ;   vector bit, set interrupt mask)
80000060-64  (literal pool words: 0x800001fc, 0x800001fc)
80000068  mtc0  s0, Status          ; write back normalized CP0 Status
8000006c  nop
80000070  b     0x80000090          ; unconditional branch, skips FUN_80000078
```

**Interpretation**: this is classic MIPS reset-time CP0 setup — read/clear an
MMIO word (likely a watchdog-disable or boot-strap-latch register at
`0xb000af08`), poke a second MMIO control word at `0xb000a000` (in the same
control-register family as the already-documented HW register port
`0xb000a0bc` from `rom/reverse_engineering_rom_regs.md`), then read, mask, and
rewrite CP0 `Status` to clear the boot-exception-vector bit and establish the
interrupt mask the rest of ROM expects. This is **the closest thing this chip
has to a documented "reset vector handler."**

### `FUN_80000078` (size 40 bytes, 32-bit MIPS, unannotated) — entered via the `b 0x80000090` above, NOT sequentially

The branch at `0x80000070` jumps to `0x80000090`, *inside* `FUN_80000078`'s
auto-detected body, skipping its first 24 bytes (`0x80000078`–`0x8000008f`,
which include `addiu s0, sp, 0x0`, `jr ra`-looking bytes, and a literal pool —
i.e. that earlier portion is dead from this boot path and is presumably reached
from elsewhere, e.g. a subroutine call from later code, not from reset). The
boot path itself goes:

```
80000090  lui   ra, 0x8000
80000094  addiu ra, ra, 0xa0         ; ra = 0x800000a0
80000098  jr    ra                  ; jump (not call -- ra is being used as a
                                     ;   scratch jump target, not a real return
                                     ;   address here)
8000009c  nop
```

This is a computed jump to `0x800000a0` — the boot code builds its next target
address in `$ra` and jumps to it (this `lui`/`addiu`/`jr` triplet pattern repeats:
the same pattern at `0x80000010`/`0x80000014`/`0x80000018` jumped from
`0x80000000` to `0x80000020` earlier in the same block). This looks like a
deliberate code-relocation-free "goto absolute address" idiom used throughout
this 32-bit preamble, not real subroutine calls.

### Continuing at `0x800000a0` onward (32-bit MIPS, unannotated, NOT part of any Ghidra-detected function)

```
800000a0  lui   t0, 0x8012           ; t0 = 0x8012xxxx (RAM region --
                                      ;   c.f. "bos" struct base ~0x80120000-
                                      ;   0x8012dc50 documented in
                                      ;   kovah_function_list.md note 4)
800000a4  addiu t0, t0, 0x0          ; (low 16 bits not yet isolated)
800000a8  lui   t1, 0x7fff
800000ac  ori   t1, t1, 0xffff       ; t1 = 0x7fffffff (mask constant)
800000b8(approx) ...
800000be  addiu s1, s0, 0x4
800000c0(approx) sw/branch using s0/s1 (likely a zero-fill or copy loop --
                                      ;   pattern consistent with BSS zeroing
                                      ;   or a small RAM struct init, walking
                                      ;   s0 forward by 4 bytes per iteration)
800000c4  mfc0  ... (a second CP0 read, t0 register family)
```

**This region (`0x800000be` onward) was NOT recognized as a function by
Ghidra's auto-analysis** — `FunctionAt`/`FunctionContaining` both report `NONE`,
and the only symbols Ghidra placed there are degenerate 1-byte "functions"
(`FUN_800000be`, `FUN_800000bf`, `FUN_800000c0`, `FUN_800000c1`) which are
analysis artifacts (mis-detected code/data boundary guesses), not real function
boundaries. The actual instruction stream continues correctly when read
manually (confirmed via raw bytes + Ghidra's per-address instruction decode),
it's just that nothing forced Ghidra to disassemble it as one connected
function. **This is exactly the class of problem `DM_RealtekMIPS16eForceDisassembly.py`
solves for MIPS16e regions** (per its description in `GHIDRA_SCRIPTS.md`) —
but that script is hardcoded for MIPS16e and a START/END range; it was not run
over this 32-bit preamble region. A natural follow-up (not done in this turn)
would be adapting that approach (or simply `disassemble`-ing a fixed range) to
fully recover this block as defined code.

**Working hypothesis** (medium confidence): `0x800000a0`–`~0x800000c8` performs
early RAM/struct initialization — building a pointer into the `0x8012xxxx` RAM
region (the same region documented as containing the 12-entry "bos" connection-
state struct array at `0x8012dc50` per `kovah_function_list.md` note 4) and a
loop incrementing by 4 bytes at a time. This is consistent with either a BSS-zero
loop or a fixed-size RAM struct pre-init, but the loop bounds and exact store
target were not isolated in this pass — **marked UNRESOLVED**, see Open
Questions below.

---

## Confirmed: transition to MIPS16e

**Correction (2026-06-21, via `reverse_engineering_interrupt_vectors.md`)**:
this section originally claimed `disable_interrupts` and `enable_interrupts`
were MIPS16e. That was wrong — direct disassembly shows both are **32-bit
MIPS**, decoding as `mfc0`/`mtc0` (COP0 Status access), which MIPS16e cannot
encode at all. They form a small cluster of 32-bit-MIPS CP0-Status helpers at
`0x80009000`-`0x800090fe` (alongside unnamed siblings `FUN_80009014` and
`FUN_800090b8`), immediately *before* the interrupt trampoline at `0x80009160`.
See that doc for the full disassembly and the `jalx`/`jalr`-based ISA-mode-switch
mechanism these helpers participate in.

`lots_of_initialization` (`0x8000fb5c`) and `calls_to_0x8010a001_as_fptr_to_install_patches`
(`0x800109ac`) genuinely **are MIPS16e** functions (confirmed: their first
instructions disassemble as MIPS16e compressed forms — e.g. `0x800109ac`
decodes as `addiu sp,-0x58`, a MIPS16e-style stack adjustment, and `0x8000fb5c`
likewise).

**Update (2026-06-21, via `reverse_engineering_interrupt_vectors.md`)**: a
concrete `jalx`-class mode switch was found at `0x800109ac`
(`calls_to_0x8010a001_as_fptr_to_install_patches`) — a `jalx 0x80009014` call
into the 32-bit-MIPS CP0-Status helper cluster described above, returning to
MIPS16e via the standard link-register-LSB convention. That confirms the
mechanism (`jalx`/`jr` round-trips between ISA modes) but is a single call site
found during interrupt-trampoline work, not a sweep of the boot path itself —
the *original* power-on 32-bit-MIPS→MIPS16e transition (as opposed to this
later runtime mode-switch-for-a-CP0-access pattern) still has not been located.
**The gap `~0x800000c8`–`0x80009104` (roughly 36 KB of ROM) remains unwalked**
and is the main open territory for a future continuation; it's reasonably
likely the genuine "lots of initialization" work (BSS zeroing if any, watchdog
config, clock/PLL bring-up if visible, and the actual power-on jalx) lives
somewhere in this gap, possibly including or near `lots_of_initialization`
itself (`0x8000fb5c`).

---

## Confirmed: how control reaches the patch entry (`0x8010a001`)

Per `kovah_function_list.md` (pre-existing Kovah annotation, re-confirmed in
this pass via direct disassembly):

- **`0x800109ac`** — `calls_to_0x8010a001_as_fptr_to_install_patches` — is a
  ROM (MIPS16e) function, **916 bytes**, `USER_DEFINED` (Kovah-named). Its
  first instruction is `addiu sp, -0x58` (MIPS16e stack-frame setup), i.e. a
  normal function prologue — this runs as part of ROM's regular runtime flow,
  **not as reset-time boot code**. It is the function that, somewhere in its
  916-byte body, calls through a function pointer to `0x8010a001` (the patch
  entry point, documented in `firmware/reverse_engineering_patch_entry.md`).
- This matches CLAUDE.md's existing high-level description: "ROM bootstrap
  that jumps into patch." The patch is downloaded into RAM at `0x80100000`
  by the host (via `btrtl`'s HCI VSC `0xFC20` load), and at some point during
  normal ROM operation (almost certainly **download-triggered**, not boot-time
  — the chip must already be running and able to receive USB/HCI traffic
  before a patch blob can even arrive), ROM calls this 916-byte function, which
  in turn calls the freshly-loaded patch entry at `0x8010a001`.
- **This pass did not fully trace the internal logic of the 916-byte
  `calls_to_0x8010a001_as_fptr_to_install_patches` body** (e.g. exactly what
  triggers it — HCI VSC `0xFC20` completion handler, a post-download check, or
  something else). That would be a natural next step but is **out of scope**
  for "boot/reset sequence" proper, since by the time this function runs the
  chip has already completed reset, enumerated over USB, and is processing HCI
  commands — it is part of the *patch-load* path, not the *power-on* path.
  (Cross-reference: this overlaps with the not-yet-done TODO "Document USB/UART
  transport framing and the ROM-side HCI driver" and "Document the full HCI
  command parser/router" — the trigger for this function likely lives in
  whichever ROM code handles VSC `0xFC20`.)

**Conclusion: the boot/reset path and the patch-entry-call path are two
different things that happen at very different times.** Reset brings the chip
from power-on to a running Bluetooth-stack-ready state entirely within ROM
(32-bit MIPS preamble → MIPS16e ROM runtime, somewhere in the unexplored
`0x800000c8`–`0x80009104` gap). The patch-entry call (`calls_to_0x8010a001_...`
at `0x800109ac`) is normal *post-boot* ROM code that runs once per patch
download, triggered by host activity over USB, long after reset has finished.

---

## Summary table

| Address | What | Confidence |
|---------|------|------------|
| `0x80000000` | First instruction in ROM = de facto reset vector (no separate vector table found below it) | Confirmed |
| `0x80000000`–`0x8000006f` | 32-bit MIPS preamble: MMIO pokes @ `0xb000af08`/`0xb000a000`, CP0 Status read/mask/write | Confirmed (disasm) |
| `0x80000070` | `b 0x80000090` — skips dead code in `FUN_80000078`'s head | Confirmed |
| `0x80000090`–`0x8000009c` | Computed jump (`lui`/`addiu ra`/`jr ra`) to `0x800000a0` | Confirmed |
| `0x800000a0`–`~0x800000c8` | RAM pointer setup into `0x8012xxxx` region + loop incrementing by 4 (BSS-zero or struct-init candidate) + second CP0 read | Partial / hypothesis |
| `~0x800000c8`–`0x80009104` | **Unexplored gap** (~36 KB). MIPS16e mode switch (likely `jalx`) and any watchdog/clock/PLL init presumed to live here | **Unresolved — not walked this pass** |
| `0x80009104` / `0x80009120` | `disable_interrupts` / `enable_interrupts` — first confirmed MIPS16e, Kovah-named, ordinary runtime helpers (not boot-specific) | Confirmed (pre-existing) |
| `0x8000fb5c` | `lots_of_initialization` — MIPS16e, Kovah-named, 436 bytes, **not yet decompiled in this pass** | Located, not analyzed |
| `0x800109ac` | `calls_to_0x8010a001_as_fptr_to_install_patches` — calls patch entry; runs at *patch-download time*, not boot time | Confirmed (pre-existing Kovah annotation, re-verified) |
| `0x8010a001` | Patch entry point (see `firmware/reverse_engineering_patch_entry.md`) | Confirmed (pre-existing) |

---

## Open questions / future work

1. **`~0x800000c8`–`0x80009104` gap (~36 KB) not walked.** This almost
   certainly contains: the actual MIPS32→MIPS16e mode switch (likely via
   `jalx`), and any watchdog-disable / clock-PLL bring-up code not already
   captured in the `0x80000000`–`0x800000c8` preamble. Highest-value next step
   for continuing this ticket.
2. **`lots_of_initialization` (`0x8000fb5c`, 436 bytes) not decompiled.** Its
   name (Kovah's own annotation) strongly suggests it's a major piece of the
   init story but this pass only confirmed its entry instruction and size.
3. **Exact BSS-zero / RAM-struct-init loop at `0x800000a0`–`0x800000c8` not
   fully resolved** — loop bounds, store target, and whether it's truly BSS
   zeroing vs. something narrower (e.g. one struct) are unconfirmed.
4. **Trigger condition for `calls_to_0x8010a001_as_fptr_to_install_patches`**
   (`0x800109ac`) not traced — presumably tied to HCI VSC `0xFC20` completion,
   but this overlaps with the still-open "HCI command parser/router" and
   "USB/UART transport framing" TODOs and was treated as out-of-scope here.
5. No evidence of a **separate boot ROM / vector table distinct from
   `0x80000000`** was found — the GZF's `rom` block starts exactly at
   `0x80000000` and that is where the first (and only) low-address code is.
   If the real silicon has an earlier mask-ROM stage not captured in this dump
   (e.g. a tiny first-stage loader before `0x80000000`), it is invisible to
   this analysis; everything here is built on what the GZF actually contains.

---

## Tool note (wairz limitation encountered, not a blocker)

`mcp__wairz__decompile_function` / `disassemble_function` / `xrefs_to` do not
accept this GZF's `binary_path` directly (`Binary not found` errors) — this is
expected/known (see `CLAUDE.md` and prior docs: GZF multi-block analysis only
works through `run_ghidra_headless` + scripts).

**New finding this pass**: `mcp__wairz__save_ghidra_script` appears to **not
actually update the script content used by `run_ghidra_headless`'s
`script_name` execution path when overwriting an existing filename.** Repro:
saved new content to `FindXrefsTo.java` (confirmed via `read_ghidra_script`
returning the new 5778-byte content), then ran it via `run_ghidra_headless`
twice — both runs executed the **old** 1442-byte single-target xrefs script
(visible from its output: it printed `XREFS_TO_START`/the old hardcoded
`0x801212e4` target, not the new `BOOT_SEQUENCE_PROBE` markers). The on-disk
copy at `/root/wairz/ghidra/scripts/FindXrefsTo.java` (visible from this repo's
shell, last-modified June 7) also still has the old content. This suggests
`save_ghidra_script`'s overwrite path writes to a different backing store
(used by `list_ghidra_research_files`/`read_ghidra_script`) than the one
`run_ghidra_headless`'s `script_name` lookup actually executes from, so
**overwriting an existing research-script filename silently has no effect on
subsequent runs**. Worked around for this task by using the unmodified,
already-working `DiagAddr.java` with `script_args` (one address at a time)
instead of a batch script. Flagging per CLAUDE.md's "wairz modifications"
rule — this should be fixed in wairz (script overwrites should take effect,
or `save_ghidra_script` should error/warn if it can't actually update the
execution path).
