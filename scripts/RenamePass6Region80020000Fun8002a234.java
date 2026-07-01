// Rename FUN_8002a234 -> lookup_three_slot_0x34_acl_record_by_connection_handle
// Pass 6 continuation (223), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002a234 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002a234");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002a234");
            return;
        }
        String oldName = f.getName();
        f.setName("lookup_three_slot_0x34_acl_record_by_connection_handle",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}