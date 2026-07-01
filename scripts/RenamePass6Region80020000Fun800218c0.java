// Rename FUN_800218c0 -> dispatch_connection_policy_match_or_priority_by_mode
// Pass 6 continuation (233), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800218c0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800218c0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800218c0");
            return;
        }
        String oldName = f.getName();
        f.setName("dispatch_connection_policy_match_or_priority_by_mode",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}