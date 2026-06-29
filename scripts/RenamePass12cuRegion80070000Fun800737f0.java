// RenamePass12cuRegion80070000Fun800737f0.java — Pass 12cu conn link-loss teardown
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12cuRegion80070000Fun800737f0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800737f0L;
        String newName = "conn_link_loss_teardown_unsniff_or_lmp_detach";
        Function fn = getFunctionAt(toAddr(addr));
        if (fn == null) {
            println("MISSED at 0x" + Long.toHexString(addr));
            return;
        }
        String old = fn.getName();
        fn.setName(newName, SourceType.USER_DEFINED);
        println("renamed=1 old=" + old + " new=" + newName);
    }
}
