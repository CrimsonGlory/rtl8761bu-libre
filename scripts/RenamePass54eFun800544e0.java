// RenamePass54eFun800544e0.java — Pass 54e cold-triage rank-2 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54eFun800544e0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800544e0L;
        String newName = "build_linked_conn_param_buffers_and_schedule_link_timing_setup";
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
