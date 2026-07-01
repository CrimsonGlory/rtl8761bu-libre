// Rename FUN_80039a10 -> noop_config_dispatch_hook_fptr_jr_ra_stub
// Pass 137, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass137Region80030000Fun80039a10 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80039a10");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80039a10");
            return;
        }
        String oldName = f.getName();
        f.setName("noop_config_dispatch_hook_fptr_jr_ra_stub",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}