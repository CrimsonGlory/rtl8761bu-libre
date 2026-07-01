// Rename FUN_800257b0 -> copy_unscramble_two_independent_half_reversals
// Pass 6 continuation (196), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800257b0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800257b0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800257b0");
            return;
        }
        String oldName = f.getName();
        f.setName("copy_unscramble_two_independent_half_reversals",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}