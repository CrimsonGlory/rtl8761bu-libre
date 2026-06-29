// RenamePass12bbRegion80070000Fun80076c08.java — Pass 12bb bignum reverse byte copy
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12bbRegion80070000Fun80076c08 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80076c08L;
        String newName = "crypto_copy_u8_array_reversed_to_dest";
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
