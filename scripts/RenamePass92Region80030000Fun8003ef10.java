// Rename FUN_8003ef10 -> orchestrate_sco_esco_link_setup_baseband_regs_collision_and_afh
// Pass 92, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass92Region80030000Fun8003ef10 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003ef10");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003ef10");
            return;
        }
        String oldName = f.getName();
        f.setName("orchestrate_sco_esco_link_setup_baseband_regs_collision_and_afh",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}