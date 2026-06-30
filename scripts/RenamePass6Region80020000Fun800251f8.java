// Rename FUN_800251f8 -> derive_sres_e1_or_e22_and_send_lmp_response
// Pass 6 continuation (27), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800251f8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800251f8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800251f8");
            return;
        }
        String oldName = f.getName();
        f.setName("derive_sres_e1_or_e22_and_send_lmp_response",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}