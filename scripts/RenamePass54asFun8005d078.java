// RenamePass54asFun8005d078.java — Pass 54as cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54asFun8005d078 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8005d078L;
        String newName = "commit_8byte_link_params_hook10_and_send_meta_evt_0x04";
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
