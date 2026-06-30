// Rename FUN_8002fb54 -> init_sco_hw_channel_disable_be_c0_restore_saved_bb_regs
// Pass 6 continuation (37), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002fb54 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002fb54");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002fb54");
            return;
        }
        String oldName = f.getName();
        f.setName("init_sco_hw_channel_disable_be_c0_restore_saved_bb_regs",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}