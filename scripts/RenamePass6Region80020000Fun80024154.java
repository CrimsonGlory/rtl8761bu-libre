// Rename FUN_80024154 -> start_encryption_vsc_pair_on_mode3_enable
// Pass 6 continuation (57), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80024154 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80024154");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80024154");
            return;
        }
        String oldName = f.getName();
        f.setName("start_encryption_vsc_pair_on_mode3_enable",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}