// Rename FUN_8002bc28 -> alloc_first_free_credit_scheduler_slot_0xd
// Pass 6 continuation (151), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002bc28 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002bc28");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002bc28");
            return;
        }
        String oldName = f.getName();
        f.setName("alloc_first_free_credit_scheduler_slot_0xd",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}