// Rename FUN_80039518 -> dispatch_optional_subsystem_hooks_during_hw_reg_config
// Pass 100, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass100Region80030000Fun80039518 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80039518");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80039518");
            return;
        }
        String oldName = f.getName();
        f.setName("dispatch_optional_subsystem_hooks_during_hw_reg_config",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}