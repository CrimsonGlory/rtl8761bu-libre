// Rename FUN_8003c608 -> write_indexed_bb_register_low16_with_mask_and_hook
// Pass 47, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass47Region80030000Fun8003c608 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003c608");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003c608");
            return;
        }
        String oldName = f.getName();
        f.setName("write_indexed_bb_register_low16_with_mask_and_hook",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}