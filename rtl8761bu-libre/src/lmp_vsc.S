/*
 * lmp_vsc.S — LMP VSC gateway (FUN_8010bba4), validator (FUN_8010bb54),
 *              and bos+0x1c dispatcher (FUN_8010bc74).
 *
 * Literal pools match GZF data block (verified via DiagAddr 2026-06-09).
 * ROM leaf VSC handler FUN_80047c50 is called through the gateway pool;
 * no reimplementation of the 700-byte ROM dispatcher.
 *
 */

	.set	mips16
	.include "stub_macros.S"

	.section	.text.hooks

/*
 * FUN_8010bb54 — eSCO packet-type / connection validator (74 B).
 * Called from gateway pool slot bc5c before per-link dispatch.
 */
	.globl	fn_bb54
	.type	fn_bb54, @function
fn_bb54:
	addiu	$sp, $sp, -0x18
	sw	$ra, 0x14($sp)
	sw	$s0, 0x10($sp)
	addiu	$v0, $a0, 0x3
	move	$s0, $a0
	lbu	$a0, 0($v0)
	lw	$v0, 1f
	jalr	$v0
	nop
	lbu	$a0, 4($s0)
	li	$v1, 0x10
	and	$v1, $a0
	bnez	$v1, 6f
	li	$v1, 0x3
	and	$v1, $a0
	cmpi	$v1, 0x3
	bteqz	5f
	beqz	$v0, 6f
	lhu	$v1, 0x2c($v0)
	beqz	$v1, 2f
	li	$v1, 0x2
	and	$v1, $a0
	li	$a1, 0xff
	and	$v1, $a1
	bnez	$v1, 5f
2:
	lhu	$v0, 0x2e($v0)
	beqz	$v0, 6f
	li	$v0, 0x1
	and	$v0, $a0
	beqz	$v0, 6f
5:
	li	$v0, 0x12
	b	7f
6:
	li	$v0, 0
7:
	lw	$a3, 0x14($sp)
	lw	$s0, 0x10($sp)
	jr	$a3
	addiu	$sp, $sp, 0x18
	.align	2
1:	.word	0x8004e2d1
	.size	fn_bb54, . - fn_bb54

/*
 * FUN_8010bba4 — LMP VSC gateway for bos+0xd8 / LMP opcode 0x268 (176 B).
 * Phases: alloc → busy check → validator → conn lookup → state inject →
 * ROM FUN_80047c50 dispatch → restore → 5-byte LMP response.
 */
	.globl	fn_bba4
	.type	fn_bba4, @function
fn_bba4:
	addiu	$sp, $sp, -0x40
	sw	$ra, 0x3c($sp)
	sw	$s1, 0x38($sp)
	sw	$s0, 0x34($sp)
	li	$v0, 0
	move	$v1, $sp
	move	$s1, $a0
	sb	$v0, 0x10($v1)
	lw	$v0, 9f
	jalr	$v0
	li	$a0, 0x2
	beqz	$v0, 1f
	lw	$v0, 8f
	lbu	$v1, 0x28($v0)
	li	$v0, 0x1
	and	$v0, $v1
	bnez	$v0, 1f
	lw	$v0, 7f
	jalr	$v0
	move	$a0, $s1
	move	$a1, $v0
	li	$v1, 0x12
	bnez	$v0, 2f
	lbu	$a0, 3($s1)
	lw	$v0, 6f
	jalr	$v0
	sw	$a1, 0x24($sp)
	move	$s0, $v0
	lw	$a1, 0x24($sp)
	bnez	$v0, 3f
	li	$a2, 0
	sw	$a2, 0x20($sp)
	move	$a3, $a2
	b	4f
3:
	lhu	$a3, 0x2c($v0)
	beqz	$a3, 10f
	sh	$a1, 0x2c($v0)
10:
	lhu	$v0, 0x2e($s0)
	li	$a2, 0x1
	sw	$v0, 0x20($sp)
	beqz	$v0, 4f
	li	$v0, 0
	sh	$v0, 0x2e($s0)
