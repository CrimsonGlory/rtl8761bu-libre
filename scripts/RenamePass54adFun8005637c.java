// RenamePass54adFun8005637c.java — Pass 54ad cold-triage rank-1 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54adFun8005637c extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8005637cL;
        String newName = "set_hw_control_flag_bit6";
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
