// Rename FUN_80029680 -> find_random_bdaddr_encrypted_link_slot_for_link_key_evt
// Pass 6 continuation (172), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80029680 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80029680");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80029680");
            return;
        }
        String oldName = f.getName();
        f.setName("find_random_bdaddr_encrypted_link_slot_for_link_key_evt",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}