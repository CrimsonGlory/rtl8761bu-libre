// Rename FUN_8002b894 -> zero_esco_link_high_nibble_and_merge_slot_config_low_bits
// Pass 6 continuation (187), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002b894 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002b894");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002b894");
            return;
        }
        String oldName = f.getName();
        f.setName("zero_esco_link_high_nibble_and_merge_slot_config_low_bits",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}