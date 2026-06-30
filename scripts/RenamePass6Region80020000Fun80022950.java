// Rename FUN_80022950 -> hci_ogf1_ogf3_shared_command_complete_event_sender
// Pass 6 continuation (5), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80022950 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80022950");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80022950");
            return;
        }
        String oldName = f.getName();
        f.setName("hci_ogf1_ogf3_shared_command_complete_event_sender",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}