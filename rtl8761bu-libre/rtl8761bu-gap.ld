/*
 * rtl8761bu-gap.ld — LEGACY layout (do not use on hardware)
 *
 * Places entry at body+0x6880 assuming load base 0x80103780.  FC20 actually
 * writes to 0x8010A000 and ROM calls body+0 — this layout hangs (FC20 timeout).
 * Kept for bisect only: make LAYOUT=gap
 */

ENTRY(patch_entry)

ENTRY_OFFSET = 0x6880;
ENTRY_ADDR   = 0x80103780 + ENTRY_OFFSET;

MEMORY {
    pram (rwx) : ORIGIN = 0x80103780, LENGTH = 27808
    meta (rw)  : ORIGIN = 0x90000000, LENGTH = 1M
}

SECTIONS {
    /* Pre-entry code — .text.entry is placed separately at ENTRY_ADDR. */
    .text 0x80103780 : {
        *(.text)
        *(.text.hooks)
        *(.rodata .rodata.*)
        . = ALIGN(4);
    } > pram

    .text.entry ENTRY_ADDR : {
        KEEP(*(.text.entry))
        . = ALIGN(4);
    } > pram

    /* ELF metadata — not part of the chip image. */
    .rel.dyn        : { *(.rel.dyn) } > meta
    .rela.dyn       : { *(.rela.dyn) } > meta
    .dynsym         : { *(.dynsym) } > meta
    .dynstr         : { *(.dynstr) } > meta
    .hash           : { *(.hash) } > meta
    .dynamic        : { *(.dynamic) } > meta

    /DISCARD/ : {
        *(.rld_map)
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
        *(.interp .got .plt)
    }
}
