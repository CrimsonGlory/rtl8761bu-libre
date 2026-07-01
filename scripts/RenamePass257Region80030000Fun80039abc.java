// Rename FUN_80039abc -> config_gated_dispatch_dual_fptr_hooks_via_shared_arg_provider
// Pass 257, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass257Region80030000Fun80039abc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80039abc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80039abc");
            return;
        }
        String oldName = f.getName();
        f.setName("config_gated_dispatch_dual_fptr_hooks_via_shared_arg_provider",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}