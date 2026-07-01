// Rename FUN_8003a360 -> dispatch_clock_trim_or_bb_reg_0x6f_by_config_flag0x8
// Pass 215, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass215Region80030000Fun8003a360 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003a360");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003a360");
            return;
        }
        String oldName = f.getName();
        f.setName("dispatch_clock_trim_or_bb_reg_0x6f_by_config_flag0x8",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}