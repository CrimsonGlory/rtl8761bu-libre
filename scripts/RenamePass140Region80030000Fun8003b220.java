// Rename FUN_8003b220 -> program_sco_bb_regs_from_config_offset_0x106_via_hw_hook
// Pass 140, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass140Region80030000Fun8003b220 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003b220");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003b220");
            return;
        }
        String oldName = f.getName();
        f.setName("program_sco_bb_regs_from_config_offset_0x106_via_hw_hook",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}