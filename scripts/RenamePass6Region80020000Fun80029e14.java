// Rename FUN_80029e14 -> derive_master_link_key_hci_event_0x0e_via_safer_plus_xor
// Pass 6 continuation (150), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80029e14 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80029e14");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80029e14");
            return;
        }
        String oldName = f.getName();
        f.setName("derive_master_link_key_hci_event_0x0e_via_safer_plus_xor",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}