// Rename FUN_8003cf9c -> select_max_dword_parallel_slot_type_0x101_store_byte_at_plus6
// Pass 262, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass262Region80030000Fun8003cf9c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003cf9c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003cf9c");
            return;
        }
        String oldName = f.getName();
        f.setName("select_max_dword_parallel_slot_type_0x101_store_byte_at_plus6",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}