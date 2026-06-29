// RenamePass54arFun8005e648.java — Pass 54ar cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54arFun8005e648 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8005e648L;
        String newName = "commit_esco_sco_8byte_params_and_alloc_tag9_subrecord";
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
