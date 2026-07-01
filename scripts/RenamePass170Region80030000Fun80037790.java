// Rename FUN_80037790 -> dispatch_acl_tx_by_handle_to_completion_or_pending_queue
// Pass 170, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass170Region80030000Fun80037790 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80037790");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80037790");
            return;
        }
        String oldName = f.getName();
        f.setName("dispatch_acl_tx_by_handle_to_completion_or_pending_queue",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}