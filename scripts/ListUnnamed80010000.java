// List unnamed FUN_* in 0x80010000-0x8001ffff, sort by xref_in desc then size desc
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.*;
import ghidra.program.model.address.*;
import java.util.*;

public class ListUnnamed80010000 extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address start = toAddr(0x80010000L);
        Address end = toAddr(0x8001ffffL);
        FunctionIterator it = currentProgram.getFunctionManager().getFunctions(start, true);
        List<String> rows = new ArrayList<>();
        int total = 0, unnamed = 0;
        while (it.hasNext()) {
            Function fn = it.next();
            if (fn.getEntryPoint().compareTo(end) > 0) break;
            total++;
            String name = fn.getName();
            if (!name.startsWith("FUN_8001")) continue;
            unnamed++;
            int size = (int) fn.getBody().getNumAddresses();
            int xrefIn = fn.getSymbol().getReferenceCount();
            rows.add(String.format("%04d %4d 0x%08x %s", xrefIn, size, fn.getEntryPoint().getOffset(), name));
        }
        Collections.sort(rows, (a, b) -> {
            int xa = Integer.parseInt(a.substring(0, 4));
            int xb = Integer.parseInt(b.substring(0, 4));
            if (xb != xa) return Integer.compare(xb, xa);
            int sa = Integer.parseInt(a.substring(5, 9).trim());
            int sb = Integer.parseInt(b.substring(5, 9).trim());
            return Integer.compare(sb, sa);
        });
        println("total=" + total + " unnamed=" + unnamed);
        int n = Math.min(15, rows.size());
        for (int i = 0; i < n; i++) println(rows.get(i));
    }
}