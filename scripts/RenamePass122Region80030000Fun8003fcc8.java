// Rename FUN_8003fcc8 -> role_switch_commit_staged_slot_transition
// Pass 122, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass122Region80030000Fun8003fcc8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003fcc8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003fcc8");
            return;
        }
        String oldName = f.getName();
        f.setName("role_switch_commit_staged_slot_transition",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}