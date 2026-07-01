// Rename FUN_80038c94 -> compute_int64_halves_signed_shift_width
// Pass 64, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass64Region80030000Fun80038c94 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80038c94");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80038c94");
            return;
        }
        String oldName = f.getName();
        f.setName("compute_int64_halves_signed_shift_width",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}