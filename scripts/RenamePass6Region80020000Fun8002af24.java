// Rename FUN_8002af24 -> drain_sco_pending_queue_by_conn_index
// Pass 6 continuation (275), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002af24 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002af24");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002af24");
            return;
        }
        String oldName = f.getName();
        f.setName("drain_sco_pending_queue_by_conn_index",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}