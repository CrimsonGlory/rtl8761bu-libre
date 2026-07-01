// Rename FUN_80033b14 -> adjust_link_mode_change_slot_budget_and_secondary_timing
// Pass 99, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass99Region80030000Fun80033b14 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80033b14");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80033b14");
            return;
        }
        String oldName = f.getName();
        f.setName("adjust_link_mode_change_slot_budget_and_secondary_timing",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}