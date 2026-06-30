// Rename FUN_80026570 -> derive_link_key_hmac_on_ssp_pairing_complete
// Pass 6 continuation (77), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80026570 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80026570");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80026570");
            return;
        }
        String oldName = f.getName();
        f.setName("derive_link_key_hmac_on_ssp_pairing_complete",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}