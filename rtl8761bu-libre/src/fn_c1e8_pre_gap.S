/*
 * fn_c1e8_pre_gap.S — vendor bytes @ PRAM [0x21D6, 0x21E8) (18 B).
 *
 * Literal pool before fn_c1e8; pad.py left NOP zeros here (HCI 0x2036).
 * Regenerate: scripts/gen_fn_c1e8_pre_gap_asm.py
 */

	.section	.text.fn_c1e8_pre_gap, "ax"
	.globl		fn_c1e8_pre_gap
	.type		fn_c1e8_pre_gap, @object
fn_c1e8_pre_gap:

	/* 0x21d6 / 0x8010c1d6 */
	.byte	0xb0, 0x67, 0x05, 0x6e, 0x58, 0xc4, 0x0c, 0xb2, 0x40, 0xea, 0x0e, 0x6c, 0x09, 0x93, 0x0f, 0x97

	/* 0x21e6 / 0x8010c1e6 */
	.byte	0x0e, 0x91

	.size	fn_c1e8_pre_gap, . - fn_c1e8_pre_gap
