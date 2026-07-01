// Rename FUN_8002384c -> hci_resolve_conn_fhci_user_passkey_request_reply_0x34
// Pass 6 continuation (248), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002384c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002384c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002384c");
            return;
        }
        String oldName = f.getName();
        f.setName("hci_resolve_conn_fhci_user_passkey_request_reply_0x34",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}