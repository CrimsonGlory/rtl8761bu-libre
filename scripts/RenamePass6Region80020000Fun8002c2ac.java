// Rename FUN_8002c2ac -> copy_codec8_h2_h0_rom_templates_to_staging
// Pass 6 continuation (268), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002c2ac extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002c2ac");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002c2ac");
            return;
        }
        String oldName = f.getName();
        f.setName("copy_codec8_h2_h0_rom_templates_to_staging",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}