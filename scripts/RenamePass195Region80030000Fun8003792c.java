// Rename FUN_8003792c -> arm_link_state_advance_pending_and_commit_conn_slot_timing_mode1
// Pass 195, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass195Region80030000Fun8003792c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003792c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003792c");
            return;
        }
        String oldName = f.getName();
        f.setName("arm_link_state_advance_pending_and_commit_conn_slot_timing_mode1",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}