import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;
import ghidra.program.model.address.Address;

public class RenamePass12eeRegion80070000Fun80064fbc extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x80064fbcL;
        Address entry = toAddr(addr);
        Function fn = getFunctionAt(entry);
        if (fn == null) {
            disassemble(entry);
            fn = createFunction(entry, null);
            println("created fn at 0x" + Long.toHexString(addr));
        }
        if (fn == null) {
            println("MISSED at 0x" + Long.toHexString(addr));
            return;
        }
        String newName = "gate_psm_qos_dual_quantizer_or_iter_channel_slots";
        String old = fn.getName();
        fn.setName(newName, SourceType.USER_DEFINED);
        println("renamed=1 old=" + old + " new=" + newName + " size=" + fn.getBody().getNumAddresses());
    }
}
