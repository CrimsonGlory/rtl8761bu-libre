// Rename FUN_800378e4 -> clear_link_state_advance_pending_and_commit_conn_slot_timing_mode2
// Pass 192, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass192Region80030000Fun800378e4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800378e4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800378e4");
            return;
        }
        String oldName = f.getName();
        f.setName("clear_link_state_advance_pending_and_commit_conn_slot_timing_mode2",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}