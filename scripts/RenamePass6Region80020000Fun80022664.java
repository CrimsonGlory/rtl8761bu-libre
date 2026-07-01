// Rename FUN_80022664 -> reject_pending_lmp_with_not_accepted_reason0x18_and_clear
// Pass 6 continuation (231), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80022664 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80022664");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80022664");
            return;
        }
        String oldName = f.getName();
        f.setName("reject_pending_lmp_with_not_accepted_reason0x18_and_clear",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}