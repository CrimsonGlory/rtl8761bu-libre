// Rename FUN_80025368 -> validate_stored_link_key_send_hci_notify_and_advance_state
// Pass 6 continuation (145), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80025368 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80025368");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80025368");
            return;
        }
        String oldName = f.getName();
        f.setName("validate_stored_link_key_send_hci_notify_and_advance_state",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}