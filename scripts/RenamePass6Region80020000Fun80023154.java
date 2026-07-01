// Rename FUN_80023154 -> hci_resolve_conn_dispatch_pairing_continuation_link_key_neg_reply_0xc
// Pass 6 continuation (250), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80023154 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80023154");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80023154");
            return;
        }
        String oldName = f.getName();
        f.setName("hci_resolve_conn_dispatch_pairing_continuation_link_key_neg_reply_0xc",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}