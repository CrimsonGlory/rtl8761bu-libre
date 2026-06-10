/*
 * fn_c780_pool.S — literal pool @ PRAM+0x27A4 (runtime 0x8010C7A4).
 * GZF FUN_8010c780 +0x24: fn_10868+1, ROM 0x800083ed/0x80007af1, flag 0x8012133b.
 */

	.section	.text.fn_c780_pool, "a"
	.globl		fn_c780_pool
	.type		fn_c780_pool, @object
fn_c780_pool:
	.word	fn_10868 + 1
	.word	0x800083ed
	.word	0x80007af1
	.word	0x8012133b
	.size	fn_c780_pool, . - fn_c780_pool
