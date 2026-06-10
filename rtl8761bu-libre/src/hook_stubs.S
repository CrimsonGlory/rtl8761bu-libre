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
	/* fn_b174 @ PRAM+0x1174, fn_b7d4 @ PRAM+0x17D4, fn_b7f0 @ PRAM+0x17F0, fn_c09c @ PRAM+0x209C, fn_c0f4 @ PRAM+0x20F4, fn_bce0 @ PRAM+0x1CE0 — t2_hooks.S */
	/* fn_be20 @ PRAM+0x1E20 — t1_hooks.S */
	/* fn_c198 @ PRAM+0x2198, fn_c1e8 @ PRAM+0x21E8, fn_c224 @ PRAM+0x2224 — t1_hooks.S */
	/* fn_d1f4 @ PRAM+0x31F4 — t1_hooks.S */
	/* fn_c780 @ PRAM+0x2780, fn_c63c @ PRAM+0x263C — t1_hooks.S */
	/* fn_dd1c @ PRAM+0x3D1C, fn_d890 @ PRAM+0x3890, fn_d618 @ PRAM+0x3618 — t1_hooks.S */
	/* fn_a594 @ PRAM+0x0594, fn_a49c @ PRAM+0x049C — t1_hooks.S (pool overlay) */
	/* fn_a410 @ PRAM+0x0468, fn_a4ac @ PRAM+0x04AC, fn_a550 @ PRAM+0x0550, fn_a5ac @ PRAM+0x05AC, fn_bce0 @ PRAM+0x1CE0, fn_bda0 @ PRAM+0x1DA0, fn_abd0 @ PRAM+0xBD0, fn_f884 @ PRAM+0x5884, fn_b4d0, fn_a84c, fn_aa58 — t2_hooks.S */
	/* fn_c160 @ PRAM+0x2160, fn_c178 @ PRAM+0x2178, fn_c854 — t2_hooks.S */
	/* fn_c49c @ PRAM+0x249C, fn_c43c @ PRAM+0x243C — t1_hooks.S */
	/* fn_ccb8 @ PRAM+0x2CB8, fn_ce0c @ PRAM+0x2E0C — t3_hooks.S (init-chain callee + hook) */
	/* fn_d168 @ PRAM+0x3168, fn_e350 @ PRAM+0x4350, fn_f85c @ PRAM+0x585C, fn_f950 @ PRAM+0x5950, fn_fa34 @ PRAM+0x5A34, fn_fb08 @ PRAM+0x5B08 — t3_hooks.S */
	/* fn_10ddc @ PRAM+0x0E4C — t4_hooks.S (relocated from native 0x09F8) */
	/* fn_10ca4 @ PRAM+0x6350 — t4_hooks.S (sub-inst #3; sub2_fn_18 @ +0x14) */
	/* fn_e350 @ PRAM+0x4350 — t3_hooks.S */
	STUB_RET	fn_e82c
	STUB_RET	fn_eac0_callee
	/* fn_daa4 @ PRAM+0x3AA4 — protocol_dispatch.S */
	/* fn_d9f4 @ PRAM+0x39F4 — protocol_dispatch.S */
	/* fn_da70 @ PRAM+0x3A70 — protocol_dispatch.S */
	/* fn_ca20 @ PRAM+0x2A20 — protocol_dispatch.S (534 B IMPL-T3) */

	/* sub-installer #2 targets (19) — sub2_hooks.S @ vendor-fixed PRAM offsets */
