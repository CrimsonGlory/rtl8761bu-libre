/*
 * shims.S — ROM tail-call shims and mandatory small dispatch helpers.
 * SPDX-License-Identifier: GPL-2.0-or-later
 */

	.set	mips16
	.include "stub_macros.S"

	.section	.text.hooks

#ifndef INSTALLER_TAIL_TRANSCRIBED
	.globl	fn_c088
	.type	fn_c088, @function
fn_c088:
	lw	$v0, 1f
	beqz	$v0, 2f
	nop
	jalr	$v0
	nop
2:
	jr	$ra
	nop
1:	.word	0x80120961
	.size	fn_c088, . - fn_c088
#endif /* !INSTALLER_TAIL_TRANSCRIBED */

	.globl	fn_d154
	.type	fn_d154, @function
fn_d154:
	lw	$v0, 1f
	jalr	$v0
	nop
	jr	$ra
	nop
1:	.word	0x80066e69
	.size	fn_d154, . - fn_d154

	.globl	fn_cc94
	.type	fn_cc94, @function
fn_cc94:
	addiu	$sp, $sp, -0x18
	sw	$ra, 0x14($sp)
	sw	$s0, 0x10($sp)
	lw	$v0, 1f
	jalr	$v0
	move	$s0, $a0
	move	$a0, $s0
	lw	$v0, 2f
	jalr	$v0
	nop
	lw	$a0, 0x14($sp)
	lw	$s0, 0x10($sp)
	addiu	$sp, $sp, 0x18
	move	$ra, $a0
	jr	$ra
	nop
1:	.word	0x800138cd
2:	.word	fn_ca20 + 1
	.size	fn_cc94, . - fn_cc94
