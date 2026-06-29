// RenamePass12adRegion80070000Fun800796b8.java — Pass 12ad codec bit-stream serializer
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class RenamePass12adRegion80070000Fun800796b8 extends GhidraScript {
    @Override
    public void run() throws Exception {
        long addr = 0x800796b8L;
        String newName = "serialize_codec_buffer_bits_lsb_to_state_machine";
        Function fn = getFunctionAt(toAddr(addr));
        if (fn == null) {
            println("MISSING at 0x" + Long.toHexString(addr));
            return;
        }
        String old = fn.getName();
        fn.setName(newName, SourceType.USER_DEFINED);
        println("renamed=1 old=" + old + " new=" + newName);
    }
}
