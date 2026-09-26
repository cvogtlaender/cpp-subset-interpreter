package de.cvogtlaender.lsp;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.services.WorkspaceService;

/** Workspace-level notifications; MiniC++ programs are single files, so nothing to do yet. */
public class MiniCppWorkspaceService implements WorkspaceService {

  @Override
  public void didChangeConfiguration(DidChangeConfigurationParams params) {
  }

  @Override
  public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
  }
}
