// Rename FUN_8002f378 -> hci_evt_buffer_mode2_dispatch_offset4_u16_from_buf_plus2
// Pass 6 continuation (260), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002f378 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002f378");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002f378");
            return;
        }
        String oldName = f.getName();
        f.setName("hci_evt_buffer_mode2_dispatch_offset4_u16_from_buf_plus2",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}