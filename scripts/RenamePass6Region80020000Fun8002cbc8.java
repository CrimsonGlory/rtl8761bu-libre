// Rename FUN_8002cbc8 -> crypto_bignum_write_u8_bytes_at_bit_offset
// Pass 6 continuation (105), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002cbc8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002cbc8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002cbc8");
            return;
        }
        String oldName = f.getName();
        f.setName("crypto_bignum_write_u8_bytes_at_bit_offset",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}