// Rename FUN_800299d8 -> on_random_bdaddr_stop_encrypt_finalize_lmp_detach_and_scan_links
// Pass 6 continuation (212), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800299d8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800299d8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800299d8");
            return;
        }
        String oldName = f.getName();
        f.setName("on_random_bdaddr_stop_encrypt_finalize_lmp_detach_and_scan_links",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}