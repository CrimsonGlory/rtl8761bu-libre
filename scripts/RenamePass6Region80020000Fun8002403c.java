// Rename FUN_8002403c -> check_local_role_byte_matches_lmp_pdu_role_bit
// Pass 6 continuation (294), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002403c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002403c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002403c");
            return;
        }
        String oldName = f.getName();
        f.setName("check_local_role_byte_matches_lmp_pdu_role_bit",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}