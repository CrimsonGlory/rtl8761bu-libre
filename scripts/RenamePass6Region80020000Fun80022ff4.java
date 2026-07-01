// Rename FUN_80022ff4 -> lookup_conn_index_from_hci_bdaddr_at_offset3
// Pass 6 continuation (292), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80022ff4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80022ff4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80022ff4");
            return;
        }
        String oldName = f.getName();
        f.setName("lookup_conn_index_from_hci_bdaddr_at_offset3",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}