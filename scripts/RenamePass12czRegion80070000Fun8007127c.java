// RenamePass12czRegion80070000Fun8007127c.java — Pass 12cz inquiry-result HCI emitter
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12czRegion80070000Fun8007127c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8007127cL;
        String newName = "emit_hci_inquiry_result_or_extended_and_maybe_complete";
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