4:
	sw	$a2, 0x24($sp)
	sw	$a3, 0x28($sp)
	move	$a0, $s1
	lw	$v0, 5f
	jalr	$v0
	addiu	$a1, $sp, 0x10
	lw	$a2, 0x24($sp)
	move	$v1, $v0
	lw	$a3, 0x28($sp)
	beqz	$a2, 2f
	move	$a0, $sp
	addiu	$v0, $a0, 0x7
	sh	$a3, 0x2c($s0)
	addiu	$v0, 0x19
	lhu	$a0, 0($v0)
	sh	$a0, 0x2e($s0)
	b	2f
1:
	li	$v1, 0xc
2:
	lw	$v0, 4f
	jalr	$v0
	sw	$v1, 0x24($sp)
	lhu	$a0, 0($s1)
	addiu	$s0, $sp, 0x14
	lw	$v0, 3f
	jalr	$v0
	move	$a1, $s0
	lw	$v1, 0x24($sp)
	move	$v0, $sp
	move	$a0, $sp
	sb	$v1, 0x17($v0)
	lbu	$v0, 0x10($v0)
	move	$a1, $s0
	li	$a2, 0x5
	sb	$v0, 0x18($a0)
	lw	$v0, 2f
	jalr	$v0
	li	$a0, 0xe
	lw	$v1, 0x24($sp)
	lw	$a3, 0x3c($sp)
	lw	$s1, 0x38($sp)
	lw	$s0, 0x34($sp)
	move	$v0, $v1
	jr	$a3
	addiu	$sp, $sp, 0x40
	.align	2
9:	.word	0x80044589	/* alloc (arg=2) */
8:	.word	0x8012382c	/* large2 base; busy @ +0x28 */
7:	.word	fn_bb54 + 1	/* validator */
6:	.word	0x8004e2d1	/* per-link conn lookup */
5:	.word	0x80047c51	/* ROM VSC leaf FUN_80047c50 */
4:	.word	0x80050305	/* post-dispatch cleanup */
3:	.word	0x80044565	/* LMP response header builder */
2:	.word	0x8001d071	/* hci_event_sender */
	.size	fn_bba4, . - fn_bba4

/*
 * FUN_8010bc74 — bos+0x1c LMP/HCI VSC dispatcher companion (80 B).
 * Alloc → double-indirect ROM handler → build/send 5-byte response.
 */
	.globl	fn_bc74
	.type	fn_bc74, @function
fn_bc74:
	addiu	$sp, $sp, -0x30
	sw	$ra, 0x2c($sp)
	sw	$s1, 0x28($sp)
	sw	$s0, 0x24($sp)
	lw	$v0, 8f
	sw	$a0, 0x30($sp)
	lbu	$s1, 0x2c($v0)
	lw	$v0, 7f
	jalr	$v0
	li	$a0, 0x1
	li	$s0, 0
	bnez	$v0, 1f
	li	$s0, 0xc
1:
	lw	$v0, 6f
	lw	$v0, 0($v0)
	move	$a0, $s1
	jalr	$v0
	li	$a1, 0
	move	$s1, $v0
	lw	$v0, 0x30($sp)
	lw	$v1, 5f
	sra	$s1, $s1, 1
	lhu	$a0, 0($v0)
	addiu	$v0, $sp, 0x10
	move	$a1, $v0
	jalr	$v1
	sw	$v0, 0x18($sp)
	move	$v1, $sp
	sb	$s0, 0x13($v1)
	sb	$s1, 0x14($v1)
	lw	$v0, 0x18($sp)
	li	$a0, 0xe
	move	$a1, $v0
	lw	$v0, 4f
	jalr	$v0
	li	$a2, 0x5
	move	$v0, $s0
	lw	$a3, 0x2c($sp)
	lw	$s1, 0x28($sp)
	lw	$s0, 0x24($sp)
	jr	$a3
	addiu	$sp, $sp, 0x30
	.align	2
8:	.word	0x80123394	/* conn struct base */
7:	.word	0x80044589	/* alloc (arg=1) */
6:	.word	0x80120650	/* RAM double-indirect slot */
5:	.word	0x80044565	/* response header builder */
4:	.word	0x8001d071	/* hci_event_sender */
	.size	fn_bc74, . - fn_bc74
