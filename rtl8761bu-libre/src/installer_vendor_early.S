/*
 * installer_vendor_early.S — vendor patch1 bytes [0, VENDOR_EARLY_PREFIX)
 *
 * UB500 bisect: connect requires vendor patch_entry … sub_installer_1 through
 * file offset 0x763; libre linker layout does not match vendor encoding.
 * Built with VENDOR_EARLY_PREFIX=0x764 and NF_REF; replaces init.S patch_entry.
 *
 * SPDX-License-Identifier: GPL-2.0-or-later
 */

	.section	.text
	.globl		patch_entry
	.type		patch_entry, @function
patch_entry:
	.incbin		"build/vendor_prefix.bin"
	.size		patch_entry, . - patch_entry

	/* Same .text object as patch_entry → sub_installer_2 @ file 0x764 (no ld padding). */
	.include	"sub_installer_2_body.S"
