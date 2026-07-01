// Rename FUN_8003b744 -> or_mask_into_global_dword_at_indirect_ptr
// Pass 284, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass284Region80030000Fun8003b744 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003b744");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003b744");
            return;
        }
        String oldName = f.getName();
        f.setName("or_mask_into_global_dword_at_indirect_ptr",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}