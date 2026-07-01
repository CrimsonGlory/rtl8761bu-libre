// Rename FUN_80029a14 -> on_random_bdaddr_encryption_finalize_lmp_detach_and_scan_links
// Pass 6 continuation (211), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80029a14 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80029a14");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80029a14");
            return;
        }
        String oldName = f.getName();
        f.setName("on_random_bdaddr_encryption_finalize_lmp_detach_and_scan_links",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}