// Rename FUN_8003ce68 -> toggle_global_status_bit0x10_by_enable_flag
// Pass 211, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass211Region80030000Fun8003ce68 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003ce68");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003ce68");
            return;
        }
        String oldName = f.getName();
        f.setName("toggle_global_status_bit0x10_by_enable_flag",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}