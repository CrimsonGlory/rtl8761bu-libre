// RenamePass12duRegion80070000Fun800758cc.java — Pass 12du packet-slot fill-count read
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12duRegion80070000Fun800758cc extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800758ccL;
        String newName = "read_packet_slot_ring_fill_count_or_invalid";
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
