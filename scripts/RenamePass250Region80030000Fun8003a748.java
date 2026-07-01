// Rename FUN_8003a748 -> bulk_write_bb_regs_0x10_to_0x2d_with_reg0xe_bit2_gate
// Pass 250, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass250Region80030000Fun8003a748 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003a748");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003a748");
            return;
        }
        String oldName = f.getName();
        f.setName("bulk_write_bb_regs_0x10_to_0x2d_with_reg0xe_bit2_gate",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}