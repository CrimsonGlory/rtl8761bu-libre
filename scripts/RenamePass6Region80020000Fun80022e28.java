// Rename FUN_80022e28 -> send_evt_hci_pin_code_request_and_set_pairing_state
// Pass 6 continuation (242), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80022e28 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80022e28");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80022e28");
            return;
        }
        String oldName = f.getName();
        f.setName("send_evt_hci_pin_code_request_and_set_pairing_state",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}