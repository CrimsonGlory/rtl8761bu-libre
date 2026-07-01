// RenamePass105Region80010000Fun80014770.java — Pass 105 cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass105Region80010000Fun80014770 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80014770L;
        String newName = "program_key_block_into_codec_table_masked";
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