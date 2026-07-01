// Rename FUN_8003b2fc -> fill_sco_bb_register_pair_buffer_from_index_table
// Pass 219, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass219Region80030000Fun8003b2fc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003b2fc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003b2fc");
            return;
        }
        String oldName = f.getName();
        f.setName("fill_sco_bb_register_pair_buffer_from_index_table",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}