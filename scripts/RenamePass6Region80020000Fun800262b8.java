// Rename FUN_800262b8 -> verify_ssp_oob_confirmation_hash
// Pass 6 continuation (24), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800262b8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800262b8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800262b8");
            return;
        }
        String oldName = f.getName();
        f.setName("verify_ssp_oob_confirmation_hash",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}