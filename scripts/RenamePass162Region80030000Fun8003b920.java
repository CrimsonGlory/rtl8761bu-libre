// Rename FUN_8003b920 -> compute_clamped_tx_power_level_from_link_class_baselines
// Pass 162, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass162Region80030000Fun8003b920 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003b920");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003b920");
            return;
        }
        String oldName = f.getName();
        f.setName("compute_clamped_tx_power_level_from_link_class_baselines",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}