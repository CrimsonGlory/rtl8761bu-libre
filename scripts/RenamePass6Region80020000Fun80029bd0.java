// Rename FUN_80029bd0 -> emit_hci_link_key_type_changed_or_lmp_detach_on_global_state_3
// Pass 6 continuation (94), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80029bd0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80029bd0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80029bd0");
            return;
        }
        String oldName = f.getName();
        f.setName("emit_hci_link_key_type_changed_or_lmp_detach_on_global_state_3",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}