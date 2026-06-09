/*
 * patch_entry_code.S — FUN_8010a000 code body @ file [0, 0x242)
 *
 * PE-1: verbatim vendor machine code (578 B). Ghidra DATA block @ 0x8010A000;
 * prologue addiu sp,-0x28 / sw ra,s1,s0; phased hook installs via PC-relative
 * pools @ 0x242+. Semantic map: analysis/reverse_engineering_patch_entry.md.
 *
 * SPDX-License-Identifier: GPL-2.0-or-later
 */

	.section	.text.entry
	.globl		patch_entry
	.type		patch_entry, @function
patch_entry:
	.incbin		"build/vendor_entry_code.bin"
	.set		patch_entry_end, .
	.size		patch_entry, patch_entry_end - patch_entry
	/* Pool @ +0x242 must follow with no align padding (vendor PC-relative lw). */
	.balign		1
