// Rename FUN_80022098 -> arm_encryption_when_crypto_substate_0x11_or_0x1e
// Pass 6 continuation (138), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80022098 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80022098");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80022098");
            return;
        }
        String oldName = f.getName();
        f.setName("arm_encryption_when_crypto_substate_0x11_or_0x1e",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}