/*
 * stub_macros.S — shared MIPS16e macros for rtl8761bu-libre
 *
 * Literal pools use \@ (requires .altmacro) so multiple expansions in one
 * function do not merge pools and corrupt loaded addresses.
 */

.set	PATCH_ACTIVE_ADDR, 0x80120538

.altmacro

	.macro LOAD_ADDR reg, addr
	lw	\reg, .Lla_\@
	b	.Llb_\@
	nop
	.align	2
.Lla_\@:
	.word	\addr
.Llb_\@:
	.endm

	.macro CALL_ROM rom_addr
	addiu	$sp, $sp, -8
	sw	$ra, 4($sp)
	LOAD_ADDR $a0, \rom_addr
	jalr	$a0
	nop
	lw	$a0, 4($sp)
	addiu	$sp, $sp, 8
	move	$ra, $a0
	.endm

	.macro CALL_PATCH fn
	addiu	$sp, $sp, -8
	sw	$ra, 4($sp)
	lw	$a0, .Lcp_\@
	jalr	$a0
	nop
	lw	$a0, 4($sp)
	addiu	$sp, $sp, 8
	move	$ra, $a0
	b	.Lcp_end_\@
	nop
	.align	2
.Lcp_\@:
	.word	\fn + 1
.Lcp_end_\@:
	.endm

	.macro PATCH_FINISH
	LOAD_ADDR $a2, PATCH_ACTIVE_ADDR
	li	$v0, 4
	sw	$v0, 0($a2)
	lw	$a0, 4($sp)
	addiu	$sp, $sp, 8
	jr	$a0
	nop
	.endm

	.macro INSTALL_HOOK slot_addr, handler
	LOAD_ADDR $a2, \slot_addr
	lw	$a3, .Lih_\@
	sw	$a3, 0($a2)
	b	.Lih_end_\@
	nop
	.align	2
.Lih_\@:
	.word	\handler + 1
.Lih_end_\@:
	.endm

	/* handler_runtime: even MIPS16e address in patch1 (body injected from vendor) */
	.macro INSTALL_HOOK_ABS slot_addr, handler_runtime
	LOAD_ADDR $a2, \slot_addr
	lw	$a3, .Lihabs_\@
	sw	$a3, 0($a2)
	b	.Lihabs_end_\@
	nop
	.align	2
.Lihabs_\@:
	.word	\handler_runtime + 1
.Lihabs_end_\@:
	.endm
