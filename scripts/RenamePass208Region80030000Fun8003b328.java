// Rename FUN_8003b328 -> apply_cached_sco_bb_register_pairs_via_hw_hook
// Pass 208, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass208Region80030000Fun8003b328 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003b328");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003b328");
            return;
        }
        String oldName = f.getName();
        f.setName("apply_cached_sco_bb_register_pairs_via_hw_hook",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}