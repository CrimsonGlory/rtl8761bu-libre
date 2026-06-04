/*
 * rtl8761bu.ld — linker script for RTL8761BU libre firmware
 *
 * The RTL8761BU Patch RAM (PRAM) overlays the chip's ROM at
 * 0x8010A000.  The ROM bootloader copies the EPatch payload here
 * then calls the entry function at 0x8010A000 via JALX.
 *
 * Extended PRAM (0x80110CA0 – 0x80113FFF, ~13 KB) is available for
 * Phase 2 hook implementations if the standard 27808-byte region
 * fills up.  For Phase 1 the code is tiny and this is irrelevant.
 */

OUTPUT_ARCH(mipsel)
OUTPUT_FORMAT("elf32-littlemips")

ENTRY(patch_entry)

MEMORY {
    /*
     * Standard PRAM: 27808 bytes.
     * Keep the LENGTH matching PATCH_SIZE in the Makefile so the
     * linker catches any accidental overflows.
     */
    pram (rwx) : ORIGIN = 0x8010A000, LENGTH = 27808

    /*
     * Extended PRAM: available but unused in Phase 1.
     * Uncomment and extend the sections below if Phase 2 hooks
     * push the binary past 27808 bytes.
     */
    /* epram (rwx) : ORIGIN = 0x80110CA0, LENGTH = 13151 */
}

SECTIONS {
    /*
     * .text.entry must be the very first thing in the binary so that
     * patch_entry() lands at exactly 0x8010A000.
     * The KEEP() prevents --gc-sections from discarding it.
     */
    .text 0x8010A000 : {
        KEEP(*(.text.entry))    /* patch_entry — must be first */
        *(.text .text.*)
        *(.rodata .rodata.*)
        . = ALIGN(4);
    } > pram

    /*
     * No .data or .bss: the firmware has no mutable globals and the
     * ROM does not zero-initialise PRAM before calling patch_entry().
     * If Phase 2 hooks need global state, add small .data/.bss here
     * and initialise them explicitly inside patch_entry().
     */

    /*
     * Discard everything the C runtime and compiler inject that we
     * neither need nor want in a bare-metal MIPS16e binary.
     */
    /DISCARD/ : {
        *(.reginfo)
        *(.MIPS.abiflags)
        *(.MIPS.options)
        *(.gnu.attributes)
        *(.comment)
        *(.eh_frame .eh_frame_hdr)
        *(.note .note.*)
        *(.pdr)
        *(.mdebug .mdebug.*)
    }
}
