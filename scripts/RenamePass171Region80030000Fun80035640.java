// Rename FUN_80035640 -> gated_link_mode_change_dispatch_with_prehook_and_completion
// Pass 171, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass171Region80030000Fun80035640 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80035640");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80035640");
            return;
        }
        String oldName = f.getName();
        f.setName("gated_link_mode_change_dispatch_with_prehook_and_completion",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}