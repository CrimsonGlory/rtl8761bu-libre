// Rename FUN_80025634 -> wrap_release_and_clear_pending_callback_at_crypto_0x1e8
// Pass 6 continuation (267), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80025634 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80025634");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80025634");
            return;
        }
        String oldName = f.getName();
        f.setName("wrap_release_and_clear_pending_callback_at_crypto_0x1e8",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}