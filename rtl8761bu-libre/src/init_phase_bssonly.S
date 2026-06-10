/*
 * phase-bssonly: fn_bss_init only (after minimal prologue)
 */
	.set	mips16
	.include "stub_macros.S"
	.section	.text
	.globl	patch_entry
	.type	patch_entry, @function
patch_entry:
	addiu	$sp, $sp, -8
	sw	$ra, 4($sp)
	CALL_PATCH fn_bss_init
	PATCH_FINISH
	.size	patch_entry, . - patch_entry
