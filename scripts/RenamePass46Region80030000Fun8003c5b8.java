// Rename FUN_8003c5b8 -> read_indexed_bb_register_low16_with_mask_and_poll
// Pass 46, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass46Region80030000Fun8003c5b8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003c5b8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003c5b8");
            return;
        }
        String oldName = f.getName();
        f.setName("read_indexed_bb_register_low16_with_mask_and_poll",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}