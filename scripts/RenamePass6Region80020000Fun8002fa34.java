// Rename FUN_8002fa34 -> set_config_byte_bit2_from_enable_byte
// Pass 6 continuation (239), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002fa34 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002fa34");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002fa34");
            return;
        }
        String oldName = f.getName();
        f.setName("set_config_byte_bit2_from_enable_byte",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}