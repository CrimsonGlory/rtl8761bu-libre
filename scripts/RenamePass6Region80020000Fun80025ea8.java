// Rename FUN_80025ea8 -> derive_dhkey_check_nonce_and_send_lmp_0x42
// Pass 6 continuation (82), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80025ea8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80025ea8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80025ea8");
            return;
        }
        String oldName = f.getName();
        f.setName("derive_dhkey_check_nonce_and_send_lmp_0x42",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}