// Rename FUN_8002ef48 -> dispatch_lc_tx_logger_hook_subcases_with_pending_queue
// Pass 6 continuation (36), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002ef48 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002ef48");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002ef48");
            return;
        }
        String oldName = f.getName();
        f.setName("dispatch_lc_tx_logger_hook_subcases_with_pending_queue",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}