// Rename FUN_800356bc -> remote_name_feature_cleanup_and_lmp268_timer_dispatch
// Pass 158, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass158Region80030000Fun800356bc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800356bc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800356bc");
            return;
        }
        String oldName = f.getName();
        f.setName("remote_name_feature_cleanup_and_lmp268_timer_dispatch",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}