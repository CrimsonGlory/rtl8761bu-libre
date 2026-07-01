// Rename FUN_80024218 -> invoke_lmp_0x268_from_crypto_pending_slot_if_active
// Pass 6 continuation (232), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80024218 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80024218");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80024218");
            return;
        }
        String oldName = f.getName();
        f.setName("invoke_lmp_0x268_from_crypto_pending_slot_if_active",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}