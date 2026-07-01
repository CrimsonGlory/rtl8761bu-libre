// Rename FUN_8002f454 -> hci_evt_try_extract_status_and_conn_handle_when_03_08_or_30
// Pass 6 continuation (261), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002f454 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002f454");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002f454");
            return;
        }
        String oldName = f.getName();
        f.setName("hci_evt_try_extract_status_and_conn_handle_when_03_08_or_30",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}