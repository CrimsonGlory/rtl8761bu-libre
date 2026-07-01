// Rename FUN_80025dd8 -> extract_passkey_confirm_bit_and_send_lmp_0x3f
// Pass 6 continuation (158), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80025dd8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80025dd8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80025dd8");
            return;
        }
        String oldName = f.getName();
        f.setName("extract_passkey_confirm_bit_and_send_lmp_0x3f",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}