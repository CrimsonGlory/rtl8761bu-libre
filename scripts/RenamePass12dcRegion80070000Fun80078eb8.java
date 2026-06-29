// RenamePass12dcRegion80070000Fun80078eb8.java — Pass 12dc LE conn-param template filler
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12dcRegion80070000Fun80078eb8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80078eb8L;
        String newName = "fill_le_conn_param_defaults_by_profile_byte";
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
