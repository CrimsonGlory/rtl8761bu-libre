// Rename FUN_800336f4 -> clear_connection_setup_flag_and_program_bb_regs_0x36_bit13_and_0x8e_via_hook
// Pass 112, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass112Region80030000Fun800336f4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800336f4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800336f4");
            return;
        }
        String oldName = f.getName();
        f.setName("clear_connection_setup_flag_and_program_bb_regs_0x36_bit13_and_0x8e_via_hook",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}