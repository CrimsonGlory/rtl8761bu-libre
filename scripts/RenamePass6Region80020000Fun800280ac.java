// Rename FUN_800280ac -> handle_lmp_encryption_mode_req_not_accepted
// Pass 6 continuation (28), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800280ac extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800280ac");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800280ac");
            return;
        }
        String oldName = f.getName();
        f.setName("handle_lmp_encryption_mode_req_not_accepted",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}