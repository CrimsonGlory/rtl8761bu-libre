/*
 * init.S — patch_entry master installer (FUN_8010a000 equivalent)
 *
 * ROM calls patch_entry @ 0x8010A001 (FC20 loads body to 0x8010A000).
 * patch_entry @ body+0 (no thin stub — ROM calls 0x8010A001 directly).
 *
 */

	.set	mips16
	.include "stub_macros.S"

	.section	.text.entry
	.globl	patch_entry
	.type	patch_entry, @function
patch_entry:
	addiu	$sp, $sp, -8
	sw	$ra, 4($sp)

	/* Uninit DRAM may be non-zero; sub-installers #3/#6 indirect-call these */
	move	$a3, $zero
	LOAD_ADDR $a2, 0x80120f0c
	sw	$a3, 0($a2)
	LOAD_ADDR $a2, 0x80120c80
	sw	$a3, 0($a2)

	/* Phase 0 — config word split from TLV boundary */
	LOAD_ADDR $a2, 0x801115f8
	lw	$a3, 0($a2)
	LOAD_ADDR $a2, 0x80120060
	sh	$a3, 0($a2)
	srl	$a3, 16
	LOAD_ADDR $a2, 0x80120062
	sh	$a3, 0($a2)

	CALL_PATCH fn_bss_init

	/* Phase 1 — hooks #1–4 (T1 bodies vendor-injected @ native offset) */
	INSTALL_HOOK_ABS 0x80121318, 0x8010b118
	INSTALL_HOOK_ABS 0x80120844, 0x8010b174
	INSTALL_HOOK_ABS 0x80120784, 0x8010bba4
	INSTALL_HOOK_ABS 0x801286c0, 0x8010be20

	CALL_PATCH sub_installer_1
	CALL_PATCH sub_installer_2

	/* hooks #5–17 */
	INSTALL_HOOK_ABS 0x801206cc, 0x8010c1e8
	INSTALL_HOOK_ABS 0x801206d0, 0x8010c224
	INSTALL_HOOK_ABS 0x8012088c, 0x8010b3d8
	INSTALL_HOOK_ABS 0x80121368, 0x8010b0a4
	INSTALL_HOOK_ABS 0x8012136c, 0x8010c198
	INSTALL_HOOK_ABS 0x80121360, 0x8010d1f4
	INSTALL_HOOK_ABS 0x80121344, 0x8010c780
	INSTALL_HOOK_ABS 0x80125550, 0x8010c63c
	INSTALL_HOOK_ABS 0x8012084c, 0x8010b7f0
	INSTALL_HOOK_ABS 0x80120c9c, 0x8010e27c
	INSTALL_HOOK_ABS 0x80120de8, 0x8010dd1c
	INSTALL_HOOK_ABS 0x80120f10, 0x8010d890
	INSTALL_HOOK_ABS 0x80120dbc, 0x8010d618

	/* Phase 2 — sub-installer #3 + hooks #18–34 */
	CALL_PATCH sub_installer_3

	INSTALL_HOOK_ABS 0x80120f3c, 0x8010a594
	INSTALL_HOOK_ABS 0x80120cf8, 0x8010c0f4
	INSTALL_HOOK_ABS 0x80121414, 0x8010a4ac
	INSTALL_HOOK_ABS 0x801213dc, 0x8010a49c

	LOAD_ADDR $a2, 0x80120198
	move	$a3, $zero
	sb	$a3, 0($a2)

	INSTALL_HOOK_ABS 0x80121348, 0x8010bce0
	INSTALL_HOOK_ABS 0x80120590, 0x8010c49c
	INSTALL_HOOK_ABS 0x8012067c, 0x8010c43c
	INSTALL_HOOK_ABS 0x80120f4c, 0x8010d168
	INSTALL_HOOK_ABS 0x801213e8, 0x8010fa34
	INSTALL_HOOK_ABS 0x801213c8, 0x8010f950
	INSTALL_HOOK_ABS 0x80121458, 0x8010fb08
	INSTALL_HOOK_ABS 0x80121410, 0x8010abd0
	INSTALL_HOOK_ABS 0x801206fc, 0x8010f884
	INSTALL_HOOK_ABS 0x80120a0c, 0x8010f85c
	INSTALL_HOOK_ABS 0x80120cd4, 0x8010a550
	INSTALL_HOOK_ABS 0x80120cf4, 0x8010c160
	INSTALL_HOOK_ABS 0x80120824, 0x8010c178

	CALL_PATCH sub_installer_4
	CALL_PATCH sub_installer_5
	CALL_PATCH sub_installer_6

	/* hooks #35–38 */
	INSTALL_HOOK_ABS 0x801206dc, 0x8010c088
	INSTALL_HOOK 0x80120990, fn_b4d0
	INSTALL_HOOK_ABS 0x80121370, 0x8010a5ac
	INSTALL_HOOK 0x80120bfc, fn_c854

	/* Phase 3 — ROM register script + silicon rev + hooks #39–40 */
	LOAD_ADDR $a0, 0x80120264
	li	$a1, 2
	LOAD_ADDR $a2, 0x801205b4
	lw	$a3, 0($a2)
	jalr	$a3
	nop

	CALL_PATCH fn_version_check

	INSTALL_HOOK 0x80121334, fn_c09c
	INSTALL_HOOK_ABS 0x801206c8, 0x8010bc74

	CALL_PATCH fn_tlv_applier
	CALL_PATCH fn_bdaddr_sync

	LOAD_ADDR $a2, 0x8012b814
	lbu	$a3, 1($a2)
	move	$v1, $a3
	li	$a3, 0x1f
	and	$a3, $v1
	li	$v1, 8
	beq	$a3, $v1, skip_bb_init
	li	$v1, 6
	beq	$a3, $v1, skip_bb_init
	li	$v1, 5
	beq	$a3, $v1, skip_bb_init
	lbu	$a3, 2($a2)
	beqz	$a3, skip_bb_init
	CALL_PATCH fn_hw_probe
	li	$a2, 1
	bne	$v0, $a2, skip_bb_init
	nop
	CALL_PATCH fn_bb_init
skip_bb_init:
	CALL_PATCH patch_entry_tail

	lw	$a0, 4($sp)
	addiu	$sp, $sp, 8
	move	$ra, $a0
	jr	$ra
	nop

	.size	patch_entry, . - patch_entry
