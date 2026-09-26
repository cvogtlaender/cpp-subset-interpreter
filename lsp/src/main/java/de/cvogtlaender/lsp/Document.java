package de.cvogtlaender.lsp;

import de.cvogtlaender.interpreter.MiniCpp;

/**
 * An open text document and the result of its latest analysis.
 *
 * {@link #compilation()} may lag behind {@link #text()} while an analysis is
 * pending, and its program is null if the text has syntax errors.
 */
public final class Document {

  private final String uri;
  private volatile String text;
  private volatile int version;
  private volatile MiniCpp.Compilation compilation;

  public Document(String uri, String text, int version) {
    this.uri = uri;
    this.text = text;
    this.version = version;
  }

  public String uri() {
    return uri;
  }

  public String text() {
    return text;
  }

  public int version() {
    return version;
  }

  public MiniCpp.Compilation compilation() {
    return compilation;
  }

  synchronized void update(String text, int version) {
    this.text = text;
    this.version = version;
  }

  void setCompilation(MiniCpp.Compilation compilation) {
    this.compilation = compilation;
  }
}
