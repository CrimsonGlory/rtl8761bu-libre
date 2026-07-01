// Rename FUN_8003a7b4 -> program_bb_regs_41_43_44_46_47_via_hook_and_da_d6_dispatch
// Pass 103, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass103Region80030000Fun8003a7b4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003a7b4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003a7b4");
            return;
        }
        String oldName = f.getName();
        f.setName("program_bb_regs_41_43_44_46_47_via_hook_and_da_d6_dispatch",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}