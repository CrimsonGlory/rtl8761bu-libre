// Rename FUN_8002c7d0 -> compute_ssp_confirm_hash_hmac_variable_blocks
// Pass 6 continuation (127), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002c7d0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002c7d0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002c7d0");
            return;
        }
        String oldName = f.getName();
        f.setName("compute_ssp_confirm_hash_hmac_variable_blocks",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}