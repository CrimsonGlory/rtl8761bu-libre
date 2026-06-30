// Rename FUN_80029d60 -> start_hci_master_link_key_0x417_phase1_across_connections
// Pass 6 continuation (58), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80029d60 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80029d60");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80029d60");
            return;
        }
        String oldName = f.getName();
        f.setName("start_hci_master_link_key_0x417_phase1_across_connections",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}