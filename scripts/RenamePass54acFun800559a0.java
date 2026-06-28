// RenamePass54acFun800559a0.java — Pass 54ac cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54acFun800559a0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800559a0L;
        String newName = "maybe_invoke_le_channel_update_timing_if_pending";
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
