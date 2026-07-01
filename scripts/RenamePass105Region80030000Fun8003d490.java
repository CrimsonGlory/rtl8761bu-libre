// Rename FUN_8003d490 -> log_role_slot_state_evt_0x2c5_when_not_role_switch
// Pass 105, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass105Region80030000Fun8003d490 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003d490");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003d490");
            return;
        }
        String oldName = f.getName();
        f.setName("log_role_slot_state_evt_0x2c5_when_not_role_switch",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}