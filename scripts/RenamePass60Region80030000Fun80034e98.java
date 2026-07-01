// Rename FUN_80034e98 -> log_ogc3_config_apply_evt_0x4b6_if_no_patch3
// Pass 60, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass60Region80030000Fun80034e98 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80034e98");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80034e98");
            return;
        }
        String oldName = f.getName();
        f.setName("log_ogc3_config_apply_evt_0x4b6_if_no_patch3",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}