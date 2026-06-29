// RenamePass12dvRegion80070000Fun800758b4.java — Pass 12dv packet-slot empty-ring test
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12dvRegion80070000Fun800758b4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800758b4L;
        String newName = "is_packet_slot_ring_empty";
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
