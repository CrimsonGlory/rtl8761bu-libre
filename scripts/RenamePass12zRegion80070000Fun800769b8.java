// RenamePass12zRegion80070000Fun800769b8.java — Pass 12z bignum left-shift word merge
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12zRegion80070000Fun800769b8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800769b8L;
        String newName = "crypto_bignum_left_shift_words_into_dest";
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
