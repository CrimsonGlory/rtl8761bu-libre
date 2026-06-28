// RenamePass54fFun80057684.java — Pass 54f cold-triage rank-3 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54fFun80057684 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80057684L;
        String newName = "merge_masked_high_halfword_into_link_register_for_slot";
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
