// Rename FUN_8003b818 -> merge_status_high_bits_2_3_and_dispatch_fptr_hook_0x27c
// Pass 185, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass185Region80030000Fun8003b818 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003b818");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003b818");
            return;
        }
        String oldName = f.getName();
        f.setName("merge_status_high_bits_2_3_and_dispatch_fptr_hook_0x27c",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}