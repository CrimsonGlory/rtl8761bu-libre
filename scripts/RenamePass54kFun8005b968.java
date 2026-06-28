// RenamePass54kFun8005b968.java — Pass 54k cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54kFun8005b968 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8005b968L;
        String newName = "init_connection_tx_timing_triple_from_template_or_defaults";
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
