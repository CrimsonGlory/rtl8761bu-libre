// Rename FUN_8003cf80 -> reset_parallel_slot_table_tail_states_slots_0_through_2
// Pass 228, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass228Region80030000Fun8003cf80 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003cf80");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003cf80");
            return;
        }
        String oldName = f.getName();
        f.setName("reset_parallel_slot_table_tail_states_slots_0_through_2",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}