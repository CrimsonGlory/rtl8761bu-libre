// Rename FUN_80033630 -> latch_config_bit0x164_8_when_global_status_bit13_set
// Pass 203, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass203Region80030000Fun80033630 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80033630");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80033630");
            return;
        }
        String oldName = f.getName();
        f.setName("latch_config_bit0x164_8_when_global_status_bit13_set",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}