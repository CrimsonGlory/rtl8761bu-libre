// Rename FUN_8003b364 -> invoke_sco_bb_reg_hook_from_5entry_table_pack_7bit_and_flag_bits
// Pass 256, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass256Region80030000Fun8003b364 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003b364");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003b364");
            return;
        }
        String oldName = f.getName();
        f.setName("invoke_sco_bb_reg_hook_from_5entry_table_pack_7bit_and_flag_bits",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}