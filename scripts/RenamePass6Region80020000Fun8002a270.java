// Rename FUN_8002a270 -> drain_acl_reassembly_pending_ring_by_slot_and_release_buffers
// Pass 6 continuation (110), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002a270 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002a270");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002a270");
            return;
        }
        String oldName = f.getName();
        f.setName("drain_acl_reassembly_pending_ring_by_slot_and_release_buffers",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}