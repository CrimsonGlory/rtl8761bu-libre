/*
 * version_check.S — FUN_8010e214 silicon rev + chip-rev table gate (FUN_8010a000)
 *
 * fn_silicon_rev: reads HW config fields 0x7e/0x7f via ROM FUN_8000b820.
 * fn_version_check: stores bos_state @ 0x8012b814, table lookup, always-OK
 *   fallback for known RTL8761BU hardware (P1).
 *
 * SPDX-License-Identifier: GPL-2.0-or-later
 */

	.set	mips16
	.include "stub_macros.S"

	.section	.text.hooks

	.globl	fn_silicon_rev
	.type	fn_silicon_rev, @function
fn_silicon_rev:
	addiu	$sp, $sp, -0x20
	sw	$ra, 0x1c($sp)
	sw	$s0, 0x18($sp)
	LOAD_ADDR $v0, 0xb000a013
	li	$v1, 0x80
	neg	$v1, $v1
	sb	$v1, 0($v0)
	addiu	$a1, $sp, 0x11
	LOAD_ADDR $s0, 0x8000b821
	jalr	$s0
	li	$a0, 0x7f
	beqz	$v0, 1f
	li	$a0, 0x7e
	jalr	$s0
	addiu	$a1, $sp, 0x10
	beqz	$v0, 1f
	move	$v0, $sp
	lbu	$a0, 0x11($v0)
	lbu	$v0, 0x10($v0)
	sll	$v1, $a0, 8
	or	$v1, $v0
	li	$a0, 0xffff
	bne	$v1, $a0, 2f
	b	1f
2:
	sltu	$a0, $v0
	bnez	$a0, 1f
	li	$a0, 2
	li	$v0, 5
3:
	mult	$a0, $v0
	move	$a1, $v1
	mflo	$v0
	srav	$a1, $v0
	move	$v0, $a1
	li	$a1, 0x1f
	and	$v0, $a1
	move	$a1, $v0
	srl	$a1, 4
	beqz	$a1, 4f
	beqz	$a0, 1f
	addiu	$a0, -1
	li	$v0, 0xff
	and	$a0, $v0
	b	3b
1:
	li	$v0, 0xff
4:
	lw	$a3, 0x1c($sp)
	lw	$s0, 0x18($sp)
	addiu	$sp, $sp, 0x20
	move	$ra, $a3
	jr	$ra
	nop
	.size	fn_silicon_rev, . - fn_silicon_rev

	.globl	fn_version_check
	.type	fn_version_check, @function
fn_version_check:
	addiu	$sp, $sp, -8
	sw	$ra, 4($sp)
	lw	$a0, 1f
	jalr	$a0
	nop
	move	$s0, $v0
	LOAD_ADDR $a2, 0x8012b814
	sb	$s0, 0($a2)
	LOAD_ADDR $a3, 0xb000a0a6
	lbu	$a0, 0($a3)
	move	$a1, $a0
	li	$a0, 0x1f
	and	$a1, $a0
	sb	$a1, 1($a2)
	move	$v1, $zero
	move	$a0, $s0
	li	$a3, 0x10
	sltu	$a0, $a3
	beqz	$a0, 9f
	bne	$a1, $s0, 8f
	LOAD_ADDR $a0, chip_rev_table
	addu	$a0, $a0, $s0
	lbu	$v1, 0($a0)
	li	$a0, 1
	bne	$v1, $a0, 8f
	b	9f
8:
	li	$v1, 1
9:
	sb	$v1, 2($a2)
	lw	$a0, 4($sp)
	addiu	$sp, $sp, 8
	move	$ra, $a0
	jr	$ra
	nop
	.align	2
1:	.word	fn_silicon_rev + 1
	.size	fn_version_check, . - fn_version_check

	.section	.rodata.chip_rev
	.align	2
	.globl	chip_rev_table
chip_rev_table:
	.byte	1, 1, 1, 0, 1, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0
