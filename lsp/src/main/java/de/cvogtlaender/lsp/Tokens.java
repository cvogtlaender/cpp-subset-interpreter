package de.cvogtlaender.lsp;

import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

import de.cvogtlaender.interpreter.MiniCppLexer;

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

  public Token get(int index) {
    return index >= 0 && index < tokens.size() ? tokens.get(index) : null;
  }

  public static int start(Token token) {
    return token.getStartIndex();
  }

  public static int end(Token token) {
    return token.getStopIndex() + 1;
  }

  public int indexStartingAt(int offset) {
    int i = firstEndingAfter(offset);
    return i < tokens.size() && start(tokens.get(i)) == offset ? i : -1;
  }

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

  public int lastEndingAtOrBefore(int offset) {
    return firstEndingAfter(offset) - 1;
  }

  public static boolean isIdentifier(Token token) {
    return token != null && token.getType() == MiniCppLexer.Identifier;
  }

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
