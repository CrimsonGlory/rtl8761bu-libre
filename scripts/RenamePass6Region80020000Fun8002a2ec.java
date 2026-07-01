// Rename FUN_8002a2ec -> drain_acl_reassembly_slots_0_through_2_and_clear_flags
// Pass 6 continuation (184), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002a2ec extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002a2ec");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002a2ec");
            return;
        }
        String oldName = f.getName();
        f.setName("drain_acl_reassembly_slots_0_through_2_and_clear_flags",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}