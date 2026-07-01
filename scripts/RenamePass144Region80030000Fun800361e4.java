// Rename FUN_800361e4 -> flush_armed_esco_codec_slots_up_to_12_and_apply
// Pass 144, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass144Region80030000Fun800361e4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800361e4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800361e4");
            return;
        }
        String oldName = f.getName();
        f.setName("flush_armed_esco_codec_slots_up_to_12_and_apply",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}