// RenamePass12ajRegion80070000Fun80071138.java — Pass 12aj LMP accept/mirror connection handler
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12ajRegion80070000Fun80071138 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80071138L;
        String newName = "LMP_accept_or_mirror_connection_handler";
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
