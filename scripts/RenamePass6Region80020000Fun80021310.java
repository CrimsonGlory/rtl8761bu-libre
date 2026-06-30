// Rename FUN_80021310 -> validate_afh_host_channel_class_params_and_store_weight
// Pass 6 continuation (53), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80021310 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80021310");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80021310");
            return;
        }
        String oldName = f.getName();
        f.setName("validate_afh_host_channel_class_params_and_store_weight",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}