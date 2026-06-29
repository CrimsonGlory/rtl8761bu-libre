// RenamePass12dqRegion80070000Fun80074c04.java — Pass 12dq feature-page logger buffer
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12dqRegion80070000Fun80074c04 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80074c04L;
        String newName = "pack_and_log_event_buf_ff_05_22_tag_0x70190";
        Function fn = getFunctionAt(toAddr(addr));
        if (fn == null) {
            println("MISSED at 0x" + Long.toHexString(addr));
            return;
        }
        String old = fn.getName();
        fn.setName(newName, SourceType.USER_DEFINED);
        println("renamed=1 old=" + old + " new=" + newName);
    }
}
