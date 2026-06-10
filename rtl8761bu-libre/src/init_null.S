/*
 * SPDX-License-Identifier: GPL-2.0-or-later
 * init_null.S — absolute minimum: return to ROM immediately.
 * Bisect: if this still FC20-times out, the bug is not patch_entry logic.
 */

	.set	mips16

	.section	.text
	.globl	patch_entry
	.type	patch_entry, @function
patch_entry:
	jr	$ra
	nop
	.size	patch_entry, . - patch_entry
