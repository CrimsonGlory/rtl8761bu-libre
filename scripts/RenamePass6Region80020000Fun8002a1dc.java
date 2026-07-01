// Rename FUN_8002a1dc -> boot_init_chain_string_user_baseband_and_subsystems
// Pass 6 continuation (166), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002a1dc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002a1dc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002a1dc");
            return;
        }
        String oldName = f.getName();
        f.setName("boot_init_chain_string_user_baseband_and_subsystems",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}