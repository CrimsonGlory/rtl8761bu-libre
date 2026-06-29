// RenamePass54avFun8005fdd0.java — Pass 54av cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54avFun8005fdd0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8005fdd0L;
        String newName = "validate_and_commit_esco_packet_type_params_or_reject";
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
