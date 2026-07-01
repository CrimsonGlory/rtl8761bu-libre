// Rename FUN_80027300 -> handle_lmp_in_rand_not_accepted_alt
// Pass 6 continuation (109), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80027300 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80027300");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80027300");
            return;
        }
        String oldName = f.getName();
        f.setName("handle_lmp_in_rand_not_accepted_alt",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}