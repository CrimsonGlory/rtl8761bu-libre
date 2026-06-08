# RTL8761BU Firmware – Kovah Annotated Function List

Source: `2026-04-25_rtl8761buv_USB_fw-and-ROM.bin.gzf` (Veronica Kovah / Dark Mentor LLC)  
Extracted via: `ExtractAnnotations.java` on Ghidra 12.1.2 headless, `-noanalysis` mode

## Address mapping

Kovah's Ghidra project loads:
- **Patch firmware** at base `0x00000000` (file-relative). To get runtime address: `runtime = 0x80100000 + file_offset`
- **ROM dump** at base `0x80000000` (actual chip address)

---

## Patch Firmware Functions (file-relative → runtime)

| File offset | Runtime addr | Name |
|-------------|-------------|------|
| `0x00003780` | `0x80103780` | `thing_that_calls_thing_that_installs_LMP_Patch` |

---

## ROM Functions (0x80000000–0x801FFFFF)

### Interrupt / System Control
| Address | Name |
|---------|------|
| `0x80009104` | `disable_interrupts_(clear_LSBit_of_CP0_Status_Register)` |
| `0x80009120` | `enable_interrupts_(set_CP0_Status_to_arg)` |

### Memory Utilities
| Address | Name |
|---------|------|
| `0x800092e4` | `memcmp` |
| `0x8000e85c` | `optimized_memcpy` |
| `0x8000e98c` | `memset` |

### Initialization / Patch Bootstrap
| Address | Name |
|---------|------|
| `0x8000fb5c` | `lots_of_initialization` |
| `0x8000fd38` | `copies_config_bdaddr` |
| `0x8000e9cc` | `references_patch_download_mem2` |
| `0x800093f8` | `wrap_set_two_global_ptrs` |
| `0x8001343c` | `second_set_func_in_set_two_global_ptrs` |
| `0x800109ac` | `calls_to_0x8010a001_as_fptr_to_install_patches` ← ROM bootstrap that jumps into patch |
| `0x80021924` | `initialize_some_global_struct_FUN_80021924` |
| `0x8001cd74` | `initialize_0x28_sized_struct` |

### LMP (Link Manager Protocol) Handlers
| Address | Name |
|---------|------|
| `0x80009a30` | `LMP__25C_called1` |
| `0x80009a6c` | `LMP__268__most_common_for_VSCs2_checks_fptr_patch` ← **key hook point** |
| `0x80009ac8` | `LMP__25B__most_common_for_VSCs1` |
| `0x80011d9c` | `LMP_CH__0x3ee__case2_else_2` |
| `0x80011e10` | `LMP_CH__0x3ee__case2_else_1` |
| `0x80011fc0` | `LMP_CH__0x3ee__case1_if` |
| `0x80022030` | `LMP__266__FUN_80022030` |
| `0x80025cb4` | `LMP__271__FUN_80025cb4` |
| `0x8001aa3c` | `LMP_QUALITY_OF_SERVICE_REQ_0x2A` |
| `0x8001af9c` | `LMP_0x18_LMP_UNSNIFF_REQ` |
| `0x8001b370` | `fHCI_Read_Remote_Version_Information_0x1D_send_LMP_VERSION_REQ_0x25` |
| `0x8001b4e8` | `fHCI_Read_Remote_Supported_Features_0x1B` |
| `0x8001b54c` | `fHCI_Remote_Name_Request_0x19_send_LMP_NAME_REQ_0x01` |
| `0x80026c38` | `LMP_ENCRYPTION_MODE_REQ_0x0F` |
| `0x80026e64` | `LMP_ENCRYPTION_KEY_SIZE_REQ_0x10_possibility2` |
| `0x80026f54` | `LMP_COMB_KEY_0x09` |
| `0x80027100` | `LMP_SRES_0x0C` |
| `0x80027454` | `LMP_IN_RAND_0x08` |
| `0x8002763c` | `LMP_AU_RAND_0x0B` |
| `0x80027de0` | `LMP_ENCRYPTION_KEY_SIZE_REQ_0x10` |
| `0x80027f30` | `LMP_ENCRYPTION_KEY_SIZE_MASK_RES_0x3B` |
| `0x80027f80` | `LMP_ENCRYPTION_KEY_SIZE_MASK_REQ_0x3A` |
| `0x80027fd4` | `LMP_STOP_ENCRYPTION_REQ_0x12` |
| `0x800281c4` | `LMP_NOT_ACCEPTED_0x04` |
| `0x80028264` | `LMP_encryption_opcode_handlers` |
| `0x800287b8` | `LMP_DHKEY_CHECK_0x41` |
| `0x80028904` | `wraps_LMP_DHKEY_CHECK_0x41` |
| `0x80028950` | `LMP_SIMPLE_PAIRING_NUMBER_0x40` |
| `0x80028bb8` | `LMP_SIMPLE_PAIRING_CONFIRM_0x3F` |
| `0x80028fc4` | `LMP_PAUSE_ENCRYPTION_AES_REQ_0x66` |
| `0x800297bc` | `LMP_USE_SEMI_PERMANENT_KEY_0x32` |
| `0x80029830` | `LMP_TEMP_KEY_0x0E` |
| `0x800298d0` | `LMP_TEMP_RAND_0x0D` |

### HCI Command Dispatchers
| Address | Name |
|---------|------|
| `0x80020814` | `HCI_CMD_OGF_01__Link_Control` |
| `0x8002060c` | `HCI_CMD_OGF_02__Link_Policy` |
| `0x800202c0` | `HCI_CMD_OGF_03__Controller_and_Baseband` |
| `0x80020188` | `HCI_CMD_OGF_05__Status_Parameters` |
| `0x800200a8` | `HCI_CMD_OGF_06__TestMode` |
| `0x80020ee0` | `assoc_w_tHCI_CMD` |
| `0x80020bec` | `assoc_w_tHCI_EVT` |

### HCI Command Handlers – OGF 1 (Link Control)
| Address | Name |
|---------|------|
| `0x8001bfa0` | `fHCI_Inquiry_0x01` |
| `0x8001bf44` | `fHCI_Periodic_Inquiry_Mode_0x03` |
| `0x8001bd38` | `fHCI_Create_Connection_0x05` |
| `0x8001b9d4` | `fHCI_Disconnect_0x06` |
| `0x8001b8fc` | `fHCI_Add_SCO_Connection_DEPRECATED_0x07` |
| `0x8001bbbc` | `fHCI_Accept_Connection_Request_0x09` |
| `0x8001baf8` | `fHCI_Reject_Connection_Request_0x0A` |
| `0x8001b84c` | `fHCI_Change_Connection_Packet_Type_0x0F` |
| `0x8001b2c0` | `fHCI_Read_Clock_Offset_0x1F` |
| `0x8001b23c` | `fHCI_Read_LMP_Handle_0x20` |
| `0x8001c7b4` | `fHCI_Truncated_Page_0x3F` |
| `0x8001c788` | `fHCI_Truncated_Page_Cancel_0x40` |

### HCI Command Handlers – OGF 3 (Controller/Baseband)
| Address | Name |
|---------|------|
| `0x8001e610` | `OGC_3_OCF_01` |
| `0x8001e6bc` | `OGC_3_OCF_13` |
| `0x8001e6fc` | `OGC_3_OCF_16` |
| `0x8001e72c` | `OGC_3_OCF_18` |
| `0x8001f230` | `OGC_3_OCF_08` |
| `0x8001f2ac` | `OGC_3_OCF_05` |
| `0x8001eb50` | `OGC_3_OCF_28` |
| `0x8001ebac` | `OGC_3_OCF_27` |
| `0x8001ecd8` | `OGC_3_OCF_26` |
| `0x8001ed98` | `OGC_3_OCF_24` |
| `0x8001ede4` | `OGC_3_OCF_1e` |
| `0x8001ee34` | `OGC_3_OCF_1c` |
| `0x8001eea4` | `OGC_3_OCF_1a` |
| `0x8001e780` | `HCI_Read_Loopback_Mode` |
| `0x8001e784` | `HCI_Enable_Device_Under_Test_Mode` |
| `0x8001ea34` | `HCI_Write_Loopback_Mode` |
| `0x8001e780` | `HCI_Write_Simple_Pairing_Debug_Mode` |
| `0x8001a294` | `OGC_3_OCF_0x52_HCI_Write_Extended_Inquiry_Response` |
| `0x80019c88` | `deal_with_OGF_3_OCF_0x3f-0x49` |
| `0x80019ad0` | `OGC_3_OCF_3f` |
| `0x80019b88` | `OGC_3_OCF_49` |
| `0x80019bac` | `OGC_3_OCF_45` |
| `0x80019bd0` | `OGC_3_OCF_47` |
| `0x80019bf4` | `OGC_3_default_func_0_OCF_0x3F_and_above` |

### Vendor-Specific Commands (VSC)
| Address | Name |
|---------|------|
| `0x80009148` | `VSC_0xfc11_3_in_while_loop` |
| `0x800120ac` | `VSC_0xfc11_2` |
| `0x80012c18` | `VSC_0xfc11_1` |
| `0x8000bd04` | `VSC_0xfc6c` |
| `0x8000bdb4` | `VSC_common_used_in_0xfc39` |
| `0x8000be84` | `VSC_0xfc39_1` |
| `0x80013074` | `VSC_0xfc39_2` |
| `0x8000fae8` | `VSC_0xfc39_wrapper` |
| `0x80009b1c` | `VSC_0xfc95_called2` |
| `0x80014054` | `VSC_0xfcc0` |
| `0x800148f0` | `VSC_0xfcc2` |
| `0x80014cf4` | `call_to_multi_VSC_e.g._0xfcc4` |

### HCI Infrastructure
| Address | Name |
|---------|------|
| `0x8001d070` | `hci_event_sender` |
| `0x80014180` | `called_on_every_HCI_CMD_via_fptr` |
| `0x80009f68` | `called_at_end_of_every_HCI_CMD_via_fptr` |
| `0x80016780` | `wrap_look_for_non_matching_bdaddr_bos_index_(free_connection_slot)` |
| `0x8001d1bc` | `send_evt_HCI_Hardware_Error` |
| `0x8001d1f8` | `send_evt_HCI_Connection_Complete` |
| `0x8001d5fc` | `send_evt_HCI_Disconnection_Complete` |
| `0x8001d804` | `send_evt_HCI_Connection_Request` |
| `0x8001da0c` | `send_evt_HCI_Inquiry_Complete` |
| `0x8001e5d8` | `send_evt_HCI_Command_Status` |
| `0x8001d424` | `called_by_fHCI_Read_LMP_Handle_send_evt_HCI_Command_Complete` |
| `0x8001d5b4` | `send_evt_0x14_HCI_Mode_Change` |
| `0x80019e4c` | `send_evt_HCI_Read_Remote_Extended_Features_Complete` |
| `0x80019e88` | `send_evt_HCI_Synchronous_Connection_Changed` |
| `0x80019f0c` | `send_evt_HCI_Synchronous_Connection_Complete` |
| `0x80018e58` | `send_HCI_Command_Status_for_HCI_0x0A` |
| `0x80019594` | `send_HCI_Command_Status_for_HCI_0x09` |
| `0x80019830` | `send_HCI_Command_Status_for_HCI_0x07` |

### Logging / Debug
| Address | Name |
|---------|------|
| `0x80008d18` | `log_many_2_0x72_0x121-0x14e` |
| `0x800098d8` | `possible_logger_called_if_no_patch3` |
| `0x8000f53c` | `wraps_multi_VSC_called_if_no_patch3` |
| `0x8001574c` | `send_evt_invalid_0xFF` |
| `0x800157b8` | `calls_send_evt_invalid_0xFF_0_or_1` |

### Misc / Unknown
| Address | Name |
|---------|------|
| `0x80003d10` | `func2_that_uses_structs_at_0x80100000` |
| `0x80009990` | `interesting_string_user_fptr_registration_function` |
| `0x80009be4` | `call_fptr_if_set_with_2_args_possibly_allocates_buf_at_arg2` |
| `0x80009cc0` | `reg_multiple_dptrs` |
| `0x80021ab0` | `interesting_string_user_FUN_80021ab0` |
| `0x80021ba0` | `calls_reg_multiple_dptrs` |
| `0x80021c9c` | `calls_interesting_string_user` |
| `0x80013474` | `return_1` |
| `0x80018c14` | `ret_wrapper` |

---

## Key Architectural Notes

1. **Boot flow**: ROM at `0x800109ac` (`calls_to_0x8010a001_as_fptr_to_install_patches`) calls `0x8010a001` — the patch firmware entry point. This is how the patch is invoked after download.

2. **LMP hook point**: `LMP__268__most_common_for_VSCs2_checks_fptr_patch` (`0x80009a6c`) checks a function pointer that the patch installs. This is the VSC dispatch hook.

3. **Patch installer**: `thing_that_calls_thing_that_installs_LMP_Patch` (`0x00003780` / `0x80103780`) sets up the LMP/VSC function pointer hooks in ROM structures.

4. **"bos" struct**: 12 connection-state entries at RAM address `0x8012dc50`. ROM functions use this for per-connection state.

5. **Config**: `copies_config_bdaddr` (`0x8000fd38`) reads BD address from the config blob appended after the firmware binary.
