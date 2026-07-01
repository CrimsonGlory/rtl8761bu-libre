// Rename FUN_800375f8 -> init_sco_hw_channel_8plus4_slot_program_and_bb_regs
// Pass 93, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass93Region80030000Fun800375f8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800375f8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800375f8");
            return;
        }
        String oldName = f.getName();
        f.setName("init_sco_hw_channel_8plus4_slot_program_and_bb_regs",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}