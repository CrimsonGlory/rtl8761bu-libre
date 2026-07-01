// Rename FUN_8002ed70 -> hci_evt_buffer_fptr_dispatch_mode2_computed_ushort_range
// Pass 6 continuation (255), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002ed70 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002ed70");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002ed70");
            return;
        }
        String oldName = f.getName();
        f.setName("hci_evt_buffer_fptr_dispatch_mode2_computed_ushort_range",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}