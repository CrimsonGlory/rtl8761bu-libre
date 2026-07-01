// Rename FUN_80029cdc -> send_lmp_use_semi_permanent_key_0x32
// Pass 6 continuation (273), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80029cdc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80029cdc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80029cdc");
            return;
        }
        String oldName = f.getName();
        f.setName("send_lmp_use_semi_permanent_key_0x32",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}