// Rename FUN_8002dfd4 -> crypto_bignum_reduce_mod_curve_prime_by_limb_count
// Pass 6 continuation (246), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002dfd4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002dfd4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002dfd4");
            return;
        }
        String oldName = f.getName();
        f.setName("crypto_bignum_reduce_mod_curve_prime_by_limb_count",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}