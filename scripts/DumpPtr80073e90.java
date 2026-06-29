// Dump ROM dword at PTR_DAT_80073e90 and any function at that address.
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;

public class DumpPtr80073e90 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long slot = 0x80073e90L;
        int val = getInt(toAddr(slot));
        println("PTR_SLOT=0x" + Long.toHexString(slot));
        println("PTR_VALUE=0x" + Integer.toHexString(val));
        Function fnAtSlot = getFunctionAt(toAddr(slot));
        println("FN_AT_SLOT=" + (fnAtSlot == null ? "null" : fnAtSlot.getName()));
        Function fnAtTarget = getFunctionAt(toAddr(val & 0xffffffffL));
        println("FN_AT_TARGET=" + (fnAtTarget == null ? "null" : fnAtTarget.getName()));
    }
}
