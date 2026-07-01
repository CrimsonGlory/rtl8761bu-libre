// Rename FUN_800236a0 -> hci_resolve_conn_fhci_user_confirmation_request_reply_0x33
// Pass 6 continuation (249), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800236a0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800236a0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800236a0");
            return;
        }
        String oldName = f.getName();
        f.setName("hci_resolve_conn_fhci_user_confirmation_request_reply_0x33",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}