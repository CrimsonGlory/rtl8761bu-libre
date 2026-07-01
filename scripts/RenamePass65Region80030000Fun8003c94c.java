// Rename FUN_8003c94c -> program_inquiry_lap_hw_channel_by_pending_slot_count
// Pass 65, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass65Region80030000Fun8003c94c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003c94c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003c94c");
            return;
        }
        String oldName = f.getName();
        f.setName("program_inquiry_lap_hw_channel_by_pending_slot_count",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}