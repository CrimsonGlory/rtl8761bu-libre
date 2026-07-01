// Rename FUN_80037370 -> configure_hw_regs_and_init_for_sco_teardown
// Pass 221, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass221Region80030000Fun80037370 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80037370");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80037370");
            return;
        }
        String oldName = f.getName();
        f.setName("configure_hw_regs_and_init_for_sco_teardown",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}