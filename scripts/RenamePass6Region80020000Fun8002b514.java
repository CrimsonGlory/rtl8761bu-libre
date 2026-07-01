// Rename FUN_8002b514 -> poll_hw_tx_status_until_nonnegative_or_log_timeout
// Pass 6 continuation (204), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002b514 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002b514");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002b514");
            return;
        }
        String oldName = f.getName();
        f.setName("poll_hw_tx_status_until_nonnegative_or_log_timeout",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}