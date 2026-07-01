// Rename FUN_80028550 -> handle_lmp_not_accepted_opcode_0x40_ssp_complete_by_state_bitmask
// Pass 6 continuation (116), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80028550 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80028550");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80028550");
            return;
        }
        String oldName = f.getName();
        f.setName("handle_lmp_not_accepted_opcode_0x40_ssp_complete_by_state_bitmask",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}