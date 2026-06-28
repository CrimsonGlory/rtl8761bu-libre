// RenamePass54qFun80051310.java — Pass 54q cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54qFun80051310 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80051310L;
        String newName = "set_esco_neg_pending_and_dispatch_or_send_event_0x6f";
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
