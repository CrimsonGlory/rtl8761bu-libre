// RenamePass7zRegion80010000Fun800122b8.java — Pass 7z cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass7zRegion80010000Fun800122b8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800122b8L;
        String newName = "dispatch_afh_cap_param_to_bb_register_clear_loop";
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