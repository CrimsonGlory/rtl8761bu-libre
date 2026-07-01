// Rename FUN_8003ce50 -> afh_cleanup_apply_lap_hopping_and_feature_orchestrator
// Pass 53, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass53Region80030000Fun8003ce50 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003ce50");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003ce50");
            return;
        }
        String oldName = f.getName();
        f.setName("afh_cleanup_apply_lap_hopping_and_feature_orchestrator",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}