// Rename FUN_8003c19c -> program_bb_regs_6b_6c_43_6a_via_hook_and_extended_diagnostic
// Pass 127, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass127Region80030000Fun8003c19c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003c19c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003c19c");
            return;
        }
        String oldName = f.getName();
        f.setName("program_bb_regs_6b_6c_43_6a_via_hook_and_extended_diagnostic",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}