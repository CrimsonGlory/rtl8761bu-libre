// Rename FUN_8002ad30 -> init_three_slot_0x34_linked_descriptors_and_clear_buffers
// Pass 6 continuation (123), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002ad30 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002ad30");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002ad30");
            return;
        }
        String oldName = f.getName();
        f.setName("init_three_slot_0x34_linked_descriptors_and_clear_buffers",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}