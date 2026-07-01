// Rename FUN_80034a24 -> read_hw_clock_raw_dword_by_role_index
// Pass 44, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass44Region80030000Fun80034a24 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80034a24");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80034a24");
            return;
        }
        String oldName = f.getName();
        f.setName("read_hw_clock_raw_dword_by_role_index",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}