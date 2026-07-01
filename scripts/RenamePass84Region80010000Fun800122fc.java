// RenamePass84Region80010000Fun800122fc.java — Pass 84 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass84Region80010000Fun800122fc extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800122fcL;
        String newName = "apply_config_status_feature_bits_to_baseband_registers";
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