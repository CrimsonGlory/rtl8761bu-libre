// Rename FUN_800366cc -> apply_conn_class_mode_afh_role_remap_and_esco_ptype
// Pass 76, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass76Region80030000Fun800366cc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800366cc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800366cc");
            return;
        }
        String oldName = f.getName();
        f.setName("apply_conn_class_mode_afh_role_remap_and_esco_ptype",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}