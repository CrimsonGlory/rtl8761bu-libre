// Rename FUN_80024638 -> copy_global_key_template_xor_0x51_and_send_lmp_0x0a
// Pass 6 continuation (137), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80024638 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80024638");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80024638");
            return;
        }
        String oldName = f.getName();
        f.setName("copy_global_key_template_xor_0x51_and_send_lmp_0x0a",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}