// RenamePass12fkRegion80070000Fun800720c4.java — Pass 12fk HCI role-change apply/defer
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12fkRegion80070000Fun800720c4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800720c4L;
        String newName = "apply_or_defer_conn_role_change_emit_hci_evt_sync";
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
