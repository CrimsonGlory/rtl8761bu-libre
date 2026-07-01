// Rename FUN_80025800 -> classify_ssp_pairing_method_from_io_capabilities
// Pass 6 continuation (157), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun80025800 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80025800");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80025800");
            return;
        }
        String oldName = f.getName();
        f.setName("classify_ssp_pairing_method_from_io_capabilities",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}