// Rename FUN_8003b758 -> and_mask_into_global_dword_at_indirect_ptr
// Pass 283, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass283Region80030000Fun8003b758 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003b758");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003b758");
            return;
        }
        String oldName = f.getName();
        f.setName("and_mask_into_global_dword_at_indirect_ptr",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}