// Rename FUN_800399c4 -> clamp_byte_in_place_to_context_bounds_at_offsets_0xf_0x10
// Pass 278, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass278Region80030000Fun800399c4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800399c4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800399c4");
            return;
        }
        String oldName = f.getName();
        f.setName("clamp_byte_in_place_to_context_bounds_at_offsets_0xf_0x10",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}