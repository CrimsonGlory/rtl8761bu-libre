// Rename FUN_8003c69c -> read_indexed_bb_register_low16_by_byte_index
// Pass 45, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass45Region80030000Fun8003c69c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003c69c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003c69c");
            return;
        }
        String oldName = f.getName();
        f.setName("read_indexed_bb_register_low16_by_byte_index",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}