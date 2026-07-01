// Rename FUN_80033d28 -> noop_unsniff_slave_cleanup_hook_jr_ra_stub
// Pass 138, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass138Region80030000Fun80033d28 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80033d28");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80033d28");
            return;
        }
        String oldName = f.getName();
        f.setName("noop_unsniff_slave_cleanup_hook_jr_ra_stub",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}