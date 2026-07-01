// Rename FUN_80039448 -> init_register_script_context_from_config_and_clear_17pair_table
// Pass 153, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass153Region80030000Fun80039448 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80039448");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80039448");
            return;
        }
        String oldName = f.getName();
        f.setName("init_register_script_context_from_config_and_clear_17pair_table",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}