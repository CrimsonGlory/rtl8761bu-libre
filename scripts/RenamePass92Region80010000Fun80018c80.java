// RenamePass92Region80010000Fun80018c80.java — Pass 92 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass92Region80010000Fun80018c80 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80018c80L;
        String newName = "pack_sync_conn_air_mode_capability_bits_from_feature_page_bytes";
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