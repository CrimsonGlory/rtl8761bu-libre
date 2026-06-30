// Rename FUN_80028ec4 -> handle_lmp_not_accepted_for_start_encryption_req_0x18
// Pass 6 continuation (35), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80028ec4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80028ec4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80028ec4");
            return;
        }
        String oldName = f.getName();
        f.setName("handle_lmp_not_accepted_for_start_encryption_req_0x18",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}