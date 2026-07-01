// Rename FUN_80021e3c -> clear_connection_slot_lmp_pending_and_preferred_rate_mode
// Pass 6 continuation (235), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80021e3c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80021e3c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80021e3c");
            return;
        }
        String oldName = f.getName();
        f.setName("clear_connection_slot_lmp_pending_and_preferred_rate_mode",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}