// RenamePass7kRegion80010000Fun80014dac.java — Pass 7k cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass7kRegion80010000Fun80014dac extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80014dacL;
        String newName = "apply_esco_codec_config_via_hw_register_programmer";
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