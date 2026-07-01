// Rename FUN_8003cf28 -> reset_parallel_slot_table_entry_tail_state_by_index
// Pass 68, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass68Region80030000Fun8003cf28 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003cf28");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003cf28");
            return;
        }
        String oldName = f.getName();
        f.setName("reset_parallel_slot_table_entry_tail_state_by_index",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}