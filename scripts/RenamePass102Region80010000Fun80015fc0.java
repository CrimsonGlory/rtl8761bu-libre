// RenamePass102Region80010000Fun80015fc0.java — Pass 102 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass102Region80010000Fun80015fc0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80015fc0L;
        String newName = "feature_bit0_gated_vsc_fc95_lmp268_gateway_secondary_cfg_with_scaled_delay";
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