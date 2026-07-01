// Rename FUN_80035214 -> apply_link_mode_change_bb_regs_and_timeout_by_phase
// Pass 148, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass148Region80030000Fun80035214 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80035214");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80035214");
            return;
        }
        String oldName = f.getName();
        f.setName("apply_link_mode_change_bb_regs_and_timeout_by_phase",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}