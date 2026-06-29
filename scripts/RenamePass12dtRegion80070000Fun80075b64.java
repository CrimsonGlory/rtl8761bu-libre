// RenamePass12dtRegion80070000Fun80075b64.java — Pass 12dt pool index-stack push
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12dtRegion80070000Fun80075b64 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80075b64L;
        String newName = "push_tag_to_pool_descriptor_index_stack_or_fail";
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
