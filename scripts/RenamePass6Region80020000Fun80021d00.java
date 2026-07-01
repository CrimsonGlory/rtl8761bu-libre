// Rename FUN_80021d00 -> init_lc_global_eir_fec_tail_fields_from_config
// Pass 6 continuation (253), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80021d00 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80021d00");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80021d00");
            return;
        }
        String oldName = f.getName();
        f.setName("init_lc_global_eir_fec_tail_fields_from_config",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}