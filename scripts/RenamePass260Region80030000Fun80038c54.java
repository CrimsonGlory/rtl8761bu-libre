// Rename FUN_80038c54 -> extract_int64_halves_shifted_short_at_bit_offset
// Pass 260, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass260Region80030000Fun80038c54 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80038c54");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80038c54");
            return;
        }
        String oldName = f.getName();
        f.setName("extract_int64_halves_shifted_short_at_bit_offset",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}