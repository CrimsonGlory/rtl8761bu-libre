// RenamePass54nFun80052458.java — Pass 54n cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54nFun80052458 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80052458L;
        String newName = "alloc_0x60_and_0x108_record_pools_from_config_and_wire";
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
