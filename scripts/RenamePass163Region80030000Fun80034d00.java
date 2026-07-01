// Rename FUN_80034d00 -> log_link_mode_cleanup_evt_0x3eb_or_0x2d1_if_no_patch3
// Pass 163, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass163Region80030000Fun80034d00 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80034d00");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80034d00");
            return;
        }
        String oldName = f.getName();
        f.setName("log_link_mode_cleanup_evt_0x3eb_or_0x2d1_if_no_patch3",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}