// Rename FUN_80026920 -> clear_all_link_key_slot_occupied_flags_and_count
// Pass 6 continuation (256), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80026920 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80026920");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80026920");
            return;
        }
        String oldName = f.getName();
        f.setName("clear_all_link_key_slot_occupied_flags_and_count",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}