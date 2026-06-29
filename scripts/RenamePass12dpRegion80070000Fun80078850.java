// RenamePass12dpRegion80070000Fun80078850.java — Pass 12dp BB reg enable/disable writer
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12dpRegion80070000Fun80078850 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80078850L;
        String newName = "program_bb_regs_0x1a6_0xf4_enable_disable_via_patch_hook";
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
