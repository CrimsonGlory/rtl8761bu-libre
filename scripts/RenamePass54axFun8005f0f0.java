// RenamePass54axFun8005f0f0.java — Pass 54ax cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54axFun8005f0f0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8005f0f0L;
        String newName = "handle_connection_ef_substate_2_or_3_advance_to_4_or_reject";
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
