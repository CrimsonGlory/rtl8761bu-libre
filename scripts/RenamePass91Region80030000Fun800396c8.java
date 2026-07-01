// Rename FUN_800396c8 -> compute_timing_offset_from_table_base_step_and_index_div10
// Pass 91, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass91Region80030000Fun800396c8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800396c8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800396c8");
            return;
        }
        String oldName = f.getName();
        f.setName("compute_timing_offset_from_table_base_step_and_index_div10",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}