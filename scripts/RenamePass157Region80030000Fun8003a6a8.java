// Rename FUN_8003a6a8 -> config_gated_pulse_bb_reg_0x75_bit0_with_spin_delays
// Pass 157, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass157Region80030000Fun8003a6a8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003a6a8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003a6a8");
            return;
        }
        String oldName = f.getName();
        f.setName("config_gated_pulse_bb_reg_0x75_bit0_with_spin_delays",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}