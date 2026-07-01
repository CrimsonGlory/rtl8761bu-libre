// Rename FUN_8002f39c -> thunk_dispatch_hci_td_connection_event_side_effects
// Pass 6 continuation (296), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002f39c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002f39c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002f39c");
            return;
        }
        String oldName = f.getName();
        f.setName("thunk_dispatch_hci_td_connection_event_side_effects",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}