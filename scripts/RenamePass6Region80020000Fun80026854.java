// Rename FUN_80026854 -> clear_occupied_flags_on_seven_link_key_table_slots
// Pass 6 continuation (281), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80026854 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80026854");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80026854");
            return;
        }
        String oldName = f.getName();
        f.setName("clear_occupied_flags_on_seven_link_key_table_slots",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}