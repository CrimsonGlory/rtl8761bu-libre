// RenamePass124Region80010000Fun80014ba8.java — Pass 124 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass124Region80010000Fun80014ba8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80014ba8L;
        String newName = "write_esco_slot_codec_table_quad_from_role_and_byte_block";
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