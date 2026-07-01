// Rename FUN_8003b86c -> dispatch_dual_fptr_hooks_by_flag_with_config_field285_bits_or_const_0x41
// Pass 86, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass86Region80030000Fun8003b86c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003b86c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003b86c");
            return;
        }
        String oldName = f.getName();
        f.setName("dispatch_dual_fptr_hooks_by_flag_with_config_field285_bits_or_const_0x41",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}