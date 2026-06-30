// Rename FUN_80021754 -> resolve_connection_policy_priority_by_bdaddr_or_bitmask
// Pass 6 continuation (40), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80021754 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80021754");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80021754");
            return;
        }
        String oldName = f.getName();
        f.setName("resolve_connection_policy_priority_by_bdaddr_or_bitmask",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}