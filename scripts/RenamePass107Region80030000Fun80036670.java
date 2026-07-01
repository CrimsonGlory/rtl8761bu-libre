// Rename FUN_80036670 -> poll_hook_value_until_stable_and_optional_slot_offset_0x270
// Pass 107, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass107Region80030000Fun80036670 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80036670");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80036670");
            return;
        }
        String oldName = f.getName();
        f.setName("poll_hook_value_until_stable_and_optional_slot_offset_0x270",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}