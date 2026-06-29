// Pass 12fn: rename FUN_80074020 -> flush_link_sample_counters_and_emit_periodic_average_event
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12fnRegion80070000Fun80074020 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Function fn = getFunctionAt(toAddr(0x80074020L));
        if (fn == null) {
            println("MISSED at 0x80074020");
            return;
        }
        String old = fn.getName();
        fn.setName("flush_link_sample_counters_and_emit_periodic_average_event", SourceType.USER_DEFINED);
        println("renamed=1 old=" + old + " new=" + fn.getName());
    }
}
