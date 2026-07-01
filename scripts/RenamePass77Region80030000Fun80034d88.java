// Rename FUN_80034d88 -> emit_link_mode_change_cleanup_status_with_dedup
// Pass 77, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass77Region80030000Fun80034d88 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80034d88");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80034d88");
            return;
        }
        String oldName = f.getName();
        f.setName("emit_link_mode_change_cleanup_status_with_dedup",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}