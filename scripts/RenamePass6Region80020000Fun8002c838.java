// Rename FUN_8002c838 -> advance_global_sha_blake_prng_state_16byte
// Pass 6 continuation (177), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002c838 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002c838");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002c838");
            return;
        }
        String oldName = f.getName();
        f.setName("advance_global_sha_blake_prng_state_16byte",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}