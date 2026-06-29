import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionIterator;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.address.Address;

public class FindCaller800647dc extends GhidraScript {
    @Override
    public void run() throws Exception {
        Address callSite = toAddr(0x80065008L);
        Address target = toAddr(0x800647dcL);

        Function fn = getFunctionContaining(callSite);
        println("containing=" + (fn == null ? "NONE" : fn.getEntryPoint() + " " + fn.getName()));

        Instruction ins = getInstructionAt(callSite);
        if (ins != null) {
            println("insn@" + callSite + ": " + ins.toString());
        } else {
            println("no instruction at call site");
        }

        // nearest function start at or before call site
        FunctionIterator fit = currentProgram.getFunctionManager().getFunctions(true);
        Function best = null;
        while (fit.hasNext()) {
            Function f = fit.next();
            Address ep = f.getEntryPoint();
            if (ep.compareTo(callSite) <= 0 && f.getBody().contains(callSite)) {
                best = f;
            }
        }
        if (best != null) {
            println("body_contains=" + best.getEntryPoint() + " " + best.getName()
                + " size=" + best.getBody().getNumAddresses());
        }

        // list functions in [0x80064a34, 0x80065068)
        println("--- functions 0x80064a34..0x80065068 ---");
        fit = currentProgram.getFunctionManager().getFunctions(true);
        while (fit.hasNext()) {
            Function f = fit.next();
            long ep = f.getEntryPoint().getOffset();
            if (ep >= 0x80064a34L && ep < 0x80065068L) {
                println(String.format("0x%08x %4d %s", ep,
                    f.getBody().getNumAddresses(), f.getName()));
            }
        }

        // xref scan
        println("--- xrefs to 0x800647dc ---");
        var refs = getReferencesTo(target);
        for (var ref : refs) {
            Address from = ref.getFromAddress();
            Function caller = getFunctionContaining(from);
            println("from=0x" + from + " caller="
                + (caller == null ? "NONE" : caller.getEntryPoint() + " " + caller.getName()));
        }
    }
}
