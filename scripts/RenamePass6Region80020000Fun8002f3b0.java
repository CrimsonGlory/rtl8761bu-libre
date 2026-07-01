// Rename FUN_8002f3b0 -> copy_eight_literal_pool_globals_and_init_baseband_hw
// Pass 6 continuation (141), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002f3b0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002f3b0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002f3b0");
            return;
        }
        String oldName = f.getName();
        f.setName("copy_eight_literal_pool_globals_and_init_baseband_hw",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}