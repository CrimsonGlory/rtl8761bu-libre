/*
 * fn_d618_pool.S — literal pool @ PRAM+0x37C0 (runtime 0x8010D7C0).
 *
 * GZF DATA block pool for FUN_8010d618 (422 B body + adjacent words).
 * SCO dispatch: DAT_8010d7dc → fn_a84c+1, DAT_8010d7e0 → fn_aa58+1.
 */

	.section	.text.fn_d618_pool, "a"
	.align	4
	.globl		fn_d618_pool
	.type		fn_d618_pool, @object
fn_d618_pool:
	.word	0x8010d435
	.word	0x8001cd75
	.word	0x80120070
	.word	0x80123a20
	.word	0x8012dc50
	.word	0x8001d071
	.word	0x8012b83c
	.word	fn_a84c + 1
	.word	fn_aa58 + 1
	.size	fn_d618_pool, . - fn_d618_pool
