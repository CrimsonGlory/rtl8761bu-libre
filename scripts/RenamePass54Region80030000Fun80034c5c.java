// Rename FUN_80034c5c -> program_packet_type_if_stored_matches_expected
// Pass 54, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass54Region80030000Fun80034c5c extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x80034c5c");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x80034c5c");
            return;
        }
        String oldName = f.getName();
        f.setName("program_packet_type_if_stored_matches_expected",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}