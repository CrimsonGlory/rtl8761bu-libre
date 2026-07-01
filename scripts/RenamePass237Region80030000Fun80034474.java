// Rename FUN_80034474 -> clear_global_dword_via_PTR_DAT_8003447c
// Pass 237, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass237Region80030000Fun80034474 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80034474");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80034474");
            return;
        }
        String oldName = f.getName();
        f.setName("clear_global_dword_via_PTR_DAT_8003447c",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}