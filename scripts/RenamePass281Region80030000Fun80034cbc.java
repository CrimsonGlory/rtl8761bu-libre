// Rename FUN_80034cbc -> invoke_find_first_inquiry_lap_slot_with_pending_clear
// Pass 281, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass281Region80030000Fun80034cbc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80034cbc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80034cbc");
            return;
        }
        String oldName = f.getName();
        f.setName("invoke_find_first_inquiry_lap_slot_with_pending_clear",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}