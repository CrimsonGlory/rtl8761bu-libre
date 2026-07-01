// Rename FUN_80035ff4 -> program_link_mode_bb_regs_merge_ram_timing_and_arm_status
// Pass 243, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass243Region80030000Fun80035ff4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80035ff4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80035ff4");
            return;
        }
        String oldName = f.getName();
        f.setName("program_link_mode_bb_regs_merge_ram_timing_and_arm_status",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}