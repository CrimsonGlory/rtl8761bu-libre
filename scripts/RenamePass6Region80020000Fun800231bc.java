// Rename FUN_800231bc -> hci_cmd_connection_handle_lookup_failed
// Pass 6 continuation (277), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800231bc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800231bc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800231bc");
            return;
        }
        String oldName = f.getName();
        f.setName("hci_cmd_connection_handle_lookup_failed",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}