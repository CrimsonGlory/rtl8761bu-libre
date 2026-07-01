// Rename FUN_8003ca5c -> dispatch_vsc_fcc0_or_feature_page_fptr_on_role_bit0
// Pass 179, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass179Region80030000Fun8003ca5c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003ca5c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003ca5c");
            return;
        }
        String oldName = f.getName();
        f.setName("dispatch_vsc_fcc0_or_feature_page_fptr_on_role_bit0",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}