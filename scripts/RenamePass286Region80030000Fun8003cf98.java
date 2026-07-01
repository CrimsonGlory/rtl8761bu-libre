// Rename FUN_8003cf98 -> noop_void_stub_parallel_slot_table_cluster_jr_ra
// Pass 286, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass286Region80030000Fun8003cf98 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003cf98");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003cf98");
            return;
        }
        String oldName = f.getName();
        f.setName("noop_void_stub_parallel_slot_table_cluster_jr_ra",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}