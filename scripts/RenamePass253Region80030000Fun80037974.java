// Rename FUN_80037974 -> compute_max_role_slot_timing_threshold_from_conn_fields_298_294_weight_x2
// Pass 253, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass253Region80030000Fun80037974 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80037974");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80037974");
            return;
        }
        String oldName = f.getName();
        f.setName("compute_max_role_slot_timing_threshold_from_conn_fields_298_294_weight_x2",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}