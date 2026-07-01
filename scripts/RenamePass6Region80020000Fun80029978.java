// Rename FUN_80029978 -> scan_random_bdaddr_links_for_encrypted_crypto_arm_or_mode3
// Pass 6 continuation (152), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80029978 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80029978");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80029978");
            return;
        }
        String oldName = f.getName();
        f.setName("scan_random_bdaddr_links_for_encrypted_crypto_arm_or_mode3",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}