// Dump GZF byte-identical body for FUN_8010a84c (450 B @ 0x8010a84c).
// @category Wairz

import ghidra.app.script.GhidraScript;

public class DumpFnA84c extends GhidraScript {
  public void run() throws Exception {
    long start = 0x8010a84cL;
    int n = 450;
    StringBuilder line = new StringBuilder();
    int col = 0;
    println("BYTES_START");
    for (int i = 0; i < n; i++) {
      if (col == 16) {
        println("\t.byte\t" + line);
        line = new StringBuilder();
        col = 0;
      }
      if (col > 0) line.append(", ");
      line.append(String.format("0x%02x", getByte(toAddr(start + i)) & 0xff));
      col++;
    }
    if (col > 0) println("\t.byte\t" + line);
    println("BYTES_END");
  }
}
