// Rename FUN_800243b8 -> send_lmp_ext_opcode_reply_maybe_ssp_complete
// Pass 6 continuation (181), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800243b8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800243b8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800243b8");
            return;
        }
        String oldName = f.getName();
        f.setName("send_lmp_ext_opcode_reply_maybe_ssp_complete",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}