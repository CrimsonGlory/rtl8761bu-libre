// Rename FUN_8002c928 -> get_DHKey_to_3rd_param_p192
// Pass 6 continuation (72), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002c928 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002c928");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002c928");
            return;
        }
        String oldName = f.getName();
        f.setName("get_DHKey_to_3rd_param_p192",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}