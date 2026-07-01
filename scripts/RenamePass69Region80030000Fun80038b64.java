// Rename FUN_80038b64 -> ilog2_floor_plus_two_dword
// Pass 69, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass69Region80030000Fun80038b64 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80038b64");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80038b64");
            return;
        }
        String oldName = f.getName();
        f.setName("ilog2_floor_plus_two_dword",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}