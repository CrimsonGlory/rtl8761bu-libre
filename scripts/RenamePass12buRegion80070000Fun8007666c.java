// RenamePass12buRegion80070000Fun8007666c.java — Pass 12bu crypto fptr dispatcher
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12buRegion80070000Fun8007666c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8007666cL;
        String newName = "invoke_crypto_state_machine_if_tag_200";
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
