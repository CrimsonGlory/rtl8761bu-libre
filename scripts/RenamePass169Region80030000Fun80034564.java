// Rename FUN_80034564 -> poll_hw_ready_bitmask_until_clear_or_log_timeout
// Pass 169, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass169Region80030000Fun80034564 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80034564");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80034564");
            return;
        }
        String oldName = f.getName();
        f.setName("poll_hw_ready_bitmask_until_clear_or_log_timeout",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}