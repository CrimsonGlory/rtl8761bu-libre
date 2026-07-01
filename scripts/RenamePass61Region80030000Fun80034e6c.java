// Rename FUN_80034e6c -> log_role_switch_housekeeping_evt_0x330_if_no_patch3
// Pass 61, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass61Region80030000Fun80034e6c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80034e6c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80034e6c");
            return;
        }
        String oldName = f.getName();
        f.setName("log_role_switch_housekeeping_evt_0x330_if_no_patch3",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}