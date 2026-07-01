// Rename FUN_80034884 -> read_hw_channel_bits9_11_by_role_index_via_esco_remap
// Pass 114, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass114Region80030000Fun80034884 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80034884");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80034884");
            return;
        }
        String oldName = f.getName();
        f.setName("read_hw_channel_bits9_11_by_role_index_via_esco_remap",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}