// Rename FUN_800244f8 -> send_lmp_encryption_key_size_mask_res_0x3b_from_config
// Pass 6 continuation (185), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800244f8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800244f8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800244f8");
            return;
        }
        String oldName = f.getName();
        f.setName("send_lmp_encryption_key_size_mask_res_0x3b_from_config",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}