// Rename FUN_8003e0d4 -> enqueue_connection_packet_completion_ring_or_overflow_dispatch
// Pass 95, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass95Region80030000Fun8003e0d4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003e0d4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003e0d4");
            return;
        }
        String oldName = f.getName();
        f.setName("enqueue_connection_packet_completion_ring_or_overflow_dispatch",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}