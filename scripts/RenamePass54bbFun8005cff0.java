// RenamePass54bbFun8005cff0.java — Pass 54bb cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54bbFun8005cff0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8005cff0L;
        String newName = "dispatch_hook0x1000_and_meta_evt_0x17_if_adv_high_bits_clear";
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
