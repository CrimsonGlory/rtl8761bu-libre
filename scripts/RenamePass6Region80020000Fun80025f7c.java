// Rename FUN_80025f7c -> advance_prng_and_clear_crypto_pending_buffers
// Pass 6 continuation (213), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80025f7c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80025f7c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80025f7c");
            return;
        }
        String oldName = f.getName();
        f.setName("advance_prng_and_clear_crypto_pending_buffers",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}