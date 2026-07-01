// Rename FUN_8002adbc -> lookup_acl_reassembly_gate_byte_for_link_substate2_or_default1
// Pass 6 continuation (288), region 0x80020000
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;

public class RenamePass6Region80020000Fun8002adbc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address addr = currentProgram.getAddressFactory().getAddress("0x8002adbc");
        Function f = getFunctionAt(addr);
        if (f == null) {
            println("ERROR: no function at 0x8002adbc");
            return;
        }
        String oldName = f.getName();
        f.setName("lookup_acl_reassembly_gate_byte_for_link_substate2_or_default1",
                ghidra.program.model.symbol.SourceType.USER_DEFINED);
        println("Renamed " + oldName + " -> " + f.getName());
    }
}