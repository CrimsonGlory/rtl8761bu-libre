// Rename FUN_80026460 -> dispatch_ssp_pairing_method_via_lmp_0x266_dhkey_hook
// Pass 6 continuation (30), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80026460 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80026460");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80026460");
            return;
        }
        String oldName = f.getName();
        f.setName("dispatch_ssp_pairing_method_via_lmp_0x266_dhkey_hook",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}