/*
 * rtl8761bu.ld — linker script for RTL8761BU libre firmware
 *
 * The ROM bootloader calls patch_entry() at PRAM+0x0000 = 0x8010A000
 * via JALX (cross-ISA jump to MIPS16e) after the firmware download.
 *
 * OUTPUT_FORMAT and OUTPUT_ARCH are intentionally omitted: the
 * mipsel-linux-gnu toolchain uses elf32-tradlittlemips internally,
 * not elf32-littlemips (the bare-metal variant), and specifying the
 * wrong format produces "cannot represent machine" linker errors.
 * We strip to a flat binary with objcopy anyway, so the ELF container
 * format does not matter.
 */

ENTRY(patch_entry)

MEMORY {
    /*
     * Standard PRAM: 27808 bytes at 0x8010A000.
     * The LENGTH matches PATCH_SIZE in the Makefile so the linker
     * catches any overflow before pad.py runs.
     */
    pram (rwx) : ORIGIN = 0x8010A000, LENGTH = 27808
}

SECTIONS {
    /*
     * .text.entry must be the very first thing so that patch_entry()
     * lands at exactly 0x8010A000 — the ROM's hard-coded call target.
     * KEEP() prevents --gc-sections from discarding it.
     */
    .text 0x8010A000 : {
        KEEP(*(.text.entry))   /* patch_entry — must be first */
        *(.text .text.*)
        *(.rodata .rodata.*)
        . = ALIGN(4);
    } > pram

    /*
     * No .data or .bss: the firmware has no mutable globals and the
     * ROM does not zero-initialise PRAM before calling patch_entry().
     * If Phase 2 hooks need global state, add .data/.bss here and
     * initialise them explicitly inside patch_entry().
     */

    /* Discard everything the C runtime injects that we do not need. */
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
        *(.gnu_debuglink)
        *(.gnu_debugdata)
    }
}
