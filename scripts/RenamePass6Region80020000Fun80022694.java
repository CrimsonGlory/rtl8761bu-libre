// Rename FUN_80022694 -> dispatch_ssp_io_cap_or_oob_negative_reply_continuation
// Pass 6 continuation (161), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80022694 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80022694");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80022694");
            return;
        }
        String oldName = f.getName();
        f.setName("dispatch_ssp_io_cap_or_oob_negative_reply_continuation",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}