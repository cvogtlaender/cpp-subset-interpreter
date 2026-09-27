package de.cvogtlaender.lsp;

import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

import de.cvogtlaender.interpreter.MiniCppLexer;

/**
 * The tokens of a document (without comments and whitespace), ordered by
 * position. Token offsets count code points, which equal UTF-16 offsets for
 * all characters in the BMP.
 */
public final class Tokens {

  private final List<? extends Token> tokens;

  private Tokens(List<? extends Token> tokens) {
    this.tokens = tokens;
  }

  public static Tokens lex(String text) {
    MiniCppLexer lexer = new MiniCppLexer(CharStreams.fromString(text));
    lexer.removeErrorListeners();
    return new Tokens(lexer.getAllTokens());
  }

  public int size() {
    return tokens.size();
  }

  /** The token at {@code index}, or null if out of range. */
  public Token get(int index) {
    return index >= 0 && index < tokens.size() ? tokens.get(index) : null;
  }

  public static int start(Token token) {
    return token.getStartIndex();
  }

  public static int end(Token token) {
    return token.getStopIndex() + 1;
  }

  /** Index of the token starting exactly at {@code offset}, or -1. */
  public int indexStartingAt(int offset) {
    int i = firstEndingAfter(offset);
    return i < tokens.size() && start(tokens.get(i)) == offset ? i : -1;
  }

  /**
   * Index of the identifier under the cursor: the one containing
   * {@code offset} or ending right before it. -1 if there is none.
   */
  public int identifierAt(int offset) {
    int i = firstEndingAfter(offset);
    if (i < tokens.size() && start(tokens.get(i)) <= offset && isIdentifier(tokens.get(i))) {
      return i;
    }
    if (i > 0 && end(tokens.get(i - 1)) == offset && isIdentifier(tokens.get(i - 1))) {
      return i - 1;
    }
    return -1;
  }

  /** Index of the last token ending at or before {@code offset}, or -1. */
  public int lastEndingAtOrBefore(int offset) {
    return firstEndingAfter(offset) - 1;
  }

  public static boolean isIdentifier(Token token) {
    return token != null && token.getType() == MiniCppLexer.Identifier;
  }

  // binary search for the first token whose end lies after offset
  private int firstEndingAfter(int offset) {
    int low = 0;
    int high = tokens.size();
    while (low < high) {
      int mid = (low + high) >>> 1;
      if (end(tokens.get(mid)) <= offset) {
        low = mid + 1;
      } else {
        high = mid;
      }
    }
    return low;
  }
}
