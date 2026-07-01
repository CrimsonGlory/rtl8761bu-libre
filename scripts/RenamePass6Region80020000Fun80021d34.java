// Rename FUN_80021d34 -> clear_connection_slot_supervision_timing_counters
// Pass 6 continuation (193), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80021d34 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80021d34");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80021d34");
            return;
        }
        String oldName = f.getName();
        f.setName("clear_connection_slot_supervision_timing_counters",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}