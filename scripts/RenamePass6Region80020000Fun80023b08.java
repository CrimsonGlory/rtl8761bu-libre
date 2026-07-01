// Rename FUN_80023b08 -> hci_resolve_conn_ssp_pairing_continuation_lmp_ext_0x19_auth_failure
// Pass 6 continuation (226), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80023b08 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80023b08");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80023b08");
            return;
        }
        String oldName = f.getName();
        f.setName("hci_resolve_conn_ssp_pairing_continuation_lmp_ext_0x19_auth_failure",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}