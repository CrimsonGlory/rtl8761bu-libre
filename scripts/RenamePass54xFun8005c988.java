// RenamePass54xFun8005c988.java — Pass 54x cold-triage rank-2 rename
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass54xFun8005c988 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8005c988L;
        String newName = "init_stride7_codec_table_defaults_for_sco_hw_codes";
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
