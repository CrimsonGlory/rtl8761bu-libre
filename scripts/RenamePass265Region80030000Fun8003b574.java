// Rename FUN_8003b574 -> invoke_bb_reg_0xda_writer_merge_global_low10_with_param_bits_4_15
// Pass 265, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass265Region80030000Fun8003b574 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003b574");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003b574");
            return;
        }
        String oldName = f.getName();
        f.setName("invoke_bb_reg_0xda_writer_merge_global_low10_with_param_bits_4_15",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}