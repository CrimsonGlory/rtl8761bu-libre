// Rename FUN_80027278 -> handle_lmp_comb_key_not_accepted
// Pass 6 continuation (100), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80027278 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80027278");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80027278");
            return;
        }
        String oldName = f.getName();
        f.setName("handle_lmp_comb_key_not_accepted",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}