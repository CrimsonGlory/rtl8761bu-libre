// RenamePass12gsRegion80070000Fun80078844.java — Pass 12gs patch-hook gap noop stub
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12gsRegion80070000Fun80078844 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80078844L;
        String newName = "noop_unused_patch_hook_slot_before_role_switch_tail";
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