// Rename FUN_8002600c -> zero_stage_copy_16byte_crypto_buffer_inject_3bytes_from_0x138
// Pass 6 continuation (190), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002600c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002600c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002600c");
            return;
        }
        String oldName = f.getName();
        f.setName("zero_stage_copy_16byte_crypto_buffer_inject_3bytes_from_0x138",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}