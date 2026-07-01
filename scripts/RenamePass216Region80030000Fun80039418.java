// Rename FUN_80039418 -> invoke_register_script_dual_from_global_context_pairs
// Pass 216, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass216Region80030000Fun80039418 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80039418");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80039418");
            return;
        }
        String oldName = f.getName();
        f.setName("invoke_register_script_dual_from_global_context_pairs",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}