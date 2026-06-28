// RenamePass54abFun80052ffc.java — Pass 54ab cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54abFun80052ffc extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80052ffcL;
        String newName = "maybe_send_le_meta_subevent_on_slot_match";
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
