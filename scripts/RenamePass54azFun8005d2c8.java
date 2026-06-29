// RenamePass54azFun8005d2c8.java — Pass 54az cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54azFun8005d2c8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8005d2c8L;
        String newName = "commit_conn_byte7_or_dispatch_lmp25b_and_log_param_0x26f";
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
