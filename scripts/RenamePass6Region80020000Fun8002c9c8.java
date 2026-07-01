// Rename FUN_8002c9c8 -> safer_plus_pht_butterfly_mix_16byte_block
// Pass 6 continuation (244), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002c9c8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002c9c8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002c9c8");
            return;
        }
        String oldName = f.getName();
        f.setName("safer_plus_pht_butterfly_mix_16byte_block",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}