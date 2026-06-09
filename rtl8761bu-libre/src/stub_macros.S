/*
 * stub_macros.S — shared MIPS16e macros for rtl8761bu-libre
 * SPDX-License-Identifier: GPL-2.0-or-later
 */

	.macro LOAD_ADDR reg, addr
	lw	\reg, 1f
	b	2f
	nop
	.align	2
1:	.word	\addr
2:
	.endm

	.macro CALL_ROM rom_addr
	addiu	$sp, $sp, -8
	sw	$ra, 4($sp)
	LOAD_ADDR $a0, \rom_addr
	jalr	$a0
	nop
	lw	$a0, 4($sp)
	addiu	$sp, $sp, 8
	move	$ra, $a0
	.endm

	.macro CALL_PATCH fn
	addiu	$sp, $sp, -8
	sw	$ra, 4($sp)
	lw	$a0, 1f
	jalr	$a0
	nop
	lw	$a0, 4($sp)
	addiu	$sp, $sp, 8
	move	$ra, $a0
	b	2f
	nop
	.align	2
1:	.word	\fn + 1
2:
	.endm

	.macro INSTALL_HOOK slot_addr, handler
	LOAD_ADDR $a2, \slot_addr
	lw	$a3, 1f
	sw	$a3, 0($a2)
	b	2f
	nop
	.align	2
1:	.word	\handler + 1
2:
	.endm
