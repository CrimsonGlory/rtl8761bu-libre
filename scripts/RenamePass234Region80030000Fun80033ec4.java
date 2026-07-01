// Rename FUN_80033ec4 -> invoke_vsc_fc56_rf_reconfig_with_stack_arg_one
// Pass 234, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass234Region80030000Fun80033ec4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80033ec4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80033ec4");
            return;
        }
        String oldName = f.getName();
        f.setName("invoke_vsc_fc56_rf_reconfig_with_stack_arg_one",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}