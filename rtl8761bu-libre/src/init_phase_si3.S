/* phase-si3: through sub_installer_3 */
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
	INSTALL_HOOK 0x80121318, fn_b118
	INSTALL_HOOK 0x80120844, fn_b174
	INSTALL_HOOK 0x80120784, fn_bba4
	INSTALL_HOOK 0x801286c0, fn_be20
	CALL_PATCH sub_installer_1
	CALL_PATCH sub_installer_2
	INSTALL_HOOK 0x801206cc, fn_c1e8
	INSTALL_HOOK 0x801206d0, fn_c224
	INSTALL_HOOK 0x8012088c, fn_b3d8
	INSTALL_HOOK 0x80121368, fn_b0a4
	INSTALL_HOOK 0x8012136c, fn_c198
	INSTALL_HOOK 0x80121360, fn_d1f4
	INSTALL_HOOK 0x80121344, fn_c780
	INSTALL_HOOK 0x80125550, fn_c63c
	INSTALL_HOOK 0x8012084c, fn_b7f0
	INSTALL_HOOK 0x80120c9c, fn_e27c
	INSTALL_HOOK 0x80120de8, fn_dd1c
	INSTALL_HOOK 0x80120f10, fn_d890
	INSTALL_HOOK 0x80120dbc, fn_d618
	CALL_PATCH sub_installer_3
	PATCH_FINISH
	.size	patch_entry, . - patch_entry
