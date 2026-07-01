// Rename FUN_8003d4fc -> log_role_slot_state_evt_0x2c4_when_not_role_switch
// Pass 183, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass183Region80030000Fun8003d4fc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003d4fc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003d4fc");
            return;
        }
        String oldName = f.getName();
        f.setName("log_role_slot_state_evt_0x2c4_when_not_role_switch",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}