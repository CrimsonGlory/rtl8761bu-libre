// Rename FUN_8003785c -> mask_merge_hw_channel_index_from_mode_byte_with_fptr_precheck_and_post_hooks
// Pass 84, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass84Region80030000Fun8003785c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003785c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003785c");
            return;
        }
        String oldName = f.getName();
        f.setName("mask_merge_hw_channel_index_from_mode_byte_with_fptr_precheck_and_post_hooks",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}