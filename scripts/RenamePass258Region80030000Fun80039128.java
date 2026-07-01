// Rename FUN_80039128 -> config_gated_select_packet_type_for_hw_translator_4link
// Pass 258, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass258Region80030000Fun80039128 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80039128");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80039128");
            return;
        }
        String oldName = f.getName();
        f.setName("config_gated_select_packet_type_for_hw_translator_4link",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}