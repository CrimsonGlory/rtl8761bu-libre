// Rename FUN_800384ac -> advance_role_slot_link_state_and_capture_halved_hw_clock
// Pass 104, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass104Region80030000Fun800384ac extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800384ac");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800384ac");
            return;
        }
        String oldName = f.getName();
        f.setName("advance_role_slot_link_state_and_capture_halved_hw_clock",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}