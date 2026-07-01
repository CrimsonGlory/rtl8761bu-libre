// Rename FUN_8002b1a4 -> log_patch_install_bootstrap_globals_ten_fields
// Pass 6 continuation (171), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002b1a4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002b1a4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002b1a4");
            return;
        }
        String oldName = f.getName();
        f.setName("log_patch_install_bootstrap_globals_ten_fields",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}