// Rename FUN_80039174 -> invoke_register_script_from_global_context_0x38_0x3c
// Pass 230, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass230Region80030000Fun80039174 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80039174");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80039174");
            return;
        }
        String oldName = f.getName();
        f.setName("invoke_register_script_from_global_context_0x38_0x3c",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}