// Rename FUN_80037710 -> boot_init_reset_conn_sco_hw_optional_fc95_and_descriptor_reinit
// Pass 166, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass166Region80030000Fun80037710 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80037710");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80037710");
            return;
        }
        String oldName = f.getName();
        f.setName("boot_init_reset_conn_sco_hw_optional_fc95_and_descriptor_reinit",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}