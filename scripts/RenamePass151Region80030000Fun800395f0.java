// Rename FUN_800395f0 -> run_bb_reg_init_hook_chain_and_program_0x1e_5bit_field
// Pass 151, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass151Region80030000Fun800395f0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800395f0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800395f0");
            return;
        }
        String oldName = f.getName();
        f.setName("run_bb_reg_init_hook_chain_and_program_0x1e_5bit_field",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}