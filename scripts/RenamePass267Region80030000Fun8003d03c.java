// Rename FUN_8003d03c -> init_parallel_slot_table_subslot_tail_state_by_index
// Pass 267, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass267Region80030000Fun8003d03c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003d03c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003d03c");
            return;
        }
        String oldName = f.getName();
        f.setName("init_parallel_slot_table_subslot_tail_state_by_index",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}