/*
 * fn_c224_pre_gap.S — vendor bytes @ PRAM [0x2214, 0x2224) (16 B).
 *
 * Literal pool before fn_c224; pad.py left NOP zeros here.
 * Regenerate: scripts/gen_fn_c224_pre_gap_asm.py
 */

	.section	.text.fn_c224_pre_gap, "ax"
	.globl		fn_c224_pre_gap
	.type		fn_c224_pre_gap, @object
fn_c224_pre_gap:

	/* 0x2214 / 0x8010c214 */
	.byte	0x0a, 0xd1, 0x09, 0xd0, 0x04, 0x67, 0x26, 0x67, 0x20, 0xf0, 0xc0, 0xa0, 0x02, 0x6a, 0x64, 0xa5

	.size	fn_c224_pre_gap, . - fn_c224_pre_gap
