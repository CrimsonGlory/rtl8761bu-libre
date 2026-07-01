// Rename FUN_800223a8 -> invoke_lmp_0x25b_from_crypto_pending_slot_if_active
// Pass 6 continuation (227), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800223a8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800223a8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800223a8");
            return;
        }
        String oldName = f.getName();
        f.setName("invoke_lmp_0x25b_from_crypto_pending_slot_if_active",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}