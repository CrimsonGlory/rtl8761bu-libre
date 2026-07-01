// Rename FUN_800334ac -> compute_lmp_slot_offset_byte_from_conn_timing_params
// Pass 124, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass124Region80030000Fun800334ac extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800334ac");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800334ac");
            return;
        }
        String oldName = f.getName();
        f.setName("compute_lmp_slot_offset_byte_from_conn_timing_params",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}