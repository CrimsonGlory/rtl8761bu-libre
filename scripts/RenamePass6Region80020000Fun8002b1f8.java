// Rename FUN_8002b1f8 -> spin_until_global_hw_clock_advances_by_ticks
// Pass 6 continuation (118), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002b1f8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002b1f8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002b1f8");
            return;
        }
        String oldName = f.getName();
        f.setName("spin_until_global_hw_clock_advances_by_ticks",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}