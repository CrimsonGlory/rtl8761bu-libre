// Rename FUN_800285cc -> handle_lmp_not_accepted_opcode_0x3f_ssp_complete
// Pass 6 continuation (139), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800285cc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800285cc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800285cc");
            return;
        }
        String oldName = f.getName();
        f.setName("handle_lmp_not_accepted_opcode_0x3f_ssp_complete",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}