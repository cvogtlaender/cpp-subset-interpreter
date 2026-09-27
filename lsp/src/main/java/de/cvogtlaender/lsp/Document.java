package de.cvogtlaender.lsp;

import java.util.List;

import org.eclipse.lsp4j.TextDocumentContentChangeEvent;

import de.cvogtlaender.interpreter.MiniCpp;

/**
 * An open text document and the analysis of its current text.
 *
 * The analysis is computed lazily: by the debounced background analysis
 * after edits, or on demand when a request needs the current state.
 */
public final class Document {

  private final String uri;
  private String text;
  private int version;
  private Analysis analysis;

  public Document(String uri, String text, int version) {
    this.uri = uri;
    this.text = text;
    this.version = version;
  }

  public String uri() {
    return uri;
  }

  public synchronized String text() {
    return text;
  }

  public synchronized int version() {
    return version;
  }

  /** The compilation of the latest analysis, which may lag behind the text. */
  public synchronized MiniCpp.Compilation compilation() {
    return analysis == null ? null : analysis.compilation();
  }

  /** Applies full or incremental changes in order and sets the new version. */
  synchronized void update(List<TextDocumentContentChangeEvent> changes, int version) {
    for (TextDocumentContentChangeEvent change : changes) {
      if (change.getRange() == null) {
        text = change.getText();
      } else {
        SourceText source = new SourceText(text);
        int start = source.offset(change.getRange().getStart());
        int end = Math.max(start, source.offset(change.getRange().getEnd()));
        text = text.substring(0, start) + change.getText() + text.substring(end);
      }
    }
    this.version = version;
  }

  /** The analysis of the current text, computing it if necessary. */
  public synchronized Analysis analysis() {
    if (analysis == null || analysis.text().text() != text) {
      analysis = Analysis.of(text, version, analysis);
    }
    return analysis;
  }
}
