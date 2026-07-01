// Rename FUN_8002f930 -> set_vsc_fcf0_context_byte_0x269_from_enable2_and_mode_byte
// Pass 6 continuation (258), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002f930 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002f930");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002f930");
            return;
        }
        String oldName = f.getName();
        f.setName("set_vsc_fcf0_context_byte_0x269_from_enable2_and_mode_byte",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}