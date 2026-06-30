// Rename FUN_8002ee84 -> clone_hci_evt_buffer_and_dispatch_hci_evt_0x453_logger_hook
// Pass 6 continuation (81), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002ee84 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002ee84");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002ee84");
            return;
        }
        String oldName = f.getName();
        f.setName("clone_hci_evt_buffer_and_dispatch_hci_evt_0x453_logger_hook",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}