// Rename FUN_8002b15c -> reinit_hci_cmd_list_clear_active_descriptor_tails_and_drain
// Pass 6 continuation (188), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002b15c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002b15c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002b15c");
            return;
        }
        String oldName = f.getName();
        f.setName("reinit_hci_cmd_list_clear_active_descriptor_tails_and_drain",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}