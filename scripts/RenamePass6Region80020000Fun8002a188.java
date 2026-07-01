// Rename FUN_8002a188 -> dispatch_master_link_key_hci_phase_per_random_bdaddr_slot
// Pass 6 continuation (169), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002a188 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002a188");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002a188");
            return;
        }
        String oldName = f.getName();
        f.setName("dispatch_master_link_key_hci_phase_per_random_bdaddr_slot",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}