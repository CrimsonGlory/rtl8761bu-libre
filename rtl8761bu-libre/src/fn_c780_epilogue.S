/*
 * fn_c780_epilogue.S — 2 B vendor tail @ PRAM+0x27A2 (before literal pool).
 */

	.section	.text.fn_c780_epilogue, "ax"
	.globl	fn_c780_epilogue
	.type	fn_c780_epilogue, @object
fn_c780_epilogue:
	.byte	0x60, 0x9b
	.size	fn_c780_epilogue, . - fn_c780_epilogue
