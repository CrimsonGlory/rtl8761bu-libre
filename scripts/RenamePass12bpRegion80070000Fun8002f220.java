// RenamePass12bpRegion80070000Fun8002f220.java — Pass 12bp LMP TX hook dispatch
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12bpRegion80070000Fun8002f220 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x8002f220L;
        String newName = "invoke_lmp_tx_hook_with_length_word_from_pdu_buffer";
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
