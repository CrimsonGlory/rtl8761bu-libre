// Rename FUN_8002cc40 -> crypto_bignum_bit_length_of_u8_byte_array
// Pass 6 continuation (197), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002cc40 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002cc40");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002cc40");
            return;
        }
        String oldName = f.getName();
        f.setName("crypto_bignum_bit_length_of_u8_byte_array",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}