// Rename FUN_8003d068 -> init_parallel_slot_table_entry_active_state_from_dual_index
// Pass 249, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass249Region80030000Fun8003d068 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003d068");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003d068");
            return;
        }
        String oldName = f.getName();
        f.setName("init_parallel_slot_table_entry_active_state_from_dual_index",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}