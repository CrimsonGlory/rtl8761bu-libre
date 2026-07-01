// RenamePass108Region80010000Fun80018dcc.java — Pass 108 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass108Region80010000Fun80018dcc extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80018dccL;
        String newName = "reject_lmp_sco_link_req_0x2b_not_accepted_and_cleanup";
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