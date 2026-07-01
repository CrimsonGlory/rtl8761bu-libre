// Rename FUN_80029260 -> handle_lmp_ext_enc_sub2_inner0x17_stop_enc_substate_c_or_finalize
// Pass 6 continuation (115), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80029260 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80029260");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80029260");
            return;
        }
        String oldName = f.getName();
        f.setName("handle_lmp_ext_enc_sub2_inner0x17_stop_enc_substate_c_or_finalize",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}