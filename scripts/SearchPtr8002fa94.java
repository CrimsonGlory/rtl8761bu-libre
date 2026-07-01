// Search ROM for pointer value 0x8002fa94
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.mem.Memory;

public class SearchPtr8002fa94 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long target = 0x8002fa94L;
        Memory mem = currentProgram.getMemory();
        Address start = toAddr(0x80020000L);
        Address end = toAddr(0x8002ffffL);
        Address cur = start;
        int n = 0;
        while (cur.compareTo(end) < 0) {
            try {
                int val = mem.getInt(cur);
                if ((val & 0xffffffffL) == target) {
                    Function fn = getFunctionContaining(cur);
                    String ctx = fn != null ? fn.getName() : "?";
                    println(String.format("ptr at 0x%08x ctx=%s", cur.getOffset(), ctx));
                    n++;
                }
            } catch (Exception e) { /* skip unreadable */ }
            cur = cur.add(4);
        }
        println("total_ptrs=" + n);
    }
}