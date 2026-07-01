// Rename FUN_800211d0 -> read_be_uint_from_u8_buf_when_mode_0x10
// Pass 6 continuation (266), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun800211d0 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800211d0");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800211d0");
            return;
        }
        String oldName = f.getName();
        f.setName("read_be_uint_from_u8_buf_when_mode_0x10",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}