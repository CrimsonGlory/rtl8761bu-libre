// Rename FUN_8003e58c -> poll_hw_clock_stride88_slot_and_acl_credit_consumer
// Pass 149, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass149Region80030000Fun8003e58c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003e58c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003e58c");
            return;
        }
        String oldName = f.getName();
        f.setName("poll_hw_clock_stride88_slot_and_acl_credit_consumer",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}