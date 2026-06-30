// Rename FUN_80024314 -> vsc_fc95_slot0_send_lmp_ext_0x7f_0x21_and_lmp_268
// Pass 6 continuation (87), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80024314 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80024314");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80024314");
            return;
        }
        String oldName = f.getName();
        f.setName("vsc_fc95_slot0_send_lmp_ext_0x7f_0x21_and_lmp_268",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}