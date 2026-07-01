// Rename FUN_80036f60 -> hci_reset_reinit_conn_subsystem_lmp_and_descriptor_tables
// Pass 113, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass113Region80030000Fun80036f60 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80036f60");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80036f60");
            return;
        }
        String oldName = f.getName();
        f.setName("hci_reset_reinit_conn_subsystem_lmp_and_descriptor_tables",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}