// Rename FUN_8002ffc4 -> vsc_toggle_config_d0_bit_0x4000_and_field132_by_enable
// Pass 6 continuation (112), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002ffc4 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002ffc4");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002ffc4");
            return;
        }
        String oldName = f.getName();
        f.setName("vsc_toggle_config_d0_bit_0x4000_and_field132_by_enable",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}