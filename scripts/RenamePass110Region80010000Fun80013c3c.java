// RenamePass110Region80010000Fun80013c3c.java — Pass 110 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass110Region80010000Fun80013c3c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80013c3cL;
        String newName = "write_packet_type_table_bits_14_15_at_global_ptr";
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