package de.cvogtlaender.lsp;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionOptions;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.RenameOptions;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.ServerInfo;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

public class MiniCppLanguageServer implements LanguageServer, LanguageClientAware {

  public static final long DEFAULT_DEBOUNCE_MILLIS = 200;

  private final MiniCppTextDocumentService textDocuments;
  private final MiniCppWorkspaceService workspace = new MiniCppWorkspaceService();
  private boolean shutdownRequested;

  public MiniCppLanguageServer() {
    this(DEFAULT_DEBOUNCE_MILLIS);
  }

  public MiniCppLanguageServer(long debounceMillis) {
    this.textDocuments = new MiniCppTextDocumentService(debounceMillis);
  }

  @Override
  public void connect(LanguageClient client) {
    textDocuments.connect(client);
  }

  @Override
  public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
    ServerCapabilities capabilities = new ServerCapabilities();
    capabilities.setTextDocumentSync(TextDocumentSyncKind.Incremental);
    capabilities.setHoverProvider(true);
    capabilities.setCompletionProvider(new CompletionOptions(false, List.of(".", ">")));
    capabilities.setDefinitionProvider(true);
    capabilities.setReferencesProvider(true);
    capabilities.setDocumentHighlightProvider(true);
    capabilities.setDocumentSymbolProvider(true);
    capabilities.setRenameProvider(new RenameOptions(true));
    capabilities.setCodeActionProvider(new CodeActionOptions(List.of(CodeActionKind.QuickFix)));

    return CompletableFuture.completedFuture(
        new InitializeResult(capabilities, new ServerInfo("minicpp-lsp", "1.0.0")));
  }

  @Override
  public CompletableFuture<Object> shutdown() {
    shutdownRequested = true;
    textDocuments.close();
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public void exit() {
    System.exit(shutdownRequested ? 0 : 1);
  }

  @Override
  public TextDocumentService getTextDocumentService() {
    return textDocuments;
  }

  @Override
  public WorkspaceService getWorkspaceService() {
    return workspace;
  }
}
