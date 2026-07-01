// Rename FUN_80029e78 -> send_master_link_key_hci_event_0x0d_from_template
// Pass 6 continuation (220), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80029e78 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80029e78");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80029e78");
            return;
        }
        String oldName = f.getName();
        f.setName("send_master_link_key_hci_event_0x0d_from_template",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}