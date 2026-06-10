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
 *   [0x0E4C, 0x100C) fn_10ddc eSCO packet-type selector (t4_hooks.S; relocated)
 *   [0x100C, 0x10A4) fn_10ddc_gap vendor bridge (t4_hooks.S)
 *   [0x1174, …)     fn_b174 sec_base+0x14 hook (t2_hooks.S)
 *   [0x10A4, …)     fn_b0a4 ACL packet-type flag corrector (t1_hooks.S)
 *   [0x1118, …)     fn_b118 slot interval allocator (t1_hooks.S)
 *   [0x13D8, …)     fn_b3d8 ACL slot scheduler (t1_hooks.S)
 *   [0x15D8, …)     fn_b5d8 eSCO connection activator (t2_hooks.S)
 *   [0x163C, …)     fn_b5d8_pool_tail literal pool continuation (t2_hooks.S)
 *   [0x164C, …)     fn_b64c LMP eSCO link request (t2_hooks.S)
 *   [0x17D4, …)     fn_b7d4 ACL slot lookup wrapper (t2_hooks.S)
 *   [0x17F0, …)     fn_b7f0 LMP eSCO packet processor (t2_hooks.S)
 *   [0x1CE0, …)     fn_bce0 eSCO slot capability mask (t2_hooks.S)
 *   [0x1DA0, …)     fn_bda0 SCO/eSCO acceptance validator (t2_hooks.S)
 *   [0x1E20, …)     fn_be20 global dispatch (t1_hooks.S)
 *   [0x0BD0, …)     fn_abd0 eSCO audio-path dispatcher (t2_hooks.S, overlays tail)
 *   [0x209C, …)     fn_c09c per-link BB reg 0xf gate (t2_hooks.S)
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
 *   [0x2A20, 0x2C36) fn_ca20 type-0x67 coexistence monitor (protocol_dispatch.S)
 *   [0x2C36, 0x2CB8) fn_ca20_gap vendor pool bridge (protocol_dispatch.S)
 *   [0x2CB8, …)     fn_ccb8 AFH HW reg configurator (t3_hooks.S; ce0c callee)
 *   [0x2DC0, …)     fn_ce0c_pre_gap vendor pool bridge (t3_hooks.S)
 *   [0x2E0C, …)     fn_ce0c AFH capability mapper (t3_hooks.S)
 *   [0x30E4, …)     fn_ce0c_gap vendor literal-pool tail (t3_hooks.S)
 *   [0x3168, …)     fn_d168 AFH HW register init (t3_hooks.S)
 *   [0x39F4, …)     fn_d9f4 LC RX SHIM-T1 (protocol_dispatch.S)
 *   [0x3A70, …)     fn_da70 LC TX SHIM-T1 (protocol_dispatch.S)
 *   [0x3AA4, …)     fn_daa4 HCI event intercept (protocol_dispatch.S)
 *   [0x3D80, …)     fn_dd1c_gap vendor pools + dd1c tail (fn_dd1c_gap.S)
 *   [0x3FB0, …)     fn_dfb0 LMP intercept (protocol_dispatch.S)
 *   [0x41C2, …)     fn_e350_pre_gap vendor pool bridge (t3_hooks.S)
 *   [0x4350, …)     fn_e350 AFH quality ranking engine (t3_hooks.S)
 *   [0x47E6, …)     fn_e350_post_gap vendor region (t3_hooks.S)
 *   [0x585C, …)     fn_f85c AFH readiness gate (t3_hooks.S)
 *   [0x5884, …)     fn_f884 bos+0x50 connection handler (t2_hooks.S)
 *   [0x5950, …)     fn_f950 AFH quality + VSC FC95 trigger (t3_hooks.S)
 *   [0x59FE, …)     fn_f950_gap vendor pool tail (t3_hooks.S)
 *   [0x5A34, …)     fn_fa34 AFH 79-ch map merger (t3_hooks.S)
 *   [0x5AEC, …)     fn_fa34_gap vendor pool tail (t3_hooks.S)
 *   [0x5B08, …)     fn_fb08 BLE 40-ch map aggregator (t3_hooks.S)
 *   [0x5C2C, …)     shims / version_check / lmp_vsc (.text.hooks)
 *   [0x5D60, …)     fn_b4d0 (relocated; sub2 uses native 0x5E84+)
 *   [0x5E84, …)     sub2_fn_00..18 (sub2_hooks.S; sub-inst #2 targets)
 *   [0x5F48, …)     fn_aa58 (relocated into sub2 interstitial gap)
 *   [0x6740, …)     fn_c854 / fn_a84c / fn_10868 (relocated post-sub2)
 *   [0x37C0, …)     fn_d618 literal pool (fn_d618_pool.S)
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
        KEEP(*(.text.fn_10ddc))
        . = __prefix_start + 0x100C;
        KEEP(*(.text.fn_10ddc_gap))
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
        . = __prefix_start + 0x163C;
        KEEP(*(.text.fn_b5d8_pool_tail))
        . = __prefix_start + 0x164C;
        KEEP(*(.text.fn_b64c))
        . = __prefix_start + 0x17D4;
        KEEP(*(.text.fn_b7d4))
        . = __prefix_start + 0x17F0;
        KEEP(*(.text.fn_b7f0))
        . = __prefix_start + 0x1CE0;
        KEEP(*(.text.fn_bce0))
        . = __prefix_start + 0x1DA0;
        KEEP(*(.text.fn_bda0))
        . = __prefix_start + 0x1E20;
        KEEP(*(.text.fn_be20))
        . = __prefix_start + 0x209C;
        KEEP(*(.text.fn_c09c))
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
        . = __prefix_start + 0x27A2;
        KEEP(*(.text.fn_c780_epilogue))
        . = __prefix_start + 0x27A4;
        KEEP(*(.text.fn_c780_pool))
        . = __prefix_start + 0x2A20;
        KEEP(*(.text.fn_ca20))
        . = __prefix_start + 0x2C36;
        KEEP(*(.text.fn_ca20_gap))
        . = __prefix_start + 0x2CB8;
        KEEP(*(.text.fn_ccb8))
        . = __prefix_start + 0x2DC0;
        KEEP(*(.text.fn_ce0c_pre_gap))
        . = __prefix_start + 0x2E0C;
        KEEP(*(.text.fn_ce0c))
        . = __prefix_start + 0x30E4;
        KEEP(*(.text.fn_ce0c_gap))
        . = __prefix_start + 0x3168;
        KEEP(*(.text.fn_d168))
        . = __prefix_start + 0x31F4;
        KEEP(*(.text.fn_d1f4))
        . = __prefix_start + 0x3618;
        KEEP(*(.text.fn_d618))
        . = __prefix_start + 0x37C0;
        KEEP(*(.text.fn_d618_pool))
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
        . = __prefix_start + 0x3D80;
        KEEP(*(.text.fn_dd1c_gap))
        . = __prefix_start + 0x3FB0;
        KEEP(*(.text.fn_dfb0))
        . = __prefix_start + 0x41C2;
        KEEP(*(.text.fn_e350_pre_gap))
        . = __prefix_start + 0x4350;
        KEEP(*(.text.fn_e350))
        . = __prefix_start + 0x47E6;
        KEEP(*(.text.fn_e350_post_gap))
        . = __prefix_start + 0x585C;
        KEEP(*(.text.fn_f85c))
        . = __prefix_start + 0x5884;
        KEEP(*(.text.fn_f884))
        . = __prefix_start + 0x5950;
        KEEP(*(.text.fn_f950))
        . = __prefix_start + 0x59FE;
        KEEP(*(.text.fn_f950_gap))
        . = __prefix_start + 0x5A34;
        KEEP(*(.text.fn_fa34))
        . = __prefix_start + 0x5AEC;
        KEEP(*(.text.fn_fa34_gap))
        . = __prefix_start + 0x5B08;
        KEEP(*(.text.fn_fb08))

        *(.text.hooks)
        . = __prefix_start + 0x5D60;
        KEEP(*(.text.fn_b4d0))

        /* sub-installer #2 targets — vendor-fixed @ 0x8010FE84–0x80110740 */
        . = __prefix_start + 0x5E84;
        KEEP(*(.text.sub2_fn_16))
        . = __prefix_start + 0x5ED8;
        KEEP(*(.text.sub2_fn_09))
        . = __prefix_start + 0x5F08;
        KEEP(*(.text.sub2_fn_04))
        . = __prefix_start + 0x5F28;
        KEEP(*(.text.sub2_fn_07))
        . = __prefix_start + 0x5F48;
        KEEP(*(.text.fn_aa58))
        . = __prefix_start + 0x5FCC;
        KEEP(*(.text.sub2_fn_10))
        . = __prefix_start + 0x6044;
        KEEP(*(.text.sub2_fn_15))
        . = __prefix_start + 0x606C;
        KEEP(*(.text.sub2_fn_12))
        . = __prefix_start + 0x60BC;
        KEEP(*(.text.sub2_fn_01))
        . = __prefix_start + 0x621C;
        KEEP(*(.text.sub2_fn_03))
        . = __prefix_start + 0x62F0;
        KEEP(*(.text.sub2_fn_00))
        . = __prefix_start + 0x6310;
        KEEP(*(.text.sub2_fn_11))
        . = __prefix_start + 0x6364;
        KEEP(*(.text.sub2_fn_18))
        . = __prefix_start + 0x657C;
        KEEP(*(.text.sub2_fn_14))
        . = __prefix_start + 0x65BC;
        KEEP(*(.text.sub2_fn_08))
        . = __prefix_start + 0x65E8;
        KEEP(*(.text.sub2_fn_06))
        . = __prefix_start + 0x6640;
        KEEP(*(.text.sub2_fn_17))
        . = __prefix_start + 0x66BC;
        KEEP(*(.text.sub2_fn_05))
        . = __prefix_start + 0x6700;
        KEEP(*(.text.sub2_fn_13))
        . = __prefix_start + 0x6724;
        KEEP(*(.text.sub2_fn_02))
        . = __prefix_start + 0x6740;
        KEEP(*(.text.fn_c854))
        . = __prefix_start + 0x690C;
        KEEP(*(.text.fn_a84c))
        . = __prefix_start + 0x6ACE;
        KEEP(*(.text.fn_10868))

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
