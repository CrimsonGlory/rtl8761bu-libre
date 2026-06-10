/*
 * phase-bss: prologue + fn_bss_init, then patch-active + return
 */
	.set	mips16
	.include "stub_macros.S"
	.section	.text
	.globl	patch_entry
	.type	patch_entry, @function
patch_entry:
	addiu	$sp, $sp, -8
	sw	$ra, 4($sp)
	move	$a3, $zero
	LOAD_ADDR $a2, 0x80120f0c
	sw	$a3, 0($a2)
	LOAD_ADDR $a2, 0x80120c80
	sw	$a3, 0($a2)
	LOAD_ADDR $a2, 0x801115f8
	lw	$a3, 0($a2)
	LOAD_ADDR $a2, 0x80120060
	sh	$a3, 0($a2)
	srl	$a3, 16
	LOAD_ADDR $a2, 0x80120062
	sh	$a3, 0($a2)
	CALL_PATCH fn_bss_init
	PATCH_FINISH
	.size	patch_entry, . - patch_entry
