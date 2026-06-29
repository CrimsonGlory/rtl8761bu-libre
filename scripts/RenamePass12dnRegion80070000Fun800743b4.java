// RenamePass12dnRegion80070000Fun800743b4.java — Pass 12dn indexed-table range validator
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12dnRegion80070000Fun800743b4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800743b4L;
        String newName = "match_28byte_indexed_table_by_type_and_6byte_key_with_range";
        Function fn = getFunctionAt(toAddr(addr));
        if (fn == null) {
            println("MISSED at 0x" + Long.toHexString(addr));
            return;
        }
        String old = fn.getName();
        fn.setName(newName, SourceType.USER_DEFINED);
        println("renamed=1 old=" + old + " new=" + newName);
    }
}
