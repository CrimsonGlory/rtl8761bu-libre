// Rename FUN_8002f9f4 -> set_config_byte_bits0_and_1_from_enable_pair
// Pass 6 continuation (202), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002f9f4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002f9f4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002f9f4");
            return;
        }
        String oldName = f.getName();
        f.setName("set_config_byte_bits0_and_1_from_enable_pair",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}