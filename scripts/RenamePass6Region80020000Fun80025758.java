// Rename FUN_80025758 -> copy_unscramble_fixed_48byte_two_half_reversals
// Pass 6 continuation (245), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80025758 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80025758");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80025758");
            return;
        }
        String oldName = f.getName();
        f.setName("copy_unscramble_fixed_48byte_two_half_reversals",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}