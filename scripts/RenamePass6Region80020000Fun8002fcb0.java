// Rename FUN_8002fcb0 -> program_sco_hw_channel_table_and_bb_regs_from_config
// Pass 6 continuation (101), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002fcb0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002fcb0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002fcb0");
            return;
        }
        String oldName = f.getName();
        f.setName("program_sco_hw_channel_table_and_bb_regs_from_config",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}