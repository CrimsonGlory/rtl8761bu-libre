// Rename FUN_8003c6e8 -> program_bb_regs_0x188_0x18a_by_mode_byte_gated_on_config_bit0x10
// Pass 82, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass82Region80030000Fun8003c6e8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003c6e8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003c6e8");
            return;
        }
        String oldName = f.getName();
        f.setName("program_bb_regs_0x188_0x18a_by_mode_byte_gated_on_config_bit0x10",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}