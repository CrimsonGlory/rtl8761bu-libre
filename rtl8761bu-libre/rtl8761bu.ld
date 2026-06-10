/*
 * rtl8761bu.ld — linker script for RTL8761BU libre firmware
 *
 * ROM VSC FC20 resets its write pointer to 0x8010A000 and memcpy's the
 * EPatch body there.  Boot then calls 0x8010A001 — patch body offset 0.
 * patch_entry must therefore be the first bytes of the 27,808-byte image.
 *
 * Connect-critical prefix layout (file offsets, runtime 0x8010A000+):
 *   [0x0000, 0x05D8) patch_entry + literal pools
 *   [0x05D8, 0x0724) helper functions (vendor blob interim, PE-3)
 *   [0x0724, 0x0754) sub_installer_1
 *   [0x0754, 0x0764) fn_bss_init
 *   [0x0764, …)     sub_installer_2 (vendor entry calls here)
 *   [0x0820, 0x0E4C) patch_entry tail
 *   [0x15D8, …)     fn_b5d8 (eSCO activator; stub until T2)
 *   [0x3FB0, …)     fn_dfb0 LMP intercept (protocol_dispatch.S)
 */

ENTRY(patch_entry)

MEMORY {
    pram (rwx) : ORIGIN = 0x8010A000, LENGTH = 27808
    meta (rw)  : ORIGIN = 0x90000000, LENGTH = 1M
}

SECTIONS {
    .text 0x8010A000 : {
        __prefix_start = .;
        *(.text.entry)
        . = __prefix_start + 0x5D8;
        *(.text.helpers)
        . = __prefix_start + 0x724;
        *(.text.sub_inst1)
        . = __prefix_start + 0x754;
        *(.text.bss_init)
        . = __prefix_start + 0x764;
        *(.text.si2)
        . = __prefix_start + 0x820;
        *(.text.installer_tail)
        . = __prefix_start + 0xE4C;
        __prefix_end = .;

        /* Vendor-fixed offsets (fn_e27c literal pool → 0x8010xxxx) */
        . = __prefix_start + 0x15D8;
        KEEP(*(.text.fn_b5d8))
        . = __prefix_start + 0x3FB0;
        KEEP(*(.text.fn_dfb0))

        *(.text.hooks)
        *(.text)
        *(.rodata .rodata.*)
        . = ALIGN(4);
    } > pram

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
