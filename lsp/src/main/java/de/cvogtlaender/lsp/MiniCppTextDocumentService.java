package de.cvogtlaender.lsp;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.PrepareRenameDefaultBehavior;
import org.eclipse.lsp4j.PrepareRenameParams;
import org.eclipse.lsp4j.PrepareRenameResult;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.Either3;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;

/**
 * Document synchronization and per-document features. Every change schedules
 * a debounced re-analysis with {@code MiniCpp.compile}, whose diagnostics are
 * published to the client. Requests use the analysis of the current text,
 * computing it right away if the debounced one is still pending.
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
    doc.update(params.getContentChanges(), params.getTextDocument().getVersion());
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
    Analysis analysis = doc.analysis();
    if (analysis.compilation() == null || analysis.version() != doc.version()) {
      return; // crashed (logged), or edited meanwhile and a newer analysis is scheduled
    }
    publish(uri, analysis.compilation().diagnostics().stream().map(Positions::toLsp).toList(), analysis.version());
  }

  private void publish(String uri, List<org.eclipse.lsp4j.Diagnostic> diagnostics, Integer version) {
    if (client != null) {
      client.publishDiagnostics(new PublishDiagnosticsParams(uri, diagnostics, version));
    }
  }

  /** Runs a request against the current analysis of a document; {@code empty} if it is not open. */
  private <T> CompletableFuture<T> withAnalysis(String uri, T empty, Function<Analysis, T> request) {
    return CompletableFuture.supplyAsync(() -> {
      Document doc = documents.get(uri);
      if (doc == null) {
        return empty;
      }
      return request.apply(doc.analysis());
    });
  }

  // Features

  @Override
  public CompletableFuture<Hover> hover(HoverParams params) {
    return withAnalysis(params.getTextDocument().getUri(), null,
        analysis -> Hovers.hover(analysis, params.getPosition()));
  }

  @Override
  public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
    return withAnalysis(params.getTextDocument().getUri(), Either.forLeft(List.of()),
        analysis -> Either.forLeft(Completions.complete(analysis, params.getPosition())));
  }

  @Override
  public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(
      DefinitionParams params) {
    String uri = params.getTextDocument().getUri();
    return withAnalysis(uri, Either.forLeft(List.of()),
        analysis -> Either.forLeft(Navigation.definition(uri, analysis, params.getPosition())));
  }

  @Override
  public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
    String uri = params.getTextDocument().getUri();
    boolean includeDeclaration = params.getContext() == null || params.getContext().isIncludeDeclaration();
    return withAnalysis(uri, List.of(),
        analysis -> Navigation.references(uri, analysis, params.getPosition(), includeDeclaration));
  }

  @Override
  public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(DocumentHighlightParams params) {
    return withAnalysis(params.getTextDocument().getUri(), List.of(),
        analysis -> Navigation.highlights(analysis, params.getPosition()));
  }

  @Override
  public CompletableFuture<Either3<Range, PrepareRenameResult, PrepareRenameDefaultBehavior>> prepareRename(
      PrepareRenameParams params) {
    return withAnalysis(params.getTextDocument().getUri(), null, analysis -> {
      Range range = Navigation.prepareRename(analysis, params.getPosition());
      return range == null ? null : Either3.forFirst(range);
    });
  }

  @Override
  public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
    String uri = params.getTextDocument().getUri();
    return withAnalysis(uri, null,
        analysis -> Navigation.rename(uri, analysis, params.getPosition(), params.getNewName()));
  }

  @Override
  public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(
      DocumentSymbolParams params) {
    return withAnalysis(params.getTextDocument().getUri(), List.of(),
        analysis -> Navigation.documentSymbols(analysis).stream()
            .map(Either::<SymbolInformation, DocumentSymbol>forRight).toList());
  }

  @Override
  public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
    return withAnalysis(params.getTextDocument().getUri(), List.of(), analysis -> {
      String text = analysis.text().text();
      String formatted = CodeFormatter.format(text, params.getOptions().getTabSize(),
          params.getOptions().isInsertSpaces());
      if (formatted == null || formatted.equals(text)) {
        return List.of();
      }
      return List.of(new TextEdit(analysis.text().wholeDocument(), formatted));
    });
  }

  @Override
  public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
    String uri = params.getTextDocument().getUri();
    return withAnalysis(uri, List.of(),
        analysis -> CodeActions.quickFixes(uri, analysis, params.getContext().getDiagnostics()).stream()
            .map(Either::<Command, CodeAction>forRight).toList());
  }
}
