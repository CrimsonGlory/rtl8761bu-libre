// Rename FUN_8003b5b8 -> write_indexed_bb_register_low16_with_global_mask
// Pass 48, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass48Region80030000Fun8003b5b8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003b5b8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003b5b8");
            return;
        }
        String oldName = f.getName();
        f.setName("write_indexed_bb_register_low16_with_global_mask",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}