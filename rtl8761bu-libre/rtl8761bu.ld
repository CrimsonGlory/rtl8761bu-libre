/*
 * rtl8761bu.ld — linker script for RTL8761BU libre firmware
 *
 * ROM VSC FC20 resets its write pointer to 0x8010A000 and memcpy's the
 * EPatch body there.  Boot then calls 0x8010A001 — patch body offset 0.
 * patch_entry must therefore be the first bytes of the 27,808-byte image.
 *
 * Connect-critical prefix layout (file offsets, runtime 0x8010A000+):
 *   [0x0000, 0x05D8) patch_entry + literal pools (fn_a49c/a594 in pool)
 *   [0x05D8, 0x0724) helper functions (vendor blob interim, PE-3)
 *   [0x0724, 0x0754) sub_installer_1
 *   [0x0754, 0x0764) fn_bss_init
 *   [0x0764, …)     sub_installer_2 (vendor entry calls here)
 *   [0x0820, 0x0E4C) patch_entry tail
 *   [0x1174, …)     fn_b174 sec_base+0x14 hook (t2_hooks.S)
 *   [0x10A4, …)     fn_b0a4 ACL packet-type flag corrector (t1_hooks.S)
 *   [0x1118, …)     fn_b118 slot interval allocator (t1_hooks.S)
 *   [0x13D8, …)     fn_b3d8 ACL slot scheduler (t1_hooks.S)
 *   [0x15D8, …)     fn_b5d8 (eSCO activator; stub until T2)
 *   [0x17F0, …)     fn_b7f0 LMP eSCO packet processor (t2_hooks.S)
 *   [0x1CE0, …)     fn_bce0 eSCO slot capability mask (t2_hooks.S)
 *   [0x1E20, …)     fn_be20 global dispatch (t1_hooks.S)
 *   [0x0BD0, …)     fn_abd0 eSCO audio-path dispatcher (t2_hooks.S, overlays tail)
 *   [0x20F4, …)     fn_c0f4 eSCO-adjacent capability gate (t2_hooks.S)
 *   [0x2160, …)     fn_c160 eSCO slot indirect-call stub (t2_hooks.S)
 *   [0x2172, …)     fn_c160_gap literal pool tail (t2_hooks.S)
 *   [0x2178, …)     fn_c178 eSCO slot state setter (t2_hooks.S)
 *   [0x2190, …)     fn_c178_gap literal pool (t2_hooks.S)
 *   [0x2198, …)     fn_c198 early-install gate (t1_hooks.S)
 *   [0x21E8, …)     fn_c1e8 bos+0x20 HCI handler (t1_hooks.S)
 *   [0x31F4, …)     fn_d1f4 conn timing matcher (t1_hooks.S)
 *   [0x3618, …)     fn_d618 HCI internal msg dispatcher (t1_hooks.S)
 *   [0x3890, …)     fn_d890 HCI VSC msg handler (t1_hooks.S)
 *   [0x3D1C, …)     fn_dd1c HCI msg type router (t1_hooks.S)
 *   [0x2224, …)     fn_c224 bos+0x24 HCI handler (t1_hooks.S)
 *   [0x263C, …)     fn_c63c ACL retransmission handler (t1_hooks.S)
 *   [0x2780, …)     fn_c780 subsystem init (t1_hooks.S)
 *   [0x243C, …)     fn_c43c infrastructure ptr region (t1_hooks.S)
 *   [0x249C, …)     fn_c49c infrastructure ptr region (t1_hooks.S)
 *   [0x2A20, …)     fn_ca20 type-0x67 monitor STUB-T1 (protocol_dispatch.S)
 *   [0x39F4, …)     fn_d9f4 LC RX SHIM-T1 (protocol_dispatch.S)
 *   [0x3A70, …)     fn_da70 LC TX SHIM-T1 (protocol_dispatch.S)
 *   [0x3AA4, …)     fn_daa4 HCI event intercept (protocol_dispatch.S)
 *   [0x3FB0, …)     fn_dfb0 LMP intercept (protocol_dispatch.S)
 *   [0x5884, …)     fn_f884 bos+0x50 connection handler (t2_hooks.S)
 *   (.text.hooks)   fn_b4d0 / fn_c854 — past FC20 window (t2_hooks.S)
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
        . = __prefix_start + 0xBD0;
        KEEP(*(.text.fn_abd0))
        . = __prefix_start + 0xD0C;
        *(.text.installer_tail_cont)
        . = __prefix_start + 0xE4C;
        __prefix_end = .;

        /* Vendor-fixed offsets (fn_e27c literal pool → 0x8010xxxx) */
        . = __prefix_start + 0x10A4;
        KEEP(*(.text.fn_b0a4))
        . = __prefix_start + 0x1118;
        KEEP(*(.text.fn_b118))
        . = __prefix_start + 0x1174;
        KEEP(*(.text.fn_b174))
        . = __prefix_start + 0x13D8;
        KEEP(*(.text.fn_b3d8))
        . = __prefix_start + 0x15D8;
        KEEP(*(.text.fn_b5d8))
        . = __prefix_start + 0x17F0;
        KEEP(*(.text.fn_b7f0))
        . = __prefix_start + 0x1CE0;
        KEEP(*(.text.fn_bce0))
        . = __prefix_start + 0x1E20;
        KEEP(*(.text.fn_be20))
        . = __prefix_start + 0x20F4;
        KEEP(*(.text.fn_c0f4))
        . = __prefix_start + 0x2160;
        KEEP(*(.text.fn_c160))
        . = __prefix_start + 0x2172;
        KEEP(*(.text.fn_c160_gap))
        . = __prefix_start + 0x2178;
        KEEP(*(.text.fn_c178))
        . = __prefix_start + 0x2190;
        KEEP(*(.text.fn_c178_gap))
        . = __prefix_start + 0x2198;
        KEEP(*(.text.fn_c198))
        . = __prefix_start + 0x21E8;
        KEEP(*(.text.fn_c1e8))
        . = __prefix_start + 0x2224;
        KEEP(*(.text.fn_c224))
        . = __prefix_start + 0x243C;
        KEEP(*(.text.fn_c43c))
        . = __prefix_start + 0x249C;
        KEEP(*(.text.fn_c49c))
        . = __prefix_start + 0x263C;
        KEEP(*(.text.fn_c63c))
        . = __prefix_start + 0x2780;
        KEEP(*(.text.fn_c780))
        . = __prefix_start + 0x2A20;
        KEEP(*(.text.fn_ca20))
        . = __prefix_start + 0x31F4;
        KEEP(*(.text.fn_d1f4))
        . = __prefix_start + 0x3618;
        KEEP(*(.text.fn_d618))
        . = __prefix_start + 0x3890;
        KEEP(*(.text.fn_d890))
        . = __prefix_start + 0x39F4;
        KEEP(*(.text.fn_d9f4))
        . = __prefix_start + 0x3A70;
        KEEP(*(.text.fn_da70))
        . = __prefix_start + 0x3AA4;
        KEEP(*(.text.fn_daa4))
        . = __prefix_start + 0x3D1C;
        KEEP(*(.text.fn_dd1c))
        . = __prefix_start + 0x3FB0;
        KEEP(*(.text.fn_dfb0))
        . = __prefix_start + 0x5884;
        KEEP(*(.text.fn_f884))

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
