// RenamePass12alRegion80070000Fun8007223c.java — Pass 12al LMP preferred-rate sender
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12alRegion80070000Fun8007223c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8007223cL;
        String newName = "maybe_send_lmp_preferred_rate_0x24_pdu";
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
