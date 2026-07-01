// Rename FUN_800321f8 -> dispatch_lmp_0x0d_power_sample_report_via_tx_hook
// Pass 81, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass81Region80030000Fun800321f8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800321f8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800321f8");
            return;
        }
        String oldName = f.getName();
        f.setName("dispatch_lmp_0x0d_power_sample_report_via_tx_hook",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}