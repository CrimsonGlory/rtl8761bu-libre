// Rename FUN_8003845c -> log_lc_evt_0x321_with_hw_clock_by_role_index
// Pass 188, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass188Region80030000Fun8003845c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003845c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003845c");
            return;
        }
        String oldName = f.getName();
        f.setName("log_lc_evt_0x321_with_hw_clock_by_role_index",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}