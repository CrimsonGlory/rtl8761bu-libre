// RenamePass12dRegion80070000Fun80076c50.java — Pass 12d mid-BB-init patch hook
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12dRegion80070000Fun80076c50 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80076c50L;
        String newName = "invoke_optional_patch_fptr_mid_bb_hw_init";
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
