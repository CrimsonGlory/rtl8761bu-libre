// Rename FUN_8002a868 -> release_sco_hw_link_descriptor_slot
// Pass 6 continuation (10), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002a868 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002a868");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002a868");
            return;
        }
        String oldName = f.getName();
        f.setName("release_sco_hw_link_descriptor_slot",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}