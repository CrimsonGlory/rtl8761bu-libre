// Rename FUN_80021ea0 -> clear_fifteen_dword_global_table_at_boot
// Pass 6 continuation (270), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80021ea0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80021ea0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80021ea0");
            return;
        }
        String oldName = f.getName();
        f.setName("clear_fifteen_dword_global_table_at_boot",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}