// Rename FUN_800242dc -> handle_lmp_ext_subopcode_0x21_reply_0x22_when_pairing_mode
// Pass 6 continuation (218), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800242dc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800242dc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800242dc");
            return;
        }
        String oldName = f.getName();
        f.setName("handle_lmp_ext_subopcode_0x21_reply_0x22_when_pairing_mode",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}