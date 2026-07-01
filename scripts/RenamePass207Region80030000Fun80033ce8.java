// Rename FUN_80033ce8 -> single_slot_isr_dispatcher_call_or_default_ack
// Pass 207, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass207Region80030000Fun80033ce8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80033ce8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80033ce8");
            return;
        }
        String oldName = f.getName();
        f.setName("single_slot_isr_dispatcher_call_or_default_ack",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}