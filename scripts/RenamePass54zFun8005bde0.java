// RenamePass54zFun8005bde0.java — Pass 54z cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54zFun8005bde0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8005bde0L;
        String newName = "init_0x58_stride_conn_record_ptr_table_11_slots";
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
