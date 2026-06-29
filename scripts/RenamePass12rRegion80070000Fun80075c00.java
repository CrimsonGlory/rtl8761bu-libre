// RenamePass12rRegion80070000Fun80075c00.java — Pass 12r pool subdescriptor clear
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12rRegion80070000Fun80075c00 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80075c00L;
        String newName = "clear_pool_subdescriptor_backing_and_invalidate_state";
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
