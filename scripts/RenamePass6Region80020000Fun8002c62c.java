// Rename FUN_8002c62c -> hmac_ipad_opad_2pass_safer_hash_driver
// Pass 6 continuation (65), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002c62c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002c62c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002c62c");
            return;
        }
        String oldName = f.getName();
        f.setName("hmac_ipad_opad_2pass_safer_hash_driver",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}