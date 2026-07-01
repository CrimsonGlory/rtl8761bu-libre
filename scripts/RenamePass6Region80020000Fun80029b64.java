// Rename FUN_80029b64 -> decay_link_key_transition_counters_and_wrap_emit_hci_evt_if_bdaddr_random
// Pass 6 continuation (135), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80029b64 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80029b64");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80029b64");
            return;
        }
        String oldName = f.getName();
        f.setName("decay_link_key_transition_counters_and_wrap_emit_hci_evt_if_bdaddr_random",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}