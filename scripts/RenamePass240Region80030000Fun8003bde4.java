// Rename FUN_8003bde4 -> program_config_afh_channel_map_all_64ch_to_bb_regs_0x5e_0x5f
// Pass 240, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass240Region80030000Fun8003bde4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003bde4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003bde4");
            return;
        }
        String oldName = f.getName();
        f.setName("program_config_afh_channel_map_all_64ch_to_bb_regs_0x5e_0x5f",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}