// Rename FUN_8003b3b4 -> lookup_sco_bb_reg_cached_ushort_from_5entry_index_table
// Pass 268, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass268Region80030000Fun8003b3b4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003b3b4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003b3b4");
            return;
        }
        String oldName = f.getName();
        f.setName("lookup_sco_bb_reg_cached_ushort_from_5entry_index_table",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}