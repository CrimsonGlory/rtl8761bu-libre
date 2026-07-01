// Rename FUN_80035378 -> check_connection_setup_commit_gate_status
// Pass 79, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass79Region80030000Fun80035378 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80035378");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80035378");
            return;
        }
        String oldName = f.getName();
        f.setName("check_connection_setup_commit_gate_status",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}