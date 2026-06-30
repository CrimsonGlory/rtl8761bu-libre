// RenamePass12hdRegion80070000Fun80075fd0.java — Pass 12hd resource pool freelist pop stub
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12hdRegion80070000Fun80075fd0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80075fd0L;
        String newName = "pop_head_from_resource_pool_64_slot_freelist";
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