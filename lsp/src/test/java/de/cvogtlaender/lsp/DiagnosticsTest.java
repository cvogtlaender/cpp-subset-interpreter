package de.cvogtlaender.lsp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.services.LanguageClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DiagnosticsTest {

  private static final String URI = "file:///test.cpp";

  private final RecordingClient client = new RecordingClient();
  private MiniCppLanguageServer server;

  @BeforeEach
  void start() {
    server = new MiniCppLanguageServer(0);
    server.connect(client);
    server.initialize(new InitializeParams()).join();
  }

  @AfterEach
  void stop() {
    server.shutdown().join();
  }

  @Test
  void publishesErrorsWithLspPositions() throws InterruptedException {
    open("int main() {\n  int x = 1;\n  x = y;\n}");

    PublishDiagnosticsParams published = client.next();
    assertEquals(URI, published.getUri());
    assertEquals(1, published.getDiagnostics().size());
    Diagnostic d = published.getDiagnostics().get(0);
    assertEquals("use of undeclared identifier 'y'", d.getMessage().getLeft());
    assertEquals("resolve", d.getCode().getLeft());
    assertEquals(new Position(2, 6), d.getRange().getStart());
    assertEquals(new Position(2, 7), d.getRange().getEnd());
  }

  @Test
  void reportsSyntaxErrors() throws InterruptedException {
    open("int main() { int x = ; }");
    Diagnostic d = client.next().getDiagnostics().get(0);
    assertEquals("syntax", d.getCode().getLeft());
    assertEquals(new Position(0, 21), d.getRange().getStart());
  }

  @Test
  void clearsDiagnosticsWhenFixedAndClosed() throws InterruptedException {
    open("int main() { return x; }");
    assertEquals(1, client.next().getDiagnostics().size());

    change(2, "int main() { return 0; }");
    PublishDiagnosticsParams fixed = client.next();
    assertTrue(fixed.getDiagnostics().isEmpty());
    assertEquals(2, fixed.getVersion());
    assertNotNull(((MiniCppTextDocumentService) server.getTextDocumentService()).document(URI).compilation());

    server.getTextDocumentService().didClose(new DidCloseTextDocumentParams(new TextDocumentIdentifier(URI)));
    assertTrue(client.next().getDiagnostics().isEmpty());
  }

  private void open(String text) {
    server.getTextDocumentService().didOpen(
        new DidOpenTextDocumentParams(new TextDocumentItem(URI, "minicpp", 1, text)));
  }

  private void change(int version, String text) {
    server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(
        new VersionedTextDocumentIdentifier(URI, version), List.of(new TextDocumentContentChangeEvent(text))));
  }

  private static final class RecordingClient implements LanguageClient {
    final BlockingQueue<PublishDiagnosticsParams> diagnostics = new LinkedBlockingQueue<>();

    PublishDiagnosticsParams next() throws InterruptedException {
      PublishDiagnosticsParams params = diagnostics.poll(5, TimeUnit.SECONDS);
      assertNotNull(params, "no diagnostics published");
      return params;
    }

    @Override
    public void publishDiagnostics(PublishDiagnosticsParams params) {
      diagnostics.add(params);
    }

    @Override
    public void telemetryEvent(Object object) {
    }

    @Override
    public void showMessage(MessageParams messageParams) {
    }

    @Override
    public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
      return CompletableFuture.completedFuture(null);
    }

    @Override
    public void logMessage(MessageParams message) {
    }
  }
}
