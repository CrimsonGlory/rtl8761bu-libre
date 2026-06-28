// RenamePass54jFun8005c214.java — Pass 54j cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54jFun8005c214 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8005c214L;
        String newName = "compare_afh_wrapped_delta_and_stage_channel_entry";
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
