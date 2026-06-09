/*
 * patch_entry_si2.S — sub_installer_2 @ file 0x764
 *
 * Vendor FUN_8010a000 calls sub_installer_2 via pool DAT_8010a27c → 0x8010A764.
 * Must precede patch_entry_tail in the prefix layout (see installer_vendor_early.S).
 *
 * SPDX-License-Identifier: GPL-2.0-or-later
 */

	.set	mips16

	.section	.text.si2
	.include	"sub_installer_2_body.S"
