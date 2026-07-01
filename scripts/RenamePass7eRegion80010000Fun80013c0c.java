// RenamePass7eRegion80010000Fun80013c0c.java — Pass 7e cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass7eRegion80010000Fun80013c0c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80013c0cL;
        String newName = "write_packet_type_table_low_byte_at_offset";
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