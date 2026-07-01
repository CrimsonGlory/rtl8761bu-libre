// Rename FUN_80038e74 -> pack_bitmasked_bytes_into_bb_regs_0x62_low_0x63_high_via_hook
// Pass 173, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass173Region80030000Fun80038e74 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80038e74");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80038e74");
            return;
        }
        String oldName = f.getName();
        f.setName("pack_bitmasked_bytes_into_bb_regs_0x62_low_0x63_high_via_hook",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}