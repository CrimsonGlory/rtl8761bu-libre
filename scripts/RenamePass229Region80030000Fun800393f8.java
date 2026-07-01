// Rename FUN_800393f8 -> invoke_register_script_from_global_context_0x58_0x5c
// Pass 229, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass229Region80030000Fun800393f8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800393f8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800393f8");
            return;
        }
        String oldName = f.getName();
        f.setName("invoke_register_script_from_global_context_0x58_0x5c",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}