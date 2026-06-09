/*
 * patch_entry_tail.S — tail of patch_entry after prefix layout anchor @ 0x764
 *
 * Moved from init.S so patch_entry fits in [0, 0x5d8) with helpers @ 0x5d8.
 * SPDX-License-Identifier: GPL-2.0-or-later
 */

	.set	mips16
	.include "stub_macros.S"

	.section	.text.entry.tail
	.globl		patch_entry_tail
	.type		patch_entry_tail, @function
patch_entry_tail:
	INSTALL_HOOK_ABS 0x80120cdc, 0x8010ce0c

	CALL_PATCH fn_rf_init

	LOAD_ADDR $a2, PATCH_ACTIVE_ADDR
	li	$v0, 4
	sw	$v0, 0($a2)

	/* hooks #42–44 */
	INSTALL_HOOK_ABS 0x80121020, 0x8010ae4c
	INSTALL_HOOK_ABS 0x80121220, 0x8010bda0
	INSTALL_HOOK_ABS 0x8012167c, 0x8010e350

	jr	$ra
	nop
	.size	patch_entry_tail, . - patch_entry_tail
