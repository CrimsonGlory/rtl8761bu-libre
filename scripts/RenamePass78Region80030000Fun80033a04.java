// Rename FUN_80033a04 -> check_link_mode_change_gate_status
// Pass 78, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass78Region80030000Fun80033a04 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80033a04");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80033a04");
            return;
        }
        String oldName = f.getName();
        f.setName("check_link_mode_change_gate_status",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}