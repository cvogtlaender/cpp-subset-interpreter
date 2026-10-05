package de.cvogtlaender.mcp;

import java.io.IOException;

/**
 * Starts the MCP server on stdin/stdout. Stdout carries only protocol
 * messages, so diagnostics of the server itself go to stderr.
 */
public class McpServerMain {

  public static void main(String[] args) throws IOException {
    new McpStdioServer(new MiniCppTools(), System.in, System.out).serve();
  }
}
