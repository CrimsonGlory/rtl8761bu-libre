// RenamePass54mFun8005537c.java — Pass 54m cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54mFun8005537c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8005537cL;
        String newName = "scan_indexed_link_slots_le_channel_select_and_lmp_vsc_dispatch";
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
