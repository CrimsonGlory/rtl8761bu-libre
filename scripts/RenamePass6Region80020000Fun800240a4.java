// Rename FUN_800240a4 -> select_bdaddr_random_or_mode_byte_for_codec_jit_e0
// Pass 6 continuation (173), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800240a4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800240a4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800240a4");
            return;
        }
        String oldName = f.getName();
        f.setName("select_bdaddr_random_or_mode_byte_for_codec_jit_e0",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}