// Rename FUN_80039738 -> invoke_sco_esco_required_then_optional_fptr_hooks
// Pass 226, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass226Region80030000Fun80039738 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80039738");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80039738");
            return;
        }
        String oldName = f.getName();
        f.setName("invoke_sco_esco_required_then_optional_fptr_hooks",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}