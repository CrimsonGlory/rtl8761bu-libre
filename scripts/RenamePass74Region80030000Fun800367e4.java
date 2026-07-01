// Rename FUN_800367e4 -> recompute_and_commit_conn_slot_timing_hw_and_packet_types
// Pass 74, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass74Region80030000Fun800367e4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800367e4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800367e4");
            return;
        }
        String oldName = f.getName();
        f.setName("recompute_and_commit_conn_slot_timing_hw_and_packet_types",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}