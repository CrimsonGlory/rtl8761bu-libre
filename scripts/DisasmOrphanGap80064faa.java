import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.Function;
import ghidra.program.model.address.Address;

public class DisasmOrphanGap80064faa extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address start = toAddr(0x80064faaL);
        Address end = toAddr(0x80065067L);
        Address cur = start;
        while (cur.compareTo(end) <= 0) {
            Instruction ins = getInstructionAt(cur);
            if (ins != null) {
                println(String.format("0x%08x  %s", cur.getOffset(), ins.toString()));
                cur = cur.add(ins.getLength());
            } else {
                println(String.format("0x%08x  (undef)", cur.getOffset()));
                cur = cur.add(2);
            }
        }
    }
}
