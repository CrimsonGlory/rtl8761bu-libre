// RenamePass12anRegion80070000Fun80076974.java — Pass 12an bignum right-shift one bit
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12anRegion80070000Fun80076974 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80076974L;
        String newName = "crypto_bignum_right_shift_u32_array_by_one_bit";
        Function fn = getFunctionAt(toAddr(addr));
        if (fn == null) {
            println("MISSING at 0x" + Long.toHexString(addr));
            return;
        }
        String old = fn.getName();
        fn.setName(newName, SourceType.USER_DEFINED);
        println("renamed=1 old=" + old + " new=" + newName);
    }
}
