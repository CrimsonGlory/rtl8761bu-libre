// Rename FUN_8002c6c8 -> assemble_63byte_hmac_and_compute_safer_hash
// Pass 6 continuation (78), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002c6c8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002c6c8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002c6c8");
            return;
        }
        String oldName = f.getName();
        f.setName("assemble_63byte_hmac_and_compute_safer_hash",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}