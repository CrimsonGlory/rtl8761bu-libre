// Rename FUN_800347a0 -> boot_init_optional_hook_or_dispatch_slots_and_crypto_reinit
// Pass 213, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass213Region80030000Fun800347a0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800347a0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800347a0");
            return;
        }
        String oldName = f.getName();
        f.setName("boot_init_optional_hook_or_dispatch_slots_and_crypto_reinit",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}