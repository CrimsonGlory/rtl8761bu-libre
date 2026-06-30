// Rename FUN_80021fa0 -> tick_dhkey_check_stall_scan_encrypted_links_on_timer_expiry
// Pass 6 continuation (88), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80021fa0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80021fa0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80021fa0");
            return;
        }
        String oldName = f.getName();
        f.setName("tick_dhkey_check_stall_scan_encrypted_links_on_timer_expiry",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}