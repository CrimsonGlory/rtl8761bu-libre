/*
 * patch_entry_helpers.S — vendor helper cluster @ file [0x5d8, 0x724)
 *
 * Interim PE-0/PE-3: verbatim vendor bytes until libre RE implements each fn.
 * SPDX-License-Identifier: GPL-2.0-or-later
 */

	.section	.text.helpers
	.globl		patch_entry_helpers
	.type		patch_entry_helpers, @function
patch_entry_helpers:
	.incbin		"build/vendor_helpers.bin"
	.size		patch_entry_helpers, . - patch_entry_helpers
