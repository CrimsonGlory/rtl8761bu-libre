// Rename FUN_80035104 -> poll_vsc_fc11_3_until_pending_clear_with_link_mode_timeouts
// Pass 94, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass94Region80030000Fun80035104 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80035104");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80035104");
            return;
        }
        String oldName = f.getName();
        f.setName("poll_vsc_fc11_3_until_pending_clear_with_link_mode_timeouts",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}