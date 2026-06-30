// Rename FUN_80075ca8 -> packet_slot_ring_dequeue_and_dispatch_loop
// Pass 12fr, region 0x80070000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass12frFun80075ca8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80075ca8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80075ca8");
            return;
        }
        String oldName = f.getName();
        f.setName("packet_slot_ring_dequeue_and_dispatch_loop",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}