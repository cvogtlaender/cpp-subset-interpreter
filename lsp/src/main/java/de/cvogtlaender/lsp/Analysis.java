package de.cvogtlaender.lsp;

import de.cvogtlaender.interpreter.MiniCpp;
import de.cvogtlaender.interpreter.ast.Program;
import de.cvogtlaender.interpreter.semantic.GlobalScope;

/**
 * The analysis of one version of a document.
 *
 * {@code compilation} is null if the pipeline crashed. {@code index} is null
 * if there is no AST (syntax errors). {@code lastParsed} is the most recent
 * analysis of this document that has an AST, possibly this one; completion
 * falls back to it while the user is typing incomplete code.
 */
public record Analysis(int version, SourceText text, Tokens tokens, MiniCpp.Compilation compilation,
    SymbolIndex index, Analysis lastParsed) {

  public static Analysis of(String text, int version, Analysis previous) {
    SourceText source = new SourceText(text);
    Tokens tokens = Tokens.lex(text);
    MiniCpp.Compilation compilation;
    try {
      compilation = MiniCpp.compile(text);
    } catch (RuntimeException | StackOverflowError e) {
      // never let a crash in the pipeline take down the server
      System.err.println("analysis failed: " + e);
      compilation = null;
    }

    if (compilation == null || compilation.program() == null) {
      Analysis fallback = previous == null ? null : previous.lastParsed();
      return new Analysis(version, source, tokens, compilation, null, fallback);
    }
    SymbolIndex index = null;
    try {
      index = SymbolIndex.build(compilation.program(), compilation.globals(), tokens, source);
    } catch (RuntimeException e) {
      System.err.println("indexing failed: " + e);
    }
    return new Analysis(version, source, tokens, compilation, index, null);
  }

  @Override
  public Analysis lastParsed() {
    return hasProgram() ? this : lastParsed;
  }

  public boolean hasProgram() {
    return compilation != null && compilation.program() != null;
  }

  public Program program() {
    return hasProgram() ? compilation.program() : null;
  }

  public GlobalScope globals() {
    return hasProgram() ? compilation.globals() : null;
  }
}
