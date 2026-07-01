// Rename FUN_8002adac -> get_three_slot_descriptor_global_flag_byte
// Pass 6 continuation (309), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002adac extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002adac");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002adac");
            return;
        }
        String oldName = f.getName();
        f.setName("get_three_slot_descriptor_global_flag_byte",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}