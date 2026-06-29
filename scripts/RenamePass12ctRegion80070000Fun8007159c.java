// RenamePass12ctRegion80070000Fun8007159c.java — Pass 12ct role-switch pending commit
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12ctRegion80070000Fun8007159c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8007159cL;
        String newName = "commit_pending_role_switch_emit_hci_or_lmp_slot_offset";
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
