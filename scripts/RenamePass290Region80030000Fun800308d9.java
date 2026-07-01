// Rename FUN_800308d9 -> spurious_byte_after_type3_slot_memcpy_dest_stride_base
// Pass 290, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass290Region80030000Fun800308d9 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800308d9");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800308d9");
            return;
        }
        String oldName = f.getName();
        f.setName("spurious_byte_after_type3_slot_memcpy_dest_stride_base",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}