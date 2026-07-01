// Rename FUN_800393d8 -> invoke_register_script_from_global_context_0x60_0x64
// Pass 135, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass135Region80030000Fun800393d8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800393d8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800393d8");
            return;
        }
        String oldName = f.getName();
        f.setName("invoke_register_script_from_global_context_0x60_0x64",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}