/*
 * hook_stubs.S — minimal MIPS16e stubs for hook targets not yet fully implemented.
 * Each stub returns immediately; the master installer still installs them so ROM
 * dispatch slots are non-NULL. Full implementations replace these in later phases.
 *
 */

	.set	mips16
	.include "stub_macros.S"

	.macro STUB_RET name
	.globl	\name
	.type	\name, @function
\name:
	jr	$ra
	nop
	.size	\name, . - \name
	.endm

	.section	.text.hooks

	/* fn_b0a4 @ PRAM+0x10A4, fn_b118 @ PRAM+0x1118, fn_b3d8 @ PRAM+0x13D8 — t1_hooks.S */
	/* fn_b174 @ PRAM+0x1174, fn_b7f0 @ PRAM+0x17F0, fn_c0f4 @ PRAM+0x20F4 — t2_hooks.S */
	/* fn_be20 @ PRAM+0x1E20 — t1_hooks.S */
	/* fn_c198 @ PRAM+0x2198, fn_c1e8 @ PRAM+0x21E8, fn_c224 @ PRAM+0x2224 — t1_hooks.S */
	/* fn_d1f4 @ PRAM+0x31F4 — t1_hooks.S */
	/* fn_c780 @ PRAM+0x2780, fn_c63c @ PRAM+0x263C — t1_hooks.S */
	/* fn_dd1c @ PRAM+0x3D1C, fn_d890 @ PRAM+0x3890, fn_d618 @ PRAM+0x3618 — t1_hooks.S */
	/* fn_a594 @ PRAM+0x0594, fn_a49c @ PRAM+0x049C — t1_hooks.S (pool overlay) */
	STUB_RET	fn_a4ac
	STUB_RET	fn_bce0
	/* fn_c49c @ PRAM+0x249C, fn_c43c @ PRAM+0x243C — t1_hooks.S */
	STUB_RET	fn_d168
	STUB_RET	fn_fa34
	STUB_RET	fn_f950
	STUB_RET	fn_fb08
	STUB_RET	fn_abd0
	STUB_RET	fn_f884
	STUB_RET	fn_f85c
	STUB_RET	fn_a550
	STUB_RET	fn_c160
	STUB_RET	fn_c178
	STUB_RET	fn_b4d0
	STUB_RET	fn_a5ac
	STUB_RET	fn_c854
	STUB_RET	fn_c09c
	STUB_RET	fn_ce0c
	STUB_RET	fn_10ddc
	STUB_RET	fn_bda0
	STUB_RET	fn_e350
	STUB_RET	fn_10ca4
	STUB_RET	fn_e82c
	STUB_RET	fn_eac0_callee
	/* fn_daa4 @ PRAM+0x3AA4 — protocol_dispatch.S */
	/* fn_d9f4 @ PRAM+0x39F4 — protocol_dispatch.S */
	/* fn_da70 @ PRAM+0x3A70 — protocol_dispatch.S */
	/* fn_ca20 @ PRAM+0x2A20 — protocol_dispatch.S STUB-T1 */

	/* sub-installer #2 targets — stubs until individually implemented */
	STUB_RET	sub2_fn_00
	STUB_RET	sub2_fn_01
	STUB_RET	sub2_fn_02
	STUB_RET	sub2_fn_03
	STUB_RET	sub2_fn_04
	STUB_RET	sub2_fn_05
	STUB_RET	sub2_fn_06
	STUB_RET	sub2_fn_07
	STUB_RET	sub2_fn_08
	STUB_RET	sub2_fn_09
	STUB_RET	sub2_fn_10
	STUB_RET	sub2_fn_11
	STUB_RET	sub2_fn_12
	STUB_RET	sub2_fn_13
	STUB_RET	sub2_fn_14
	STUB_RET	sub2_fn_15
	STUB_RET	sub2_fn_16
	STUB_RET	sub2_fn_17
	STUB_RET	sub2_fn_18
