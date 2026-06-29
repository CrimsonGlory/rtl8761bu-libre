// RenamePass12bxRegion80070000Fun80071f60.java — Pass 12bx LMP preferred-rate payload encoder
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12bxRegion80070000Fun80071f60 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80071f60L;
        String newName = "encode_lmp_preferred_rate_payload_byte";
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
