// Rename FUN_8003851c -> check_role_slot_timing_deadline_overrun_and_set_flag
// Pass 126, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass126Region80030000Fun8003851c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003851c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003851c");
            return;
        }
        String oldName = f.getName();
        f.setName("check_role_slot_timing_deadline_overrun_and_set_flag",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}