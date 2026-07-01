// Rename FUN_8002d378 -> crypto_bignum_subtract_dual_curve_constants_by_key_size_index
// Pass 6 continuation (154), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002d378 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002d378");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002d378");
            return;
        }
        String oldName = f.getName();
        f.setName("crypto_bignum_subtract_dual_curve_constants_by_key_size_index",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}