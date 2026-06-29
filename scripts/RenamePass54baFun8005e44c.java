// RenamePass54baFun8005e44c.java — Pass 54ba cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54baFun8005e44c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8005e44cL;
        String newName = "handle_connection_ee4_ef0_advance_to_ef3_hook4_or_reject";
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
