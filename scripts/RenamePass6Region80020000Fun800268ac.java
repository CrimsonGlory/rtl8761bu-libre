// Rename FUN_800268ac -> store_link_keys_in_global_slot_table
// Pass 6 continuation (119), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800268ac extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800268ac");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800268ac");
            return;
        }
        String oldName = f.getName();
        f.setName("store_link_keys_in_global_slot_table",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}