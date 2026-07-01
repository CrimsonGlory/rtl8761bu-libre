// Rename FUN_8002ee54 -> relay_lc_tx_subcase3_hci_evt_0x454_and_completed_packets
// Pass 6 continuation (236), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002ee54 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002ee54");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002ee54");
            return;
        }
        String oldName = f.getName();
        f.setName("relay_lc_tx_subcase3_hci_evt_0x454_and_completed_packets",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}