// Rename FUN_80021a04 -> init_eleven_pool_slots_via_call_fptr_if_set_wraps_pool_slot_init_and_zero
// Pass 6 continuation (136), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80021a04 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80021a04");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80021a04");
            return;
        }
        String oldName = f.getName();
        f.setName("init_eleven_pool_slots_via_call_fptr_if_set_wraps_pool_slot_init_and_zero",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}