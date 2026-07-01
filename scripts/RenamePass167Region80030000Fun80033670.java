// Rename FUN_80033670 -> check_calibration_mode_conn_weight_vs_config_threshold
// Pass 167, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass167Region80030000Fun80033670 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80033670");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80033670");
            return;
        }
        String oldName = f.getName();
        f.setName("check_calibration_mode_conn_weight_vs_config_threshold",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}