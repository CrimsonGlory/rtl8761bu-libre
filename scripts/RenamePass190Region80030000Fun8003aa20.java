// Rename FUN_8003aa20 -> dispatch_config_flag_bit2_afh_or_fptr_pair_store_status_byte
// Pass 190, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass190Region80030000Fun8003aa20 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003aa20");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003aa20");
            return;
        }
        String oldName = f.getName();
        f.setName("dispatch_config_flag_bit2_afh_or_fptr_pair_store_status_byte",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}