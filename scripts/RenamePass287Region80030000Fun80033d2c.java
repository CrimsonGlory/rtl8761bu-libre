// Rename FUN_80033d2c -> noop_void_stub_unsniff_cleanup_to_codec_lower_triplet_gap_jr_ra
// Pass 287, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass287Region80030000Fun80033d2c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80033d2c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80033d2c");
            return;
        }
        String oldName = f.getName();
        f.setName("noop_void_stub_unsniff_cleanup_to_codec_lower_triplet_gap_jr_ra",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}