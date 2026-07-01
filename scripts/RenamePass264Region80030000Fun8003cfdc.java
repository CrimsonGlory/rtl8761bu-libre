// Rename FUN_8003cfdc -> select_min_dword_parallel_slot_index_type_0x101
// Pass 264, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass264Region80030000Fun8003cfdc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003cfdc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003cfdc");
            return;
        }
        String oldName = f.getName();
        f.setName("select_min_dword_parallel_slot_index_type_0x101",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}