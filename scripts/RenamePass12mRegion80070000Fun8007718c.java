// RenamePass12mRegion80070000Fun8007718c.java — Pass 12m BB slot / eSCO orchestrator
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12mRegion80070000Fun8007718c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8007718cL;
        String newName = "eSCO_SCO_connection_slave_establishment_orchestrator";
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
