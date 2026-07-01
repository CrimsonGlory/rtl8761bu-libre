// RenamePass117Region80010000Fun800172bc.java — Pass 117 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass117Region80010000Fun800172bc extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800172bcL;
        String newName = "init_SCO_sync_setup_state_apply_primary_hw_hooks_vsc_fc95_lmp268";
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