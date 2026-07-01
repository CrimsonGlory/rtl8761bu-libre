// Rename FUN_8003a5ec -> read_link_register_0xe_role_bits_13_14_by_slot
// Pass 191, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass191Region80030000Fun8003a5ec extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003a5ec");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003a5ec");
            return;
        }
        String oldName = f.getName();
        f.setName("read_link_register_0xe_role_bits_13_14_by_slot",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}