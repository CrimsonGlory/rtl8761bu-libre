/*
 * bootstrap.S — BSS init, sub-installers #1–#6.
 * SPDX-License-Identifier: GPL-2.0-or-later
 */

	.set	mips16
	.include "stub_macros.S"

#ifdef VENDOR_EARLY_PREFIX
	/* sub_installer_2 lives in installer_vendor_early.o; si3+ only here. */
	.section	.text
#else
	.section	.text.hooks
#endif

#ifndef VENDOR_EARLY_PREFIX
	.globl	fn_bss_init
	.type	fn_bss_init, @function
fn_bss_init:
	/* Vendor FUN_8010a6c8 @ file 0x754 in patch block; pool @ +0x18. */
	.byte	0xfd, 0x63, 0x05, 0x62, 0x05, 0xb2, 0x06, 0xb6
	.byte	0x82, 0x67, 0x5b, 0xe6, 0x05, 0xb2, 0x40, 0xea
	.word	0x80109c00
	.word	0x8010a000
	.word	0x8000e98d
	.size	fn_bss_init, . - fn_bss_init

	.globl	sub_installer_1
	.type	sub_installer_1, @function
sub_installer_1:
	/* Vendor FUN_8010af40 @ file 0x724 (48 B machine code). */
	.byte	0xff, 0x6a, 0x4c, 0xe9, 0x03, 0x59, 0xa7, 0x61
	.byte	0x01, 0x48, 0x4c, 0xe8, 0x04, 0x58, 0x02, 0x60
	.byte	0x00, 0x69, 0xa1, 0x17, 0x17, 0x97, 0x16, 0x91
	.byte	0x15, 0x90, 0x00, 0x6a, 0x00, 0xef, 0x0c, 0x63
	.byte	0x2c, 0x38, 0x12, 0x80, 0x70, 0x00, 0x12, 0x80
	.byte	0x14, 0x07, 0x11, 0x80, 0xa9, 0x4f, 0x07, 0x80
	.size	sub_installer_1, . - sub_installer_1
#endif /* !VENDOR_EARLY_PREFIX */

#ifndef VENDOR_EARLY_PREFIX
	.include	"sub_installer_2_body.S"
#endif

	.globl	sub_installer_3
	.type	sub_installer_3, @function
sub_installer_3:
	addiu	$sp, $sp, -8
	sw	$ra, 4($sp)
	INSTALL_HOOK_ABS 0x80120f08, 0x80110350
	LOAD_ADDR $a2, 0x80120f0c
	lw	$v0, 0($a2)
	beqz	$v0, 1f
	jalr	$v0
	nop
1:
	lw	$a0, 4($sp)
	addiu	$sp, $sp, 8
	move	$ra, $a0
	jr	$ra
	nop
	.size	sub_installer_3, . - sub_installer_3

	.globl	sub_installer_4
	.type	sub_installer_4, @function
sub_installer_4:
	LOAD_ADDR $a2, 0x80120a70
	LOAD_ADDR $a3, 1f
	lw	$v0, 0($a3)
	sw	$v0, 4($a2)
	lw	$v0, 4($a3)
	sw	$v0, 8($a2)
	lw	$v0, 8($a3)
	sw	$v0, 0($a2)
	lw	$v0, 12($a3)
	sw	$v0, 12($a2)
	lw	$v0, 16($a3)
	sw	$v0, 16($a2)
	lw	$v0, 20($a3)
	sw	$v0, 24($a2)
	lw	$v0, 24($a3)
	sw	$v0, 28($a2)
	lw	$v0, 28($a3)
	sw	$v0, 20($a2)
	lw	$v0, 32($a3)
	sw	$v0, 32($a2)
	lw	$v0, 36($a3)
	sw	$v0, 36($a2)
	jr	$ra
	nop
	.align	2
1:
	.word	0x8012382c, 0x80123a20, 0x80120060, 0x80111198
	.word	0x80120070, 0x8012382c, 0x80123a20, 0x80120060
	.word	0x80111198, 0x80120070
	.size	sub_installer_4, . - sub_installer_4

	.globl	sub_installer_5
	.type	sub_installer_5, @function
sub_installer_5:
	INSTALL_HOOK_ABS 0x80120c88, 0x8010e82c
	jr	$ra
	nop
	.size	sub_installer_5, . - sub_installer_5

	.globl	sub_installer_6
	.type	sub_installer_6, @function
sub_installer_6:
	addiu	$sp, $sp, -8
	sw	$ra, 4($sp)
	LOAD_ADDR $a2, 0x8012b800
	lw	$v0, 1f
	sw	$v0, 0($a2)
	b	2f
	nop
1:	.word	0xffffffff
2:
	LOAD_ADDR $a2, 0x80120c80
	lw	$v0, 0($a2)
	beqz	$v0, 1f
	jalr	$v0
	nop
1:
	lw	$a0, 4($sp)
	addiu	$sp, $sp, 8
	move	$ra, $a0
	jr	$ra
	nop
	.size	sub_installer_6, . - sub_installer_6
