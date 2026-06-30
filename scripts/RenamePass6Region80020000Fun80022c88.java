// Rename FUN_80022c88 -> apply_link_key_and_dispatch_auth_pairing_flow
// Pass 6 continuation (16), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80022c88 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80022c88");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80022c88");
            return;
        }
        String oldName = f.getName();
        f.setName("apply_link_key_and_dispatch_auth_pairing_flow",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}