// Rename FUN_8003b76c -> program_bb_regs_0x220_0x222_0x224_via_hook_with_masked_params
// Pass 152, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass152Region80030000Fun8003b76c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003b76c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003b76c");
            return;
        }
        String oldName = f.getName();
        f.setName("program_bb_regs_0x220_0x222_0x224_via_hook_with_masked_params",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}