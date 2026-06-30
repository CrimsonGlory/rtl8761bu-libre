// Rename FUN_8002db50 -> crypto_ec_affine_to_jacobian_mod_curve_prime
// Pass 6 continuation (8), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002db50 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002db50");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002db50");
            return;
        }
        String oldName = f.getName();
        f.setName("crypto_ec_affine_to_jacobian_mod_curve_prime",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}