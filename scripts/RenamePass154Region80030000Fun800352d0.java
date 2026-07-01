// Rename FUN_800352d0 -> evaluate_lmp_3ee_link_mode_phase_with_retry_counter
// Pass 154, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass154Region80030000Fun800352d0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800352d0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800352d0");
            return;
        }
        String oldName = f.getName();
        f.setName("evaluate_lmp_3ee_link_mode_phase_with_retry_counter",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}