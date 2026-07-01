// Rename FUN_80025318 -> memcmp_computed_link_key_against_stored_bdaddr_aware
// Pass 6 continuation (168), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80025318 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80025318");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80025318");
            return;
        }
        String oldName = f.getName();
        f.setName("memcmp_computed_link_key_against_stored_bdaddr_aware",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}