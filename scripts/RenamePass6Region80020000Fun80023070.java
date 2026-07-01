// Rename FUN_80023070 -> hci_resolve_conn_dispatch_ssp_io_cap_or_oob_negative_reply_0xe
// Pass 6 continuation (251), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80023070 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80023070");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80023070");
            return;
        }
        String oldName = f.getName();
        f.setName("hci_resolve_conn_dispatch_ssp_io_cap_or_oob_negative_reply_0xe",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}