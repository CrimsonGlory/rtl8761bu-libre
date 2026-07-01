// Rename FUN_80037a20 -> clear_role_slot_timing_flag_and_dispatch_link_supervision_event
// Pass 141, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass141Region80030000Fun80037a20 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80037a20");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80037a20");
            return;
        }
        String oldName = f.getName();
        f.setName("clear_role_slot_timing_flag_and_dispatch_link_supervision_event",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}