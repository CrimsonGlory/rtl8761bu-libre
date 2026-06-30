// Rename FUN_8002ccac -> crypto_bignum_sub_u8_byte_arrays_to_dest
// Pass 6 continuation (41), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002ccac extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002ccac");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002ccac");
            return;
        }
        String oldName = f.getName();
        f.setName("crypto_bignum_sub_u8_byte_arrays_to_dest",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}