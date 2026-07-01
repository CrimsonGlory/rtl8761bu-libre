// Rename FUN_80024050 -> read_max_encryption_key_size_from_config
// Pass 6 continuation (301), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80024050 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80024050");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80024050");
            return;
        }
        String oldName = f.getName();
        f.setName("read_max_encryption_key_size_from_config",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}