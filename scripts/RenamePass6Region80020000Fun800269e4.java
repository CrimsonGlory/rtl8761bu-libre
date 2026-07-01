// Rename FUN_800269e4 -> stub_return_zero_link_key_lookup_cluster_gap
// Pass 6 continuation (312), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800269e4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800269e4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800269e4");
            return;
        }
        String oldName = f.getName();
        f.setName("stub_return_zero_link_key_lookup_cluster_gap",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}