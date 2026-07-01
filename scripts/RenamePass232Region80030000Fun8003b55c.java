// Rename FUN_8003b55c -> read_high_nibble_from_global_ushort_at_DAT_8003b570
// Pass 232, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass232Region80030000Fun8003b55c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003b55c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003b55c");
            return;
        }
        String oldName = f.getName();
        f.setName("read_high_nibble_from_global_ushort_at_DAT_8003b570",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}