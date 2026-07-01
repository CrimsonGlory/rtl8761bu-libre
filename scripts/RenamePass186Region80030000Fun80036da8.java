// Rename FUN_80036da8 -> log_lc_tx_evt_0x32a_and_enqueue_lmp_tx_pending_descriptor
// Pass 186, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass186Region80030000Fun80036da8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80036da8");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80036da8");
            return;
        }
        String oldName = f.getName();
        f.setName("log_lc_tx_evt_0x32a_and_enqueue_lmp_tx_pending_descriptor",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}