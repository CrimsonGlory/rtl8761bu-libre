// Rename FUN_80027ccc -> handle_lmp_encryption_key_size_req_not_accepted
// Pass 6 continuation (108), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80027ccc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80027ccc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80027ccc");
            return;
        }
        String oldName = f.getName();
        f.setName("handle_lmp_encryption_key_size_req_not_accepted",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}