// Rename FUN_800218ec -> zero_global_slot_index_and_memset_0x114_descriptor_buffer
// Pass 6 continuation (278), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800218ec extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800218ec");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800218ec");
            return;
        }
        String oldName = f.getName();
        f.setName("zero_global_slot_index_and_memset_0x114_descriptor_buffer",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}