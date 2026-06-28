// RenamePass54agFun8005cfcc.java — Pass 54ag cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54agFun8005cfcc extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8005cfccL;
        String newName = "advance_linked_queue_head_at_plus_0x80_and_notify";
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
