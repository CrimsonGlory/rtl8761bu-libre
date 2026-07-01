// RenamePass113Region80010000Fun80013bcc.java — Pass 113 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass113Region80010000Fun80013bcc extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80013bccL;
        String newName = "read_packet_type_table_high_byte_at_offset";
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