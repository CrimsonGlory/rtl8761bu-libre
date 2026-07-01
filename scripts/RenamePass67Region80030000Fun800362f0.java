// Rename FUN_800362f0 -> compute_lmp_slot_offset_and_program_hw_by_conn_cc_index
// Pass 67, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass67Region80030000Fun800362f0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800362f0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800362f0");
            return;
        }
        String oldName = f.getName();
        f.setName("compute_lmp_slot_offset_and_program_hw_by_conn_cc_index",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}