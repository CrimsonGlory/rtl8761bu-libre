// RenamePass80Region80010000Fun80013e2c.java — Pass 80 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass80Region80010000Fun80013e2c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80013e2cL;
        String newName = "read_codec_table_entry_and_wait_ready";
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