// Rename FUN_80021dcc -> clear_connection_slot_lmp_pdu_and_pending_fields
// Pass 6 continuation (122), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80021dcc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80021dcc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80021dcc");
            return;
        }
        String oldName = f.getName();
        f.setName("clear_connection_slot_lmp_pdu_and_pending_fields",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}