// Rename FUN_800379cc -> read_global_ushort_inquiry_rssi_raw_at_DAT_800379d8
// Pass 236, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass236Region80030000Fun800379cc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x800379cc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x800379cc");
            return;
        }
        String oldName = f.getName();
        f.setName("read_global_ushort_inquiry_rssi_raw_at_DAT_800379d8",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}