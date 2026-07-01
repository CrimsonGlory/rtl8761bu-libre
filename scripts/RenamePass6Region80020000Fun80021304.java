// Rename FUN_80021304 -> return_literal_0x2bf20_constant
// Pass 6 continuation (311), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80021304 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80021304");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80021304");
            return;
        }
        String oldName = f.getName();
        f.setName("return_literal_0x2bf20_constant",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}