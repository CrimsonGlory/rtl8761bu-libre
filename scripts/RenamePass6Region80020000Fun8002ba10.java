// Rename FUN_8002ba10 -> commit_credit_scheduler_slot_hw_arm_descriptor
// Pass 6 continuation (54), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002ba10 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002ba10");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002ba10");
            return;
        }
        String oldName = f.getName();
        f.setName("commit_credit_scheduler_slot_hw_arm_descriptor",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}