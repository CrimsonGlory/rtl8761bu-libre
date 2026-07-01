// Rename FUN_8003ab74 -> preserve_bb_regs_5a_5c_run_regscript_hook_then_program_bb_bundle
// Pass 145, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass145Region80030000Fun8003ab74 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003ab74");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003ab74");
            return;
        }
        String oldName = f.getName();
        f.setName("preserve_bb_regs_5a_5c_run_regscript_hook_then_program_bb_bundle",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}