// RenamePass12aeRegion80070000Fun80079808.java — Pass 12ae codec context serialize entry
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12aeRegion80070000Fun80079808 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80079808L;
        String newName = "serialize_codec_context_lsb_with_pre_hook_and_optional_tail";
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
