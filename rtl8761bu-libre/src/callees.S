/*
 * callees.S — entry-point callees (e27c, HW probe, BB, RF).
 * TLV + BD_ADDR sync live in bdaddr.S.
 * SPDX-License-Identifier: GPL-2.0-or-later
 */

	.set	mips16
	.include "stub_macros.S"

	.section	.text.hooks

	.globl	fn_e27c
	.type	fn_e27c, @function
fn_e27c:
	addiu	$sp, $sp, -8
	sw	$ra, 4($sp)
	LOAD_ADDR $a2, 0x8012ae8c
	lw	$a2, 0($a2)
	beqz	$a2, 9f
	lw	$a3, 1f
	sw	$a3, 0($a2)
	lw	$a3, 2f
	sw	$a3, 0x18($a2)
	lw	$a3, 3f
	sw	$a3, 0x30($a2)
	lw	$a3, 4f
	sw	$a3, 0x3c($a2)
	lw	$a3, 5f
	sw	$a3, 0x48($a2)
	lw	$a3, 6f
	sw	$a3, 0x60($a2)
	li	$a0, 0x40
	li	$a1, 2
	li	$a2, 2
	LOAD_ADDR $a3, 0x8001139d
	jalr	$a3
	nop
9:
	lw	$a0, 4($sp)
	addiu	$sp, $sp, 8
	move	$ra, $a0
	jr	$ra
	nop
	.align	2
1:	.word	0x8010cc95
2:	.word	0x8010d9f5
3:	.word	0x8010daa5
4:	.word	0x8010da71
5:	.word	0x8010dfb1
6:	.word	0x80066e69
	.size	fn_e27c, . - fn_e27c

	.globl	fn_hw_probe
	.type	fn_hw_probe, @function
fn_hw_probe:
	li	$v0, 1
	jr	$ra
	nop
	.size	fn_hw_probe, . - fn_hw_probe

	.globl	fn_bb_init
	.type	fn_bb_init, @function
fn_bb_init:
	addiu	$sp, $sp, -8
	sw	$ra, 4($sp)
	li	$a0, 0x114
	li	$a1, 2
	LOAD_ADDR $a3, 0x80011511
	jalr	$a3
	nop
	move	$a2, $v0
	addiu	$a2, $a2, 3
	li	$a0, 0x114
	li	$a1, 2
	LOAD_ADDR $a3, 0x80011609
	jalr	$a3
	nop
	li	$a0, 0x154
	li	$a1, 2
	LOAD_ADDR $a3, 0x80011511
	jalr	$a3
	nop
	move	$a2, $v0
	addiu	$a2, $a2, 0x10
	li	$a0, 0x154
	li	$a1, 2
	LOAD_ADDR $a3, 0x80011609
	jalr	$a3
	nop
	lw	$a0, 4($sp)
	addiu	$sp, $sp, 8
	move	$ra, $a0
	jr	$ra
	nop
	.size	fn_bb_init, . - fn_bb_init

	.globl	fn_rf_init
	.type	fn_rf_init, @function
fn_rf_init:
	addiu	$sp, $sp, -0x40
	sw	$ra, 0x3c($sp)
	LOAD_ADDR $s1, rf_table_case0
	li	$s0, 0x17
rf_loop:
	beqz	$s0, rf_done
	lhu	$a0, 0($s1)
	li	$a1, 2
	LOAD_ADDR $a3, 0x80011585
	jalr	$a3
	nop
	move	$a2, $v0
	li	$a1, 2
	LOAD_ADDR $a3, 0x80011609
	jalr	$a3
	nop
	addiu	$s1, $s1, 2
	addiu	$s0, $s0, -1
	b	rf_loop
	nop
rf_done:
	lw	$a0, 0x3c($sp)
	addiu	$sp, $sp, 0x40
	move	$ra, $a0
	jr	$ra
	nop
	.size	fn_rf_init, . - fn_rf_init

	.section	.rodata.rf
	.align	2
	.globl	rf_table_case0
rf_table_case0:
	.half	0x0160, 0x0164, 0x0168, 0x0190, 0x0194, 0x0198, 0x01a0
	.half	0x01b0, 0x01b4, 0x01b8, 0x01bc, 0x01c0, 0x01c4, 0x01c8
	.half	0x01d0, 0x01d4, 0x01d8, 0x01dc, 0x01e0, 0x01e4, 0x01e8
	.half	0x01ec, 0x01f0
