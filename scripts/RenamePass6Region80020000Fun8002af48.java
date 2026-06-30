// Rename FUN_8002af48 -> enqueue_acl_tx_descriptor_to_per_handle_pending_queue
// Pass 6 continuation (45), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002af48 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002af48");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002af48");
            return;
        }
        String oldName = f.getName();
        f.setName("enqueue_acl_tx_descriptor_to_per_handle_pending_queue",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}