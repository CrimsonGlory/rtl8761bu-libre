// RenamePass12bkRegion80070000Fun800745d8.java — Pass 12bk TLV tag-name table matcher
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12bkRegion80070000Fun800745d8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800745d8L;
        String newName = "walk_tlv_stream_match_tag_name_in_indexed_table";
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
