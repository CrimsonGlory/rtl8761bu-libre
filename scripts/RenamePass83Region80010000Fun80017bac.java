// RenamePass83Region80010000Fun80017bac.java — Pass 83 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass83Region80010000Fun80017bac extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80017bacL;
        String newName = "or_conn_status_byte_bits_via_role_remap";
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