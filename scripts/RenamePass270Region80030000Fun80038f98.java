// Rename FUN_80038f98 -> copy_status_byte_to_config_if_bit0x20_then_dispatch_bb_reg_pulse
// Pass 270, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass270Region80030000Fun80038f98 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80038f98");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80038f98");
            return;
        }
        String oldName = f.getName();
        f.setName("copy_status_byte_to_config_if_bit0x20_then_dispatch_bb_reg_pulse",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}