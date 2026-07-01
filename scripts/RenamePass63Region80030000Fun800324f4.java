// Rename FUN_800324f4 -> trigger_acl_ring_buffer_flush_on_tracked_conn_match
// Pass 63, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass63Region80030000Fun800324f4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800324f4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800324f4");
            return;
        }
        String oldName = f.getName();
        f.setName("trigger_acl_ring_buffer_flush_on_tracked_conn_match",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}