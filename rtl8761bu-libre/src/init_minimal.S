/*
 * init_minimal.S — P0 smoke-test entry (no hooks, no ROM calls)
 *
 * FC20 lands the patch at 0x8010A000; ROM calls 0x8010A001 (body+0).
 * Stock master installer is ~578 B at body+0; this only sets patch-active.
 * Post-patch ROM may still need hooks — use `make test-nf` to bisect.
 *
 * SPDX-License-Identifier: GPL-2.0-or-later
 */

	.set	mips16
	.include "stub_macros.S"

	.section	.text
	.globl	patch_entry
	.type	patch_entry, @function
patch_entry:
	addiu	$sp, $sp, -8
	sw	$ra, 4($sp)

	lw	$a2, 1f
	li	$v0, 4
	sw	$v0, 0($a2)

	lw	$a0, 4($sp)
	addiu	$sp, $sp, 8
	jr	$a0
	nop

	.align	2
1:	.word	PATCH_ACTIVE_ADDR
	.size	patch_entry, . - patch_entry
