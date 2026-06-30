// Rename FUN_80028e30 -> handle_lmp_ext_subopcode_0x1b_by_ssp_state
// Pass 6 continuation (84), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80028e30 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80028e30");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80028e30");
            return;
        }
        String oldName = f.getName();
        f.setName("handle_lmp_ext_subopcode_0x1b_by_ssp_state",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}