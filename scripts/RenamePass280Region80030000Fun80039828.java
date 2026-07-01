// Rename FUN_80039828 -> read_config_signed_byte_at_0x279_when_field620_bit2_set
// Pass 280, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass280Region80030000Fun80039828 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80039828");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80039828");
            return;
        }
        String oldName = f.getName();
        f.setName("read_config_signed_byte_at_0x279_when_field620_bit2_set",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}