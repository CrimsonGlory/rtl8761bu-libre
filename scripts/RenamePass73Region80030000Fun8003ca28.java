// Rename FUN_8003ca28 -> commit_hw_channel_merge_index_0x36_on_role_bit0
// Pass 73, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass73Region80030000Fun8003ca28 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003ca28");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003ca28");
            return;
        }
        String oldName = f.getName();
        f.setName("commit_hw_channel_merge_index_0x36_on_role_bit0",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}