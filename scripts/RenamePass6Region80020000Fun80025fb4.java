// Rename FUN_80025fb4 -> dispatch_ssp_numeric_comparison_confirm_and_arm_state
// Pass 6 continuation (163), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80025fb4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80025fb4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80025fb4");
            return;
        }
        String oldName = f.getName();
        f.setName("dispatch_ssp_numeric_comparison_confirm_and_arm_state",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}