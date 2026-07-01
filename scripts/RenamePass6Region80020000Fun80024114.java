// Rename FUN_80024114 -> stop_encryption_lmp_25c_pair_on_mode_disable
// Pass 6 continuation (195), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80024114 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80024114");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80024114");
            return;
        }
        String oldName = f.getName();
        f.setName("stop_encryption_lmp_25c_pair_on_mode_disable",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}