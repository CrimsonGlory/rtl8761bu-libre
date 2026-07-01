// Rename FUN_80037804 -> clear_conn_pending_lmp_0x50_and_pdu_on_crypto_teardown
// Pass 182, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass182Region80030000Fun80037804 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80037804");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80037804");
            return;
        }
        String oldName = f.getName();
        f.setName("clear_conn_pending_lmp_0x50_and_pdu_on_crypto_teardown",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}