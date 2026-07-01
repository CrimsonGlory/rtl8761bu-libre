// Rename FUN_80038df4 -> program_bb_reg_0x1e_5bit_from_hook_or_config_if_flag0x10
// Pass 212, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass212Region80030000Fun80038df4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80038df4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80038df4");
            return;
        }
        String oldName = f.getName();
        f.setName("program_bb_reg_0x1e_5bit_from_hook_or_config_if_flag0x10",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}