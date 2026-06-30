// Rename FUN_80028d98 -> handle_lmp_ext_dhkey_check_subopcode_0x1d_by_ssp_state
// Pass 6 continuation (76), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80028d98 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80028d98");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80028d98");
            return;
        }
        String oldName = f.getName();
        f.setName("handle_lmp_ext_dhkey_check_subopcode_0x1d_by_ssp_state",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}