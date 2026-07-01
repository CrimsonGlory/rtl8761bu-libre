// Rename FUN_8003b8cc -> quantize_u16_x4_shift_via_highest_bit_scan_and_byte_table_lookup
// Pass 254, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass254Region80030000Fun8003b8cc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003b8cc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003b8cc");
            return;
        }
        String oldName = f.getName();
        f.setName("quantize_u16_x4_shift_via_highest_bit_scan_and_byte_table_lookup",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}