/*
 * bdaddr.S — TLV config applier (FUN_8010a7b8) + ROM BD_ADDR sync
 *
 * Boot order in patch_entry Phase 3:
 *   1. fn_tlv_applier  — apply btrtl config TLV records to config_base
 *   2. fn_bdaddr_sync  — ROM copies_config_bdaddr @ 0x8000fd38
 *
 * TLV blob layout (ROM-populated from rtl8761bu_config.bin):
 *   0x801115fc  LE32 magic 0x8723ab55
 *   0x80111600  LE16 remaining (sum of entry wire sizes)
 *   0x80111602  entries: [u16 offset][u8 len][data...]
 *
 * BD_ADDR override: offset 0x0030, len 6 → config_base+0x30 (0x801200a0).
 * copies_config_bdaddr propagates that field into the global HCI BD_ADDR.
 *
 * SPDX-License-Identifier: GPL-2.0-or-later
 */

	.set	mips16
	.include "stub_macros.S"

	.section	.text.hooks

	.globl	fn_tlv_applier
	.type	fn_tlv_applier, @function
fn_tlv_applier:
	addiu	$sp, $sp, -0x18
	sw	$ra, 0x14($sp)
	sw	$s0, 0x10($sp)
	sw	$s1, 0xc($sp)

	/* Guard 1: blob_start (0x801115f8) <= blob_end (0x80113ff0) */
	LOAD_ADDR $a0, 0x801115f8
	LOAD_ADDR $a1, 0x80113ff0
	move	$v0, $a1
	sltu	$v0, $a0
	bnez	$v0, tlv_done

	/* Guard 2: *(0x801115fc) == 0x8723ab55 */
	LOAD_ADDR $a0, 0x801115fc
	lw	$v0, 0($a0)
	lw	$a1, tlv_magic
	bne	$v0, $a1, tlv_done

	LOAD_ADDR $s0, 0x80111602	/* entry pointer */
	LOAD_ADDR $a0, 0x80111600
	lhu	$s1, 0($a0)		/* remaining */

tlv_loop:
	beqz	$s1, tlv_done

	/* if (0x80113fff < entry + 2) return */
	LOAD_ADDR $a0, 0x80113fff
	addiu	$a1, $s0, 2
	move	$v0, $a0
	sltu	$v0, $a1
	bnez	$v0, tlv_done

	lbu	$v0, 2($s0)		/* entry len byte */
	addiu	$v1, $v0, 3		/* rec_len = len + 3 */
	move	$a0, $s1
	sltu	$a0, $v1
	bnez	$a0, tlv_done

	addu	$a0, $s0, $v1
	LOAD_ADDR $a1, 0x80113fff
	move	$v0, $a1
	sltu	$v0, $a0
	bnez	$v0, tlv_done

	lhu	$a0, 0($s0)		/* entry offset / type */
	li	$a1, 0x411
	move	$v0, $a0
	sltu	$v0, $a1
	beqz	$v0, tlv_next

	LOAD_ADDR $a2, 0x80120070	/* config_base */
	addu	$a0, $a2, $a0		/* dst = config_base + offset */
	addiu	$a1, $s0, 3		/* src = entry data */
	LOAD_ADDR $a3, 0x8000e85d	/* ROM optimized_memcpy */
	jalr	$a3
	nop

tlv_next:
	addu	$s0, $s0, $v1
	subu	$s1, $s1, $v1
	li	$a0, 0xffff
	and	$s1, $a0
	b	tlv_loop
	nop

tlv_done:
	lw	$a3, 0x14($sp)
	lw	$s0, 0x10($sp)
	lw	$s1, 0xc($sp)
	addiu	$sp, $sp, 0x18
	move	$ra, $a3
	jr	$ra
	nop
	.align	2
tlv_magic:
	.word	0x8723ab55
	.size	fn_tlv_applier, . - fn_tlv_applier

	.globl	fn_bdaddr_sync
	.type	fn_bdaddr_sync, @function
fn_bdaddr_sync:
	/* ROM copies_config_bdaddr: sync config_base+0x30 → global BD_ADDR */
	LOAD_ADDR $a0, 0x8000fd39
	jr	$a0
	nop
	.size	fn_bdaddr_sync, . - fn_bdaddr_sync
