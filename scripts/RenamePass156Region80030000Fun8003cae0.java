// Rename FUN_8003cae0 -> arm_lmp268_random_delay_and_remote_name_feature_apply_4
// Pass 156, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass156Region80030000Fun8003cae0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003cae0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003cae0");
            return;
        }
        String oldName = f.getName();
        f.setName("arm_lmp268_random_delay_and_remote_name_feature_apply_4",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}