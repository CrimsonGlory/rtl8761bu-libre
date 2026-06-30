// RenamePass12guRegion80070000Fun8007883c.java — Pass 12gu patch-hook gap noop stub
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12guRegion80070000Fun8007883c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8007883cL;
        String newName = "noop_unused_patch_hook_slot_after_vsc_fc13_stub_gap";
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