// Rename FUN_8003d354 -> dispatch_acl_fragment_with_per_conn_reassembly_flags
// Pass 125, region 0x80030000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass125Region80030000Fun8003d354 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8003d354");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8003d354");
            return;
        }
        String oldName = f.getName();
        f.setName("dispatch_acl_fragment_with_per_conn_reassembly_flags",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}