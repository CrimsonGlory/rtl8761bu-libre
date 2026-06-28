// RenamePass54ahFun80058a34.java — Pass 54ah cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54ahFun80058a34 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80058a34L;
        String newName = "init_global_queue_descriptor_with_count_11";
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
