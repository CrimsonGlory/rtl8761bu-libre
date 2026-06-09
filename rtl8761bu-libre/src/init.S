/*
 * init.S — patch_entry master installer (FUN_8010a000 equivalent)
 *
 * Boot sequence follows Appendix D in reverse_engineering_patch_installer.md:
 *   Phase 0 — config copy, BSS init, config bit clears
 *   Phase 1 — hooks #1–17 + sub-installers #1–2
 *   Phase 2 — sub-installer #3, hooks #18–38, sub-installers #4–6
 *   Phase 3 — ROM reg script, silicon rev, hooks #39–40, TLV, BD_ADDR
 *   Phase 4 — HW probe/BB init, hook #41, RF init, patch-active, hooks #42–44
 *
 * SPDX-License-Identifier: GPL-2.0-or-later
 */

	.set	mips16
	.include "stub_macros.S"

	.section	.text.entry
	.globl	patch_entry
	.type	patch_entry, @function
patch_entry:
	addiu	$sp, $sp, -8
	sw	$ra, 4($sp)

	/* Phase 0 — config word split from TLV boundary */
	LOAD_ADDR $a2, 0x801115f8
	lw	$a3, 0($a2)
	LOAD_ADDR $a2, 0x80120060
	sh	$a3, 0($a2)
	srl	$a3, 16
	LOAD_ADDR $a2, 0x80120062
	sh	$a3, 0($a2)

	CALL_PATCH fn_bss_init

	/* Phase 1 — hooks #1–4 */
	INSTALL_HOOK 0x80121318, fn_b118
	INSTALL_HOOK 0x80120844, fn_b174
	INSTALL_HOOK 0x80120784, fn_bba4
	INSTALL_HOOK 0x801286c0, fn_be20

	CALL_PATCH sub_installer_1
	CALL_PATCH sub_installer_2

	/* hooks #5–17 */
	INSTALL_HOOK 0x801206cc, fn_c1e8
	INSTALL_HOOK 0x801206d0, fn_c224
	INSTALL_HOOK 0x8012088c, fn_b3d8
	INSTALL_HOOK 0x80121368, fn_b0a4
	INSTALL_HOOK 0x8012136c, fn_c198
	INSTALL_HOOK 0x80121360, fn_d1f4
	INSTALL_HOOK 0x80121344, fn_c780
	INSTALL_HOOK 0x80125550, fn_c63c
	INSTALL_HOOK 0x8012084c, fn_b7f0
	INSTALL_HOOK 0x80120c9c, fn_e27c
	INSTALL_HOOK 0x80120de8, fn_dd1c
	INSTALL_HOOK 0x80120f10, fn_d890
	INSTALL_HOOK 0x80120dbc, fn_d618

	/* Phase 2 — sub-installer #3 + hooks #18–34 */
	CALL_PATCH sub_installer_3

	INSTALL_HOOK 0x80120f3c, fn_a594
	INSTALL_HOOK 0x80120cf8, fn_c0f4
	INSTALL_HOOK 0x80121414, fn_a4ac
	INSTALL_HOOK 0x801213dc, fn_a49c

	LOAD_ADDR $a2, 0x80120198
	move	$a3, $zero
	sb	$a3, 0($a2)

	INSTALL_HOOK 0x80121348, fn_bce0
	INSTALL_HOOK 0x80120590, fn_c49c
	INSTALL_HOOK 0x8012067c, fn_c43c
	INSTALL_HOOK 0x80120f4c, fn_d168
	INSTALL_HOOK 0x801213e8, fn_fa34
	INSTALL_HOOK 0x801213c8, fn_f950
	INSTALL_HOOK 0x80121458, fn_fb08
	INSTALL_HOOK 0x80121410, fn_abd0
	INSTALL_HOOK 0x801206fc, fn_f884
	INSTALL_HOOK 0x80120a0c, fn_f85c
	INSTALL_HOOK 0x80120cd4, fn_a550
	INSTALL_HOOK 0x80120cf4, fn_c160
	INSTALL_HOOK 0x80120824, fn_c178

	CALL_PATCH sub_installer_4
	CALL_PATCH sub_installer_5
	CALL_PATCH sub_installer_6

	/* hooks #35–38 */
	INSTALL_HOOK 0x801206dc, fn_c088
	INSTALL_HOOK 0x80120990, fn_b4d0
	INSTALL_HOOK 0x80121370, fn_a5ac
	INSTALL_HOOK 0x80120bfc, fn_c854

	/* Phase 3 — ROM register script + silicon rev + hooks #39–40 */
	LOAD_ADDR $a0, 0x80120264
	li	$a1, 2
	LOAD_ADDR $a2, 0x801205b4
	lw	$a3, 0($a2)
	jalr	$a3
	nop

	CALL_PATCH fn_silicon_rev

	INSTALL_HOOK 0x80121334, fn_c09c
	INSTALL_HOOK 0x801206c8, fn_bc74

	CALL_PATCH fn_tlv_applier
	CALL_ROM 0x8000fd39

	CALL_PATCH fn_hw_probe
	li	$a2, 1
	bne	$v0, $a2, skip_bb_init
	nop
	CALL_PATCH fn_bb_init
skip_bb_init:
	INSTALL_HOOK 0x80120cdc, fn_ce0c

	CALL_PATCH fn_rf_init

	LOAD_ADDR $a2, 0x80120538
	li	$v0, 4
	sw	$v0, 0($a2)

	/* hooks #42–44 */
	INSTALL_HOOK 0x80121020, fn_10ddc
	INSTALL_HOOK 0x80121220, fn_bda0
	INSTALL_HOOK 0x8012167c, fn_e350

	lw	$a0, 4($sp)
	addiu	$sp, $sp, 8
	move	$ra, $a0
	jr	$ra
	nop

	.size	patch_entry, . - patch_entry
