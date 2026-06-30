// Rename FUN_8002d2a0 -> crypto_bignum_sub_u8_byte_arrays_in_place
// Pass 6 continuation (39), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002d2a0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002d2a0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002d2a0");
            return;
        }
        String oldName = f.getName();
        f.setName("crypto_bignum_sub_u8_byte_arrays_in_place",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}