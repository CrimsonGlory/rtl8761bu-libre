// Rename FUN_8003491c -> or_merge_hw_channel_bit15_by_conn_index_via_esco_remap
// Pass 58, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass58Region80030000Fun8003491c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003491c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003491c");
            return;
        }
        String oldName = f.getName();
        f.setName("or_merge_hw_channel_bit15_by_conn_index_via_esco_remap",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}