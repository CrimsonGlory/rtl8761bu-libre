// Rename FUN_80029cfc -> arm_master_link_key_phase1_slot_lmp_0x32
// Pass 6 continuation (144), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80029cfc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80029cfc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80029cfc");
            return;
        }
        String oldName = f.getName();
        f.setName("arm_master_link_key_phase1_slot_lmp_0x32",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}