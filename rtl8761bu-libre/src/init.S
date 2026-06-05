/*
 * init.S — RTL8761BU libre firmware entry point (MIPS16e assembly)
 *
 * CPU: MIPS16e (little-endian, MIPS32r2 base)
 * Load address: 0x8010A000  (Patch RAM, overlays ROM)
 * Entry point: patch_entry at PRAM+0x0000
 *
 * The ROM calls patch_entry via JALX (cross-ISA jump to MIPS16e mode).
 *
 * Address loading — 3-instruction sequence per ROM call:
 *   li    $a0, adj_upper    signed 16-bit: upper half, +1 when lower>=0x8000
 *   sll   $a0, 16           shift left 16 (MIPS16e EXTEND+SHIFT)
 *   addiu $a0, lower_s      signed 16-bit: lower half (negative if >=0x8000)
 *   Both halves fit in [-32768..32767].  SLL 16 discards sign-extension;
 *   the signed ADDIU wraps correctly in 32-bit arithmetic.
 *
 * $ra save/restore:
 *   sw  $ra,  4($sp)   SWRASP (I8): $ra and $sp hardcoded in encoding
 *   lw  $a0,  4($sp)   LWSP:  $a0=r4 is in MIPS16e 3-bit restricted set
 *   move $ra, $a0      MOV32R: restricted $a0 -> MIPS32 $ra
 *
 * SPDX-License-Identifier: GPL-2.0-or-later
 */

	.set	mips16

	.section	.text.entry
	.globl	patch_entry
	.type	patch_entry, @function
patch_entry:
	/* Prologue: 8-byte stack frame, save $ra via SWRASP */
	addiu	$sp, $sp, -8
	sw	$ra, 4($sp)

	/* Group 1 — hardware helpers */
	li	$a0, -32751	/* hi: 0x8011 */
	sll	$a0, 16
	addiu	$a0, -24716	/* lo: 0x9f74 → 0x80109f74 */
	jalr	$a0

	li	$a0, -32751	/* hi: 0x8011 */
	sll	$a0, 16
	addiu	$a0, -24704	/* lo: 0x9f80 → 0x80109f80 */
	jalr	$a0

	li	$a0, -32751	/* hi: 0x8011 */
	sll	$a0, 16
	addiu	$a0, -24678	/* lo: 0x9f9a → 0x80109f9a */
	jalr	$a0

	li	$a0, -32751	/* hi: 0x8011 */
	sll	$a0, 16
	addiu	$a0, -24672	/* lo: 0x9fa0 → 0x80109fa0 */
	jalr	$a0

	li	$a0, -32751	/* hi: 0x8011 */
	sll	$a0, 16
	addiu	$a0, -24666	/* lo: 0x9fa6 → 0x80109fa6 */
	jalr	$a0

	li	$a0, -32751	/* hi: 0x8011 */
	sll	$a0, 16
	addiu	$a0, -24660	/* lo: 0x9fac → 0x80109fac */
	jalr	$a0

	li	$a0, -32751	/* hi: 0x8011 */
	sll	$a0, 16
	addiu	$a0, -24654	/* lo: 0x9fb2 → 0x80109fb2 */
	jalr	$a0

	li	$a0, -32751	/* hi: 0x8011 */
	sll	$a0, 16
	addiu	$a0, -24648	/* lo: 0x9fb8 → 0x80109fb8 */
	jalr	$a0

	/* Group 2 — BT core init */
	li	$a0, -32675	/* hi: 0x805d */
	sll	$a0, 16
	addiu	$a0, -24362	/* lo: 0xa0d6 → 0x805ca0d6 */
	jalr	$a0

	li	$a0, -32675	/* hi: 0x805d */
	sll	$a0, 16
	addiu	$a0, -24296	/* lo: 0xa118 → 0x805ca118 */
	jalr	$a0

	/* Group 3 — BT subsystem init */
	li	$a0, -32673	/* hi: 0x805f */
	sll	$a0, 16
	addiu	$a0, -24230	/* lo: 0xa15a → 0x805ea15a */
	jalr	$a0

	li	$a0, -32673	/* hi: 0x805f */
	sll	$a0, 16
	addiu	$a0, -24164	/* lo: 0xa19c → 0x805ea19c */
	jalr	$a0

	/* Group 4 — single call */
	li	$a0, -32611	/* hi: 0x809d */
	sll	$a0, 16
	addiu	$a0, -23702	/* lo: 0xa36a → 0x809ca36a */
	jalr	$a0

	/* Group 5 — RF/baseband (4 calls) */
	li	$a0, -32603	/* hi: 0x80a5 */
	sll	$a0, 16
	addiu	$a0, -22826	/* lo: 0xa6d6 → 0x80a4a6d6 */
	jalr	$a0

	li	$a0, -32603	/* hi: 0x80a5 */
	sll	$a0, 16
	addiu	$a0, -22812	/* lo: 0xa6e4 → 0x80a4a6e4 */
	jalr	$a0

	li	$a0, -32603	/* hi: 0x80a5 */
	sll	$a0, 16
	addiu	$a0, -22798	/* lo: 0xa6f2 → 0x80a4a6f2 */
	jalr	$a0

	li	$a0, -32603	/* hi: 0x80a5 */
	sll	$a0, 16
	addiu	$a0, -22784	/* lo: 0xa700 → 0x80a4a700 */
	jalr	$a0

	/* Group 6 — peripheral A (4 calls) */
	li	$a0, -32547	/* hi: 0x80dd */
	sll	$a0, 16
	addiu	$a0, -24320	/* lo: 0xa100 → 0x80dca100 */
	jalr	$a0

	li	$a0, -32547	/* hi: 0x80dd */
	sll	$a0, 16
	addiu	$a0, -24296	/* lo: 0xa118 → 0x80dca118 */
	jalr	$a0

	li	$a0, -32547	/* hi: 0x80dd */
	sll	$a0, 16
	addiu	$a0, -24250	/* lo: 0xa146 → 0x80dca146 */
	jalr	$a0

	li	$a0, -32547	/* hi: 0x80dd */
	sll	$a0, 16
	addiu	$a0, -24238	/* lo: 0xa152 → 0x80dca152 */
	jalr	$a0

	/* Group 7 — peripheral B (4 calls) */
	li	$a0, -32545	/* hi: 0x80df */
	sll	$a0, 16
	addiu	$a0, -24190	/* lo: 0xa182 → 0x80dea182 */
	jalr	$a0

	li	$a0, -32545	/* hi: 0x80df */
	sll	$a0, 16
	addiu	$a0, -24166	/* lo: 0xa19a → 0x80dea19a */
	jalr	$a0

	li	$a0, -32545	/* hi: 0x80df */
	sll	$a0, 16
	addiu	$a0, -24120	/* lo: 0xa1c8 → 0x80dea1c8 */
	jalr	$a0

	li	$a0, -32545	/* hi: 0x80df */
	sll	$a0, 16
	addiu	$a0, -24108	/* lo: 0xa1d4 → 0x80dea1d4 */
	jalr	$a0
	/*
	 * Epilogue: load saved $ra into $a0 (restricted-set reg),
	 * deallocate frame, copy to $ra, return (JR switches to MIPS32).
	 */
	lw	$a0, 4($sp)
	addiu	$sp, $sp, 8
	move	$ra, $a0
	jr	$ra

	.size	patch_entry, . - patch_entry
