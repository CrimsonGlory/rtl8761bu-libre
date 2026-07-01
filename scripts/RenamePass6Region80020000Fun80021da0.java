// Rename FUN_80021da0 -> init_lc_global_psm_qos_enable_and_default_timing_intervals
// Pass 6 continuation (252), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80021da0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80021da0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80021da0");
            return;
        }
        String oldName = f.getName();
        f.setName("init_lc_global_psm_qos_enable_and_default_timing_intervals",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}