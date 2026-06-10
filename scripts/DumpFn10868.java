// Dump GZF byte-identical body for FUN_80110868 (322 B @ 0x80110868).
// @category Wairz

import ghidra.app.script.GhidraScript;

public class DumpFn10868 extends GhidraScript {
  public void run() throws Exception {
    long start = 0x80110868L;
    int n = 322;
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
