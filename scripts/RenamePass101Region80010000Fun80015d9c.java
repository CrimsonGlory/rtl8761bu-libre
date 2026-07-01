// RenamePass101Region80010000Fun80015d9c.java — Pass 101 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass101Region80010000Fun80015d9c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80015d9cL;
        String newName = "feature_bit0_gated_vsc_fc95_lmp268_gateway_with_config_scaled_delay";
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