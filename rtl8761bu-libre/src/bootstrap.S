/*
 * bootstrap.S — BSS init, sub-installers #1–#6.
 * SPDX-License-Identifier: GPL-2.0-or-later
 */

	.set	mips16
	.include "stub_macros.S"

	.section	.text.hooks

	.globl	fn_bss_init
	.type	fn_bss_init, @function
fn_bss_init:
	/* Vendor FUN_8010a6c8 — PC-relative pool @ +24; do not rewrite with LOAD_ADDR. */
	.byte	0xfd, 0x63, 0x05, 0x62, 0x05, 0xb2, 0x06, 0xb6
	.byte	0x82, 0x67, 0x5b, 0xe6, 0x05, 0xb2, 0x40, 0xea
	.byte	0x00, 0x6d, 0x05, 0x97, 0x00, 0xef, 0x03, 0x63
	.word	0x80109c00
	.word	0x8010a000
	.word	0x8000e98d
	.size	fn_bss_init, . - fn_bss_init

	.globl	sub_installer_1
	.type	sub_installer_1, @function
sub_installer_1:
	/* FUN_8010af40: read BB reg 0x108, clear bit 6, write back */
	addiu	$sp, $sp, -0x18
	sw	$ra, 0x14($sp)
	sw	$s0, 0x10($sp)
	li	$s0, 0xff
	addiu	$s0, $s0, 9		/* reg index 0x108 */
	move	$a0, $s0
	li	$a1, 2
	LOAD_ADDR $a3, 0x8001136d
	jalr	$a3
	nop
	li	$a1, 0x41
	neg	$a1, $a1		/* mask 0xffffffbf — clear bit 6 */
	and	$a1, $v0
	move	$a0, $s0
	li	$a2, 2
	LOAD_ADDR $a3, 0x8001139d
	jalr	$a3
	nop
	lw	$a3, 0x14($sp)
	lw	$s0, 0x10($sp)
	jr	$a3
	addiu	$sp, $sp, 0x18
	.size	sub_installer_1, . - sub_installer_1

	.globl	sub_installer_2
	.type	sub_installer_2, @function
sub_installer_2:
	/* FUN_8011011c: install 19 fn-ptrs into 0x801205b0–0x80121100 */
	LOAD_ADDR $v1, 2f
1:
	lw	$a0, 0($v1)
	beqz	$a0, 3f
	lw	$a3, 4($v1)
	addiu	$a3, $a3, 1
	sw	$a3, 0($a0)
	addiu	$v1, $v1, 8
	b	1b
	nop
3:
	jr	$ra
	nop
	.align	2
2:
	.word	0x80121100, sub2_fn_00
	.word	0x801210f4, sub2_fn_01
	.word	0x801205f8, sub2_fn_02
	.word	0x801205b0, sub2_fn_03
	.word	0x801205a0, sub2_fn_04
	.word	0x801205ac, sub2_fn_05
	.word	0x8012063c, sub2_fn_06
	.word	0x80120644, sub2_fn_07
	.word	0x80120628, sub2_fn_08
	.word	0x80120634, sub2_fn_09
	.word	0x80120624, sub2_fn_10
	.word	0x80120608, sub2_fn_11
	.word	0x80120648, sub2_fn_12
	.word	0x801205d0, sub2_fn_13
	.word	0x801205d8, sub2_fn_14
	.word	0x801205e0, sub2_fn_15
	.word	0x801205c4, sub2_fn_16
	.word	0x801205fc, sub2_fn_17
	.word	0x80120600, sub2_fn_18
	.word	0, 0
	.size	sub_installer_2, . - sub_installer_2

	.globl	sub_installer_3
	.type	sub_installer_3, @function
sub_installer_3:
	addiu	$sp, $sp, -8
	sw	$ra, 4($sp)
	INSTALL_HOOK 0x80120f08, fn_10ca4
	LOAD_ADDR $a2, 0x80120f0c
	lw	$v0, 0($a2)
	beqz	$v0, 1f
	jalr	$v0
	nop
1:
	lw	$a0, 4($sp)
	addiu	$sp, $sp, 8
	move	$ra, $a0
	jr	$ra
	nop
	.size	sub_installer_3, . - sub_installer_3

	.globl	sub_installer_4
	.type	sub_installer_4, @function
sub_installer_4:
	LOAD_ADDR $a2, 0x80120a70
	LOAD_ADDR $a3, 1f
	lw	$v0, 0($a3)
	sw	$v0, 4($a2)
	lw	$v0, 4($a3)
	sw	$v0, 8($a2)
	lw	$v0, 8($a3)
	sw	$v0, 0($a2)
	lw	$v0, 12($a3)
	sw	$v0, 12($a2)
	lw	$v0, 16($a3)
	sw	$v0, 16($a2)
	lw	$v0, 20($a3)
	sw	$v0, 24($a2)
	lw	$v0, 24($a3)
	sw	$v0, 28($a2)
	lw	$v0, 28($a3)
	sw	$v0, 20($a2)
	lw	$v0, 32($a3)
	sw	$v0, 32($a2)
	lw	$v0, 36($a3)
	sw	$v0, 36($a2)
	jr	$ra
	nop
	.align	2
1:
	.word	0x8012382c, 0x80123a20, 0x80120060, 0x80111198
	.word	0x80120070, 0x8012382c, 0x80123a20, 0x80120060
	.word	0x80111198, 0x80120070
	.size	sub_installer_4, . - sub_installer_4

	.globl	sub_installer_5
	.type	sub_installer_5, @function
sub_installer_5:
	INSTALL_HOOK 0x80120c88, fn_e82c
	jr	$ra
	nop
	.size	sub_installer_5, . - sub_installer_5

	.globl	sub_installer_6
	.type	sub_installer_6, @function
sub_installer_6:
	addiu	$sp, $sp, -8
	sw	$ra, 4($sp)
	LOAD_ADDR $a2, 0x8012b800
	lw	$v0, 1f
	sw	$v0, 0($a2)
	b	2f
	nop
1:	.word	0xffffffff
2:
	LOAD_ADDR $a2, 0x80120c80
	lw	$v0, 0($a2)
	beqz	$v0, 1f
	jalr	$v0
	nop
1:
	lw	$a0, 4($sp)
	addiu	$sp, $sp, 8
	move	$ra, $a0
	jr	$ra
	nop
	.size	sub_installer_6, . - sub_installer_6
