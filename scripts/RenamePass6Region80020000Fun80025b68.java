// Rename FUN_80025b68 -> unscramble_codec_jit_template_and_install_hw_hook
// Pass 6 continuation (23), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80025b68 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80025b68");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80025b68");
            return;
        }
        String oldName = f.getName();
        f.setName("unscramble_codec_jit_template_and_install_hw_hook",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}