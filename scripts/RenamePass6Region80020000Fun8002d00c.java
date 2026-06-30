// Rename FUN_8002d00c -> derive_e1_aco_and_sres_via_safer_plus
// Pass 6 continuation (61), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002d00c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002d00c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002d00c");
            return;
        }
        String oldName = f.getName();
        f.setName("derive_e1_aco_and_sres_via_safer_plus",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}