// Rename FUN_80027994 -> dispatch_lmp_pairing_continuation_by_crypto_state
// Pass 6 continuation (20), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80027994 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80027994");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80027994");
            return;
        }
        String oldName = f.getName();
        f.setName("dispatch_lmp_pairing_continuation_by_crypto_state",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}