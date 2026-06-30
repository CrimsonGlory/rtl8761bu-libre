// Rename FUN_8002ae50 -> drain_sco_per_handle_pending_descriptor_queue
// Pass 6 continuation (50), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002ae50 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002ae50");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002ae50");
            return;
        }
        String oldName = f.getName();
        f.setName("drain_sco_per_handle_pending_descriptor_queue",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}