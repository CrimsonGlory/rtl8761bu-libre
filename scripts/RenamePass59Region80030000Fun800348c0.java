// Rename FUN_800348c0 -> and_clear_hw_channel_bit15_by_conn_index_via_esco_remap
// Pass 59, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass59Region80030000Fun800348c0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800348c0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800348c0");
            return;
        }
        String oldName = f.getName();
        f.setName("and_clear_hw_channel_bit15_by_conn_index_via_esco_remap",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}