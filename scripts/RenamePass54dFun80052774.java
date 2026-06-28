// RenamePass54dFun80052774.java — Pass 54d cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54dFun80052774 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80052774L;
        String newName = "transfer_or_emit_conn_negotiation_state_at_field0x14";
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
