// Rename FUN_80038d7c -> read_bb_reg_0x1e_5bit_field_via_hook
// Pass 136, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass136Region80030000Fun80038d7c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80038d7c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80038d7c");
            return;
        }
        String oldName = f.getName();
        f.setName("read_bb_reg_0x1e_5bit_field_via_hook",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}