// Rename FUN_8003b5ec -> copy_dword_array_from_src_to_dst_by_count
// Pass 279, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass279Region80030000Fun8003b5ec extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003b5ec");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003b5ec");
            return;
        }
        String oldName = f.getName();
        f.setName("copy_dword_array_from_src_to_dst_by_count",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}