// Rename FUN_80022e54 -> dispatch_pairing_continuation_by_crypto_state_and_pending_lmp
// Pass 6 continuation (75), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80022e54 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80022e54");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80022e54");
            return;
        }
        String oldName = f.getName();
        f.setName("dispatch_pairing_continuation_by_crypto_state_and_pending_lmp",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}