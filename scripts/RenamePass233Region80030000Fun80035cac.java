// Rename FUN_80035cac -> gate_conn_setup_commit_vs_link_mode_cleanup_dispatch
// Pass 233, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass233Region80030000Fun80035cac extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80035cac");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80035cac");
            return;
        }
        String oldName = f.getName();
        f.setName("gate_conn_setup_commit_vs_link_mode_cleanup_dispatch",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}