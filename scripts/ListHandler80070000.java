// List unnamed FUN_* in 0x80070000-0x8007ffff, 151-300 bytes (HANDLER tier), sort by xref_in desc
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.*;
import ghidra.program.model.address.*;
import java.util.*;

public class ListHandler80070000 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address start = toAddr(0x80070000L);
        Address end = toAddr(0x8007ffffL);
        FunctionIterator it = currentProgram.getFunctionManager().getFunctions(start, true);
        List<String> rows = new ArrayList<>();
        while (it.hasNext()) {
            Function fn = it.next();
            if (fn.getEntryPoint().compareTo(end) > 0) break;
            String name = fn.getName();
            if (!name.startsWith("FUN_8007")) continue;
            int size = (int) fn.getBody().getNumAddresses();
            if (size < 151 || size > 300) continue;
            int xrefIn = fn.getSymbol().getReferenceCount();
            rows.add(String.format("%04d 0x%08x %4dB xref_in=%d %s",
                xrefIn, fn.getEntryPoint().getOffset(), size, xrefIn, name));
        }
        Collections.sort(rows, Collections.reverseOrder());
        int n = Math.min(15, rows.size());
        for (int i = 0; i < n; i++) println(rows.get(i));
        println("total_handler_tier=" + rows.size());
    }
}
