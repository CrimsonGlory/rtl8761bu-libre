// Rename FUN_8002d0b0 -> derive_e22_aco_and_sres_via_hmac_safer
// Pass 6 continuation (68), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002d0b0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002d0b0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002d0b0");
            return;
        }
        String oldName = f.getName();
        f.setName("derive_e22_aco_and_sres_via_hmac_safer",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}