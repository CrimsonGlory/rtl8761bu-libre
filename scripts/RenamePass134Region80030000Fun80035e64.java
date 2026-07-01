// Rename FUN_80035e64 -> zero_bos_struct_and_init_link_mode_bb_timing_with_lmp_3ee_branch
// Pass 134, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass134Region80030000Fun80035e64 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80035e64");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80035e64");
            return;
        }
        String oldName = f.getName();
        f.setName("zero_bos_struct_and_init_link_mode_bb_timing_with_lmp_3ee_branch",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}