// RenamePass12clRegion80070000Fun80075428.java — Pass 12cl ISR/timer resource init
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12clRegion80070000Fun80075428 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80075428L;
        String newName = "init_isr_extended_and_crypto_timer_resources";
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
