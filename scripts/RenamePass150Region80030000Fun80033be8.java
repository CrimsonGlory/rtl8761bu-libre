// Rename FUN_80033be8 -> reset_link_mode_phase_marker_and_update_eir_budget_counter
// Pass 150, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass150Region80030000Fun80033be8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80033be8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80033be8");
            return;
        }
        String oldName = f.getName();
        f.setName("reset_link_mode_phase_marker_and_update_eir_budget_counter",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}