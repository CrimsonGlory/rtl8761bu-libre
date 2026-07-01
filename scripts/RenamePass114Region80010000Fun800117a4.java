// RenamePass114Region80010000Fun800117a4.java — Pass 114 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass114Region80010000Fun800117a4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800117a4L;
        String newName = "or_global_afh_mask_bits_fc00";
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