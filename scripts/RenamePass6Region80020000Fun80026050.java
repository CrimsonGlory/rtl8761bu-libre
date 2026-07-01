// Rename FUN_80026050 -> reset_crypto_pending_buffers_for_ssp_oob_request
// Pass 6 continuation (146), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80026050 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80026050");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80026050");
            return;
        }
        String oldName = f.getName();
        f.setName("reset_crypto_pending_buffers_for_ssp_oob_request",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}