/*
 * patch_entry_pool.S — FUN_8010a000 literal pool @ file [0x242, 0x5d8)
 *
 * PE-2 interim: verbatim vendor pool until libre emits compact .word block.
 * Required immediately after patch_entry_code.S in .text.entry (link order).
 *
 * SPDX-License-Identifier: GPL-2.0-or-later
 */

	.section	.text.entry
	.incbin		"build/vendor_entry_pool.bin"
