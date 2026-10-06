package de.cvogtlaender.mcp;

import java.io.IOException;

public class McpServerMain {

  public static void main(String[] args) throws IOException {
    new McpStdioServer(new MiniCppTools(), System.in, System.out).serve();
  }
}
