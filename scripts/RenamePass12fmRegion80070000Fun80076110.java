// Pass 12fm: rename FUN_80076110 -> reschedule_timer_wheel_slot_by_delay_ticks
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.*;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12fmRegion80070000Fun80076110 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Function fn = getFunctionAt(toAddr(0x80076110L));
        if (fn == null) {
            println("ERROR: function not found at 0x80076110");
            return;
        }
        fn.setName("reschedule_timer_wheel_slot_by_delay_ticks", SourceType.USER_DEFINED);
        println("renamed=1 " + fn.getName());
    }
}
