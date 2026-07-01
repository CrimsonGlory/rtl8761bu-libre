// Rename FUN_800343dc -> reinit_conn_table_crypto_preserve_active_slot_record
// Pass 159, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass159Region80030000Fun800343dc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800343dc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800343dc");
            return;
        }
        String oldName = f.getName();
        f.setName("reinit_conn_table_crypto_preserve_active_slot_record",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}