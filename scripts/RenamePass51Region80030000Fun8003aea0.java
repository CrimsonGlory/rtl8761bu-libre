// Rename FUN_8003aea0 -> register_script_interpreter
// Pass 51, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass51Region80030000Fun8003aea0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003aea0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003aea0");
            return;
        }
        String oldName = f.getName();
        f.setName("register_script_interpreter",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}