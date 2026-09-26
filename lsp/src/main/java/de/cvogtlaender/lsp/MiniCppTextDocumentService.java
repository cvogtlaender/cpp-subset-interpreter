package de.cvogtlaender.lsp;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;

import de.cvogtlaender.interpreter.MiniCpp;

/**
 * Document synchronization and per-document features. Every change schedules
 * a debounced re-analysis with {@link MiniCpp#compile}, whose diagnostics are
 * published to the client.
 *
 * Features beyond diagnostics are stubs. After a successful analysis the AST
 * is fully resolved, see "Hinweise für den LSP-Server" in the README:
 * {@code VarExpr.getResolvedDecl()}, {@code CallExpr.getTarget()},
 * {@code MemberAccessExpr.getResolvedField()}, {@code Expr.getInferredType()}.
 */
public class MiniCppTextDocumentService implements TextDocumentService {

  private final Map<String, Document> documents = new ConcurrentHashMap<>();
  private final Map<String, ScheduledFuture<?>> pending = new ConcurrentHashMap<>();
  private final ScheduledExecutorService analyzer = Executors.newSingleThreadScheduledExecutor(r -> {
    Thread t = new Thread(r, "minicpp-analyzer");
    t.setDaemon(true);
    return t;
  });
  private final long debounceMillis;
  private LanguageClient client;

  public MiniCppTextDocumentService(long debounceMillis) {
    this.debounceMillis = debounceMillis;
  }

  void connect(LanguageClient client) {
    this.client = client;
  }

  void close() {
    analyzer.shutdownNow();
  }

  public Document document(String uri) {
    return documents.get(uri);
  }

  // Synchronization

  @Override
  public void didOpen(DidOpenTextDocumentParams params) {
    var item = params.getTextDocument();
    documents.put(item.getUri(), new Document(item.getUri(), item.getText(), item.getVersion()));
    scheduleAnalysis(item.getUri(), 0);
  }

  @Override
  public void didChange(DidChangeTextDocumentParams params) {
    Document doc = documents.get(params.getTextDocument().getUri());
    if (doc == null || params.getContentChanges().isEmpty()) {
      return;
    }
    // full sync: the last change holds the complete new text
    String text = params.getContentChanges().get(params.getContentChanges().size() - 1).getText();
    doc.update(text, params.getTextDocument().getVersion());
    scheduleAnalysis(doc.uri(), debounceMillis);
  }

  @Override
  public void didClose(DidCloseTextDocumentParams params) {
    String uri = params.getTextDocument().getUri();
    documents.remove(uri);
    ScheduledFuture<?> task = pending.remove(uri);
    if (task != null) {
      task.cancel(false);
    }
    publish(uri, List.of(), null);
  }

  @Override
  public void didSave(DidSaveTextDocumentParams params) {
  }

  private void scheduleAnalysis(String uri, long delayMillis) {
    ScheduledFuture<?> previous = pending.put(uri,
        analyzer.schedule(() -> analyze(uri), delayMillis, TimeUnit.MILLISECONDS));
    if (previous != null) {
      previous.cancel(false);
    }
  }

  private void analyze(String uri) {
    Document doc = documents.get(uri);
    if (doc == null) {
      return;
    }
    int version = doc.version();
    MiniCpp.Compilation compilation;
    try {
      compilation = MiniCpp.compile(doc.text());
    } catch (RuntimeException | StackOverflowError e) {
      // never let a crash in the pipeline take down the server
      System.err.println("analysis of " + uri + " failed: " + e);
      return;
    }
    if (doc.version() != version) {
      return; // edited meanwhile; a newer analysis is scheduled
    }
    doc.setCompilation(compilation);
    publish(uri, compilation.diagnostics().stream().map(Positions::toLsp).toList(), version);
  }

  private void publish(String uri, List<org.eclipse.lsp4j.Diagnostic> diagnostics, Integer version) {
    if (client != null) {
      client.publishDiagnostics(new PublishDiagnosticsParams(uri, diagnostics, version));
    }
  }

  // Features

  @Override
  public CompletableFuture<Hover> hover(HoverParams params) {
    // TODO: find the innermost expression at params.getPosition() (see
    // Positions.contains) and show its getInferredType(), or the declaration
    // it resolves to
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
    // TODO: keywords, variables in scope, functions/classes from
    // compilation.globals(), members after '.' and '->'
    return CompletableFuture.completedFuture(Either.forLeft(List.of()));
  }

  @Override
  public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(
      DefinitionParams params) {
    // TODO: resolve the identifier at the position to its Decl and return
    // new Location(uri, Positions.range(decl))
    return CompletableFuture.completedFuture(Either.forLeft(List.of()));
  }
}
