// RenamePass12bhRegion80070000Fun80079634.java — Pass 12bh codec serialize tail hook
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12bhRegion80070000Fun80079634 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80079634L;
        String newName = "invoke_codec_serialize_tail_patch_hook_by_mode_count";
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
