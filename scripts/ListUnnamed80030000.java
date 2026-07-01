// List unnamed FUN_* in 0x80030000-0x8003ffff, sort by xref_in desc then size desc
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.*;
import ghidra.program.model.address.*;
import java.util.*;

public class ListUnnamed80030000 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address start = toAddr(0x80030000L);
        Address end = toAddr(0x8003ffffL);
        FunctionIterator it = currentProgram.getFunctionManager().getFunctions(start, true);
        List<String> rows = new ArrayList<>();
        while (it.hasNext()) {
            Function fn = it.next();
            if (fn.getEntryPoint().compareTo(end) > 0) break;
            String name = fn.getName();
            if (!name.startsWith("FUN_8003")) continue;
            int size = (int) fn.getBody().getNumAddresses();
            int xrefIn = fn.getSymbol().getReferenceCount();
            rows.add(String.format("%04d %4dB xref_in=%d 0x%08x %s",
                xrefIn, size, xrefIn, fn.getEntryPoint().getOffset(), name));
        }
        Collections.sort(rows, Collections.reverseOrder());
        int n = Math.min(10, rows.size());
        for (int i = 0; i < n; i++) println(rows.get(i));
        println("total_unnamed=" + rows.size());
    }
}