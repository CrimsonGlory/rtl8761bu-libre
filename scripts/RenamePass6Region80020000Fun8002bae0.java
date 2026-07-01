// Rename FUN_8002bae0 -> release_credit_scheduler_slot_clear_descriptor_flags
// Pass 6 continuation (121), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002bae0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002bae0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002bae0");
            return;
        }
        String oldName = f.getName();
        f.setName("release_credit_scheduler_slot_clear_descriptor_flags",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}