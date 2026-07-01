// Rename FUN_80034ccc -> log_conn_setup_commit_fallback_evt_0x2d2_if_no_patch3
// Pass 115, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass115Region80030000Fun80034ccc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80034ccc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80034ccc");
            return;
        }
        String oldName = f.getName();
        f.setName("log_conn_setup_commit_fallback_evt_0x2d2_if_no_patch3",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}