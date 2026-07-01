// Rename FUN_800347d4 -> commit_sco_teardown_bb_hook_pairs_and_18pair_param_table
// Pass 155, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass155Region80030000Fun800347d4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800347d4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800347d4");
            return;
        }
        String oldName = f.getName();
        f.setName("commit_sco_teardown_bb_hook_pairs_and_18pair_param_table",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}