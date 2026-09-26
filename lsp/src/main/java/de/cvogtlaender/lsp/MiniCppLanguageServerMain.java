package de.cvogtlaender.lsp;

import java.io.PrintStream;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

/** Starts the MiniC++ language server, speaking JSON-RPC over stdin/stdout. */
public final class MiniCppLanguageServerMain {

  private MiniCppLanguageServerMain() {
  }

  public static void main(String[] args) throws Exception {
    // stdout carries the protocol; stray prints would corrupt it
    PrintStream protocolOut = System.out;
    System.setOut(System.err);

    MiniCppLanguageServer server = new MiniCppLanguageServer();
    Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, System.in, protocolOut);
    server.connect(launcher.getRemoteProxy());
    launcher.startListening().get();
  }
}
