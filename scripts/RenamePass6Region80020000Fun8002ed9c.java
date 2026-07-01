// Rename FUN_8002ed9c -> hci_evt_buffer_fptr_dispatch_mode0_forward
// Pass 6 continuation (282), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002ed9c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002ed9c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002ed9c");
            return;
        }
        String oldName = f.getName();
        f.setName("hci_evt_buffer_fptr_dispatch_mode0_forward",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}