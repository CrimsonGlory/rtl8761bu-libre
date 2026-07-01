// Rename FUN_80033744 -> clear_connection_setup_flag_and_program_bb_regs_0x32_bit13_and_0x8f_via_hook
// Pass 193, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass193Region80030000Fun80033744 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80033744");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80033744");
            return;
        }
        String oldName = f.getName();
        f.setName("clear_connection_setup_flag_and_program_bb_regs_0x32_bit13_and_0x8f_via_hook",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}