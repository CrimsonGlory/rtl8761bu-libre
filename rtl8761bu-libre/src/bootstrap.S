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
	/* Vendor patch-block FUN_8011011c compact loop; pool immediately @ +0x1c.
	 * LOAD_ADDR loop mismatched at [0x770,0x780) — UB500 bisect 2026-06. */
	.byte	0x00, 0x6d, 0x05, 0x97, 0x00, 0xef, 0x03, 0x63
	.byte	0x00, 0x9c, 0x10, 0x80, 0x00, 0xa0, 0x10, 0x80
	.byte	0x8d, 0xe9, 0x00, 0x80, 0xf9, 0x63, 0x0d, 0x62
	.byte	0x0c, 0xd1, 0x0b, 0xd0
	.align	2
2:
	.word	0x80121100, 0x801102f0
	.word	0x801210f4, 0x801100bc
	.word	0x801205f8, 0x80110724
	.word	0x801205b0, 0x8011021c
	.word	0x801205a0, 0x8010ff08
	.word	0x801205ac, 0x801106bc
	.word	0x8012063c, 0x801105e8
	.word	0x80120644, 0x8010ff28
	.word	0x80120628, 0x801105bc
	.word	0x80120634, 0x8010fed8
	.word	0x80120624, 0x8010ffcc
	.word	0x80120608, 0x80110310
	.word	0x80120648, 0x8011006c
	.word	0x801205d0, 0x80110700
	.word	0x801205d8, 0x8011057c
	.word	0x801205e0, 0x80110044
	.word	0x801205c4, 0x8010fe84
	.word	0x801205fc, 0x80110640
	.word	0x80120600, 0x80110364
	.word	0, 0
	.size	sub_installer_2, . - sub_installer_2

	.globl	sub_installer_3
	.type	sub_installer_3, @function
sub_installer_3:
	addiu	$sp, $sp, -8
	sw	$ra, 4($sp)
	INSTALL_HOOK_ABS 0x80120f08, 0x80110350
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
	INSTALL_HOOK_ABS 0x80120c88, 0x8010e82c
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
