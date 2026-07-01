// Rename FUN_80033d30 -> patch_three_bit_codec_slot_field_lower_triplet_via_hw_hook
// Pass 57, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass57Region80030000Fun80033d30 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80033d30");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80033d30");
            return;
        }
        String oldName = f.getName();
        f.setName("patch_three_bit_codec_slot_field_lower_triplet_via_hw_hook",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}