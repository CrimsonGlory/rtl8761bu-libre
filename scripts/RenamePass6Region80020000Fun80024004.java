// Rename FUN_80024004 -> lookup_crypto_encryption_state_0x14_0x1f_flag_b
// Pass 6 continuation (306), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80024004 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80024004");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80024004");
            return;
        }
        String oldName = f.getName();
        f.setName("lookup_crypto_encryption_state_0x14_0x1f_flag_b",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}