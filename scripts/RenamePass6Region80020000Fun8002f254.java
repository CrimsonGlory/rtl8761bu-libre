// Rename FUN_8002f254 -> invoke_lc_tx_hook_with_hci_evt_payload_when_sco_active
// Pass 6 continuation (29), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002f254 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002f254");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002f254");
            return;
        }
        String oldName = f.getName();
        f.setName("invoke_lc_tx_hook_with_hci_evt_payload_when_sco_active",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}