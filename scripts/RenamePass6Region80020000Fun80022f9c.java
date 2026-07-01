// Rename FUN_80022f9c -> lookup_bdaddr_link_key_dispatch_auth_or_request_hci_key
// Pass 6 continuation (159), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80022f9c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80022f9c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80022f9c");
            return;
        }
        String oldName = f.getName();
        f.setName("lookup_bdaddr_link_key_dispatch_auth_or_request_hci_key",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}