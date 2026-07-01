// Rename FUN_8003d0d0 -> reset_slot_tail_and_hook_dispatch_status_upper_bits_if_idle
// Pass 88, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass88Region80030000Fun8003d0d0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003d0d0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003d0d0");
            return;
        }
        String oldName = f.getName();
        f.setName("reset_slot_tail_and_hook_dispatch_status_upper_bits_if_idle",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}