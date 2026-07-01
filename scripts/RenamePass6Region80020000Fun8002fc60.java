// Rename FUN_8002fc60 -> and_mask_sco_hw_channel_table_5e_and_zero_bb_regs
// Pass 6 continuation (183), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002fc60 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002fc60");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002fc60");
            return;
        }
        String oldName = f.getName();
        f.setName("and_mask_sco_hw_channel_table_5e_and_zero_bb_regs",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}