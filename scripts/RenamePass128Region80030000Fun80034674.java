// Rename FUN_80034674 -> copy_nine_dispatch_slots_and_init_baseband_subsystems
// Pass 128, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass128Region80030000Fun80034674 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80034674");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80034674");
            return;
        }
        String oldName = f.getName();
        f.setName("copy_nine_dispatch_slots_and_init_baseband_subsystems",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}