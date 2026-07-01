// Rename FUN_80033164 -> stub_return_zero_for_LC_event_TX_dispatcher
// Pass 238, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass238Region80030000Fun80033164 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80033164");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80033164");
            return;
        }
        String oldName = f.getName();
        f.setName("stub_return_zero_for_LC_event_TX_dispatcher",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}