// Rename FUN_80035768 -> commit_connection_setup_mode_by_slot_bitmask_and_gates
// Pass 75, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass75Region80030000Fun80035768 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80035768");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80035768");
            return;
        }
        String oldName = f.getName();
        f.setName("commit_connection_setup_mode_by_slot_bitmask_and_gates",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}