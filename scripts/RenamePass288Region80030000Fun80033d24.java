// Rename FUN_80033d24 -> noop_void_stub_isr_dispatch_data_to_unsniff_cleanup_gap_jr_ra
// Pass 288, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass288Region80030000Fun80033d24 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80033d24");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80033d24");
            return;
        }
        String oldName = f.getName();
        f.setName("noop_void_stub_isr_dispatch_data_to_unsniff_cleanup_gap_jr_ra",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}