// Rename FUN_8002d464 -> crypto_bignum_mod_inverse_mod_curve_prime
// Pass 6 continuation (4), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002d464 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002d464");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002d464");
            return;
        }
        String oldName = f.getName();
        f.setName("crypto_bignum_mod_inverse_mod_curve_prime",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}