// Rename FUN_8003013c -> vsc_vendor_config_subcmd0_store_credit_scheduler_global_byte
// Pass 227, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass227Region80030000Fun8003013c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003013c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003013c");
            return;
        }
        String oldName = f.getName();
        f.setName("vsc_vendor_config_subcmd0_store_credit_scheduler_global_byte",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}