/*
 * rtl8761bu-libre — free replacement firmware for Realtek RTL8761BU
 * (TP-Link UB500 and equivalent USB Bluetooth 5.0 dongles)
 *
 * Target CPU : MIPS16e (little-endian, MIPS32r2 base)
 * Load addr  : 0x8010A000 (Patch RAM, overlays ROM at same address)
 * Entry point: patch_entry() — called by the chip's ROM bootloader
 *              via JALX (cross-ISA jump) immediately after the EPatch
 *              firmware download completes.
 *
 * Strategy (Phase 1 — minimal deblob):
 *   Run the 25 hardware-initialisation ROM calls found in the original
 *   firmware's init function, then return without installing any DRAM
 *   function-pointer hooks.  The ROM's own default implementations
 *   (guarded by  if (fptr != NULL)  checks) handle everything else.
 *   If basic Bluetooth comes up, this file is the entire replacement.
 *
 * If Phase 1 is insufficient (some ROM default is broken or absent),
 * add per-hook reimplementations as separate .c files and wire them in
 * at the bottom of patch_entry().  See README.md for the hook table.
 *
 * ROM function addresses were extracted from the original firmware by
 * analysing the MIPS16e EXTEND sequences in the init function.  They
 * refer to functions in the chip's *silicon ROM* — not to anything in
 * the loaded firmware blob — so calling them is acceptable for a free
 * firmware replacement.  Addresses marked UNVERIFIED need confirmation
 * via MIPS16e disassembly in Ghidra before trusting them.
 *
 * SPDX-License-Identifier: GPL-2.0-or-later
 */

#include <stdint.h>

/* ── ROM function type ─────────────────────────────────────────────── */

/*
 * ROM functions are MIPS32 (even address, no interwork bit).
 * Calling them from MIPS16e code uses JALR with LSB=0 in the target,
 * which tells the hardware to execute the callee in MIPS32 mode.
 * $ra is set with LSB=1 by the MIPS16e JALR, so the callee returns
 * to MIPS16e mode automatically via JR $ra.
 * GCC -mips16 handles all of this transparently.
 */
typedef void (*rom_fn_t)(void);

#define ROM(addr)   ((rom_fn_t)(addr))


/* ── ROM hardware-initialisation functions ──────────────────────────── */
/*
 * These are called in the same order as the original firmware's init
 * function.  Groups match the call clusters visible in the disassembly.
 */

/* Group 1 — eight closely-spaced helpers (0x80109F74 … 0x80109FB8)
 * Likely: clock/PLL setup, reset-control, power-domain sequencing.
 * Spacing of 6–12 bytes suggests very short leaf functions.         */
static const rom_fn_t hw_group1[] = {
    ROM(0x80109F74),
    ROM(0x80109F80),
    ROM(0x80109F9A),
    ROM(0x80109FA0),
    ROM(0x80109FA6),
    ROM(0x80109FAC),
    ROM(0x80109FB2),
    ROM(0x80109FB8),
};

/* Group 2 — BT core initialisation (two calls)                       */
static const rom_fn_t hw_group2[] = {
    ROM(0x805CA0D6),   /* UNVERIFIED — confirm via Ghidra MIPS16e */
    ROM(0x805CA118),   /* UNVERIFIED */
};

/* Group 3 — BT subsystem initialisation (two calls)                  */
static const rom_fn_t hw_group3[] = {
    ROM(0x805EA15A),   /* UNVERIFIED */
    ROM(0x805EA19C),   /* UNVERIFIED */
};

/* Group 4 — single call (purpose unknown)                            */
static const rom_fn_t hw_group4[] = {
    ROM(0x809CA36A),   /* UNVERIFIED */
};

/* Group 5 — four closely-spaced calls (0x80A4A6D6 … 0x80A4A700)
 * Likely: RF / baseband configuration registers.                     */
static const rom_fn_t hw_group5[] = {
    ROM(0x80A4A6D6),   /* UNVERIFIED */
    ROM(0x80A4A6E4),   /* UNVERIFIED */
    ROM(0x80A4A6F2),   /* UNVERIFIED */
    ROM(0x80A4A700),   /* UNVERIFIED */
};

/* Group 6 — peripheral init A (four calls at 0x80DCA1xx)
 * Likely: USB or UART hardware block setup.                          */
static const rom_fn_t hw_group6[] = {
    ROM(0x80DCA100),   /* UNVERIFIED */
    ROM(0x80DCA118),   /* UNVERIFIED */
    ROM(0x80DCA146),   /* UNVERIFIED */
    ROM(0x80DCA152),   /* UNVERIFIED */
};

/* Group 7 — peripheral init B (four calls at 0x80DEA1xx)
 * Likely: companion hardware block (DMA, interrupt controller, ...). */
static const rom_fn_t hw_group7[] = {
    ROM(0x80DEA182),   /* UNVERIFIED */
    ROM(0x80DEA19A),   /* UNVERIFIED */
    ROM(0x80DEA1C8),   /* UNVERIFIED */
    ROM(0x80DEA1D4),   /* UNVERIFIED */
};

/*
 * NOTE — secondary ROM bank (0x7FB0xxxx):
 * The original init function also makes four calls into an address
 * range below 0x80000000 (0x7FB0A4C2, 0x7FB0A4DC, 0x7FB0A572,
 * 0x7FB0A58E).  These are within the BL ±16 MB range from PRAM and
 * are plausibly a secondary ROM bank or a mirror of main ROM.
 * They are omitted here pending MIPS16e disassembly verification.
 * Add them in the SECONDARY_ROM_CALLS section below once confirmed.
 */


/* ── DRAM hook-slot addresses ──────────────────────────────────────── */
/*
 * The ROM guards 21 call sites with  if (fptr != NULL) { fptr(...) }.
 * The original firmware fills these DRAM slots with patch function
 * pointers.  We leave them all zero (NULL) so the ROM uses its own
 * built-in defaults.
 *
 * If a specific feature is broken, implement its function below and
 * assign it to the relevant slot.  LSB must be 1 (MIPS16e interwork
 * bit) for any function pointer stored in a DRAM slot.
 *
 * Slot address   Hook#  Original patch offset
 * 0x80121414       23   PRAM+0x04F4   (smallest offset — called first)
 * 0x80120CF8       22   PRAM+0x0504
 * 0x8012136C       12   PRAM+0x0668
 * 0x801286C0        7   PRAM+0x1254
 * 0x8012088C       10   PRAM+0x12DC
 * 0x80120590       26   PRAM+0x299C
 * 0x80121348       25   PRAM+0x29FC
 * 0x80121360       13   PRAM+0x2CF4
 * 0x80121344       14   PRAM+0x2BF0
 * 0x80121368       11   PRAM+0x26F8
 * 0x80120F3C       21   PRAM+0x25F0
 * 0x801213DC       24   PRAM+0x1610
 * 0x80121458       31   PRAM+0x1040
 * 0x80120960       39   PRAM+0x1A00
 * 0x8012067C       27   PRAM+0x3534
 * 0x801206AC        3   PRAM+0x1350
 * 0x80125550       15   PRAM+0x1D20
 * 0x80120F4C       28   PRAM+0x50E0
 * 0x801213E8       29   PRAM+0x4FFC
 * 0x80121410       32   PRAM+0x4F30
 * 0x801213C8       30   PRAM+0x51B4
 */

typedef void (*hook_fn_t)(void);

static inline void write_hook(uint32_t slot_addr, hook_fn_t fn)
{
    /*
     * Write a function pointer (with MIPS16e interwork bit) into a
     * DRAM hook slot.  The pointer must have LSB=1 to tell the ROM's
     * JALR to enter MIPS16e mode.
     *
     * Currently unused — all slots left NULL (ROM defaults).
     * Uncomment and call from patch_entry() as needed:
     *
     *   write_hook(0x80121414, my_hook_fn);
     */
    volatile uint32_t *slot = (volatile uint32_t *)(uintptr_t)slot_addr;
    *slot = (uint32_t)(uintptr_t)fn | 1u;   /* set MIPS16e interwork bit */
}


/* ── Firmware entry point ──────────────────────────────────────────── */
/*
 * The ROM calls this function (via JALX — cross-ISA jump to MIPS16e)
 * at PRAM+0x0000 immediately after the EPatch download completes.
 * We must be placed at exactly 0x8010A000 by the linker.
 *
 * The section attribute guarantees the linker script places this
 * function before everything else regardless of link order.
 */
__attribute__((mips16, section(".text.entry"), used))
void patch_entry(void)
{
    unsigned int i;

    /* ── Hardware initialisation — call ROM functions in order ── */

    for (i = 0; i < sizeof(hw_group1)/sizeof(hw_group1[0]); i++)
        hw_group1[i]();

    for (i = 0; i < sizeof(hw_group2)/sizeof(hw_group2[0]); i++)
        hw_group2[i]();

    for (i = 0; i < sizeof(hw_group3)/sizeof(hw_group3[0]); i++)
        hw_group3[i]();

    for (i = 0; i < sizeof(hw_group4)/sizeof(hw_group4[0]); i++)
        hw_group4[i]();

    for (i = 0; i < sizeof(hw_group5)/sizeof(hw_group5[0]); i++)
        hw_group5[i]();

    for (i = 0; i < sizeof(hw_group6)/sizeof(hw_group6[0]); i++)
        hw_group6[i]();

    for (i = 0; i < sizeof(hw_group7)/sizeof(hw_group7[0]); i++)
        hw_group7[i]();

    /* ── SECONDARY_ROM_CALLS (add once addresses are verified) ── */
    /*
     * ROM(0x7FB0A4C2)();
     * ROM(0x7FB0A4DC)();
     * ROM(0x7FB0A572)();
     * ROM(0x7FB0A58E)();
     */

    /* ── DRAM hook installations (Phase 2, add as needed) ─────── */
    /*
     * write_hook(0x80121414, hook_23);   // first hook called by BT stack
     * write_hook(0x80120CF8, hook_22);
     * ...
     */

    /*
     * Return to the ROM bootloader.  The ROM will then start the BT
     * stack, which calls through any non-NULL DRAM slots.  Since all
     * slots are still NULL, the ROM executes its own default paths.
     */
}
