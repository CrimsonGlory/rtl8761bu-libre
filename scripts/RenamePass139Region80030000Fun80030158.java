// Rename FUN_80030158 -> dispatch_vsc_vendor_config_subcmd_write_feature_flags
// Pass 139, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass139Region80030000Fun80030158 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80030158");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80030158");
            return;
        }
        String oldName = f.getName();
        f.setName("dispatch_vsc_vendor_config_subcmd_write_feature_flags",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}