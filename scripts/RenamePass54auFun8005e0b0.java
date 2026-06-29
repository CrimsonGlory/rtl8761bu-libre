// RenamePass54auFun8005e0b0.java — Pass 54au cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54auFun8005e0b0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8005e0b0L;
        String newName = "validate_and_commit_timing_uint16_pairs_hook2_or_reject";
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
