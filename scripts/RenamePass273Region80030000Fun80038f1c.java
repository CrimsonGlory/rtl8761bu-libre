// Rename FUN_80038f1c -> dispatch_bb_reg_pack_zero_clear_if_config_bit0x80
// Pass 273, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass273Region80030000Fun80038f1c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80038f1c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80038f1c");
            return;
        }
        String oldName = f.getName();
        f.setName("dispatch_bb_reg_pack_zero_clear_if_config_bit0x80",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}