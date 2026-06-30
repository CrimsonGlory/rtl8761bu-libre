// RenamePass12heRegion80070000Fun80075bd4.java — Pass 12he pool descriptor index sign-bit stub
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12heRegion80070000Fun80075bd4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80075bd4L;
        String newName = "is_pool_descriptor_stack_index_negative";
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