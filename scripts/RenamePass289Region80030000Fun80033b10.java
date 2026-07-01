// Rename FUN_80033b10 -> return_ff_default_link_mode_change_prehook_stub
// Pass 289, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass289Region80030000Fun80033b10 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80033b10");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80033b10");
            return;
        }
        String oldName = f.getName();
        f.setName("return_ff_default_link_mode_change_prehook_stub",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}