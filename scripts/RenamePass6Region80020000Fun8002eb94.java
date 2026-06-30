// Rename FUN_8002eb94 -> crypto_ec_dhkey_montgomery_ladder_init
// Pass 6 continuation (12), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002eb94 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002eb94");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002eb94");
            return;
        }
        String oldName = f.getName();
        f.setName("crypto_ec_dhkey_montgomery_ladder_init",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}