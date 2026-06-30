// Rename FUN_8002dda4 -> crypto_ec_validate_affine_point_on_curve_mod_prime
// Pass 6 continuation (9), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002dda4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002dda4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002dda4");
            return;
        }
        String oldName = f.getName();
        f.setName("crypto_ec_validate_affine_point_on_curve_mod_prime",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}