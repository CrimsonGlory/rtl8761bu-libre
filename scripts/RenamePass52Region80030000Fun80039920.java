// Rename FUN_80039920 -> clamp_byte_offset_base_plus_adj_minus_product
// Pass 52, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass52Region80030000Fun80039920 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80039920");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80039920");
            return;
        }
        String oldName = f.getName();
        f.setName("clamp_byte_offset_base_plus_adj_minus_product",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}