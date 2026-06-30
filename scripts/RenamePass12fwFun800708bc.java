// Rename FUN_800708bc -> handle_lmp_489_subcase7_conn_substate_emit_25c_268
// Pass 12fw, region 0x80070000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass12fwFun800708bc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800708bc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800708bc");
            return;
        }
        String oldName = f.getName();
        f.setName("handle_lmp_489_subcase7_conn_substate_emit_25c_268",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}