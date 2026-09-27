package de.cvogtlaender.lsp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import de.cvogtlaender.interpreter.MiniCppLexer;
import de.cvogtlaender.interpreter.MiniCppParser;
import de.cvogtlaender.interpreter.MiniCppParser.ClassDefContext;
import de.cvogtlaender.interpreter.MiniCppParser.ConstructorDefContext;
import de.cvogtlaender.interpreter.MiniCppParser.DeclarationContext;
import de.cvogtlaender.interpreter.MiniCppParser.FunctionDefContext;
import de.cvogtlaender.interpreter.MiniCppParser.IfStmtContext;
import de.cvogtlaender.interpreter.MiniCppParser.MethodDefContext;
import de.cvogtlaender.interpreter.MiniCppParser.NewExprContext;
import de.cvogtlaender.interpreter.MiniCppParser.PostfixPartContext;
import de.cvogtlaender.interpreter.MiniCppParser.ProgramContext;
import de.cvogtlaender.interpreter.MiniCppParser.StatementContext;
import de.cvogtlaender.interpreter.MiniCppParser.TypeContext;
import de.cvogtlaender.interpreter.MiniCppParser.TypeRefContext;
import de.cvogtlaender.interpreter.MiniCppParser.UnaryExprContext;
import de.cvogtlaender.interpreter.MiniCppParser.VarDeclContext;
import de.cvogtlaender.interpreter.MiniCppParser.WhileStmtContext;

/**
 * Pretty-printer driven by the parse tree: one statement per line, braces
 * K&R style, {@code public:} labels at class level, non-block bodies of
 * {@code if}/{@code while} on their own indented line, spaces around binary
 * operators and none after unary ones, {@code T* p} for pointer and reference
 * types. Comments are kept, as are single blank lines; top-level declarations
 * are separated by one blank line.
 *
 * Programs with syntax errors are not formatted.
 */
public final class CodeFormatter {

  private enum Separator {
    NONE, SPACE, NEWLINE, BLANK_LINE
  }

  private record Comment(String text, boolean line, int newlinesBefore) {
  }

  private final String source;
  private final String indentUnit;
  private final String newline;
  private final List<TerminalNode> terminals = new ArrayList<>();
  private final List<Integer> extraIndent = new ArrayList<>();
  private final Set<Integer> branchStarts = new HashSet<>();
  private final Set<Integer> declarationStarts = new HashSet<>();
  private final StringBuilder out = new StringBuilder();
  private int depth;

  private CodeFormatter(String source, int tabSize, boolean insertSpaces) {
    this.source = source;
    this.indentUnit = insertSpaces ? " ".repeat(Math.max(1, tabSize)) : "\t";
    this.newline = source.contains("\r\n") ? "\r\n" : "\n";
  }

  /** The formatted source, or null if it has syntax errors. */
  public static String format(String source, int tabSize, boolean insertSpaces) {
    boolean[] failed = { false };
    BaseErrorListener listener = new BaseErrorListener() {
      @Override
      public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int column, String msg,
          RecognitionException e) {
        failed[0] = true;
      }
    };
    MiniCppLexer lexer = new MiniCppLexer(CharStreams.fromString(source));
    lexer.removeErrorListeners();
    lexer.addErrorListener(listener);
    MiniCppParser parser = new MiniCppParser(new CommonTokenStream(lexer));
    parser.removeErrorListeners();
    parser.addErrorListener(listener);
    ProgramContext program = parser.program();
    if (failed[0]) {
      return null;
    }

    CodeFormatter formatter = new CodeFormatter(source, tabSize, insertSpaces);
    formatter.collect(program, 0);
    return formatter.emit();
  }

  // Pass 1: terminals with their indentation context

  private void collect(ParseTree tree, int extra) {
    if (tree instanceof TerminalNode terminal) {
      if (terminal.getSymbol().getType() != Token.EOF) {
        terminals.add(terminal);
        extraIndent.add(extra);
      }
      return;
    }
    if (tree instanceof StatementContext statement && isNonBlockBody(statement)) {
      branchStarts.add(terminals.size());
      extra++;
    }
    if (tree instanceof DeclarationContext && !terminals.isEmpty()) {
      declarationStarts.add(terminals.size());
    }
    for (int i = 0; i < tree.getChildCount(); i++) {
      collect(tree.getChild(i), extra);
    }
  }

  // the body of 'if'/'else'/'while' without braces, except the 'if' of 'else if'
  private static boolean isNonBlockBody(StatementContext statement) {
    if (statement.block() != null) {
      return false;
    }
    if (statement.getParent() instanceof IfStmtContext ifStmt) {
      boolean elseBranch = ifStmt.statement().size() > 1 && ifStmt.statement(1) == statement;
      return !(elseBranch && statement.ifStmt() != null);
    }
    return statement.getParent() instanceof WhileStmtContext;
  }

  // Pass 2: output

  private String emit() {
    int previousEnd = 0;
    for (int k = 0; k < terminals.size(); k++) {
      TerminalNode node = terminals.get(k);
      Token token = node.getSymbol();
      String gap = source.substring(previousEnd, token.getStartIndex());

      Separator separator = k == 0 ? Separator.NONE : separator(terminals.get(k - 1), node);
      if (branchStarts.contains(k)) {
        separator = Separator.NEWLINE;
      }
      if (declarationStarts.contains(k)) {
        separator = Separator.BLANK_LINE;
      }
      boolean afterOpenBrace = k > 0 && type(terminals.get(k - 1)) == MiniCppLexer.LBRACE;
      boolean closeBrace = token.getType() == MiniCppLexer.RBRACE;

      int indent = depth + extraIndent.get(k);
      separator = comments(gap, separator, indent, k == 0, afterOpenBrace);

      if (closeBrace) {
        depth--;
        indent = depth + extraIndent.get(k);
      }
      if (isLabel(node)) {
        indent--;
      }
      if (separator == Separator.NEWLINE && countNewlines(trailingSpace(gap)) >= 2 && !afterOpenBrace && !closeBrace) {
        separator = Separator.BLANK_LINE;
      } else if (separator == Separator.BLANK_LINE && closeBrace) {
        separator = Separator.NEWLINE;
      }
      separate(separator, indent);
      out.append(token.getText());

      if (token.getType() == MiniCppLexer.LBRACE) {
        depth++;
      }
      previousEnd = token.getStopIndex() + 1;
    }

    Separator end = comments(source.substring(previousEnd), Separator.NEWLINE, 0, terminals.isEmpty(), false);
    if (out.length() > 0 && end != Separator.NONE) {
      out.append(newline);
    }
    return out.toString();
  }

  /**
   * Emits the comments in a gap between two tokens and returns the separator
   * to use before the next token.
   */
  private Separator comments(String gap, Separator separator, int indent, boolean atStart, boolean afterOpenBrace) {
    List<Comment> comments = parseComments(gap);
    if (comments.isEmpty()) {
      return separator;
    }
    boolean newlineNeeded = false;
    for (int i = 0; i < comments.size(); i++) {
      Comment c = comments.get(i);
      if (i == 0 && c.newlinesBefore() == 0 && !atStart) {
        out.append(' ');
      } else if (out.length() > 0) {
        boolean blank = !afterOpenBrace
            && (c.newlinesBefore() >= 2 || i == 0 && separator == Separator.BLANK_LINE);
        separate(blank ? Separator.BLANK_LINE : Separator.NEWLINE, indent);
      } else {
        out.append(indentUnit.repeat(Math.max(0, indent)));
      }
      out.append(c.text());
      newlineNeeded = c.line();
      afterOpenBrace = false;
    }
    int after = countNewlines(trailingSpace(gap));
    if (newlineNeeded || after > 0 || separator == Separator.NEWLINE || separator == Separator.BLANK_LINE) {
      return after >= 2 ? Separator.BLANK_LINE : Separator.NEWLINE;
    }
    return Separator.SPACE;
  }

  private void separate(Separator separator, int indent) {
    switch (separator) {
      case NONE -> {
      }
      case SPACE -> out.append(' ');
      case NEWLINE, BLANK_LINE -> {
        if (out.length() > 0) {
          out.append(newline);
          if (separator == Separator.BLANK_LINE) {
            out.append(newline);
          }
        }
        out.append(indentUnit.repeat(Math.max(0, indent)));
      }
    }
  }

  private static Separator separator(TerminalNode previous, TerminalNode current) {
    int p = type(previous);
    int c = type(current);

    if (p == MiniCppLexer.LBRACE) {
      return c == MiniCppLexer.RBRACE ? Separator.NONE : Separator.NEWLINE;
    }
    if (c == MiniCppLexer.RBRACE || p == MiniCppLexer.SEMI) {
      return Separator.NEWLINE;
    }
    if (p == MiniCppLexer.RBRACE) {
      return c == MiniCppLexer.SEMI ? Separator.NONE : c == MiniCppLexer.ELSE ? Separator.SPACE : Separator.NEWLINE;
    }
    if (p == MiniCppLexer.COLON && isLabel(previous)) {
      return Separator.NEWLINE;
    }
    if (c == MiniCppLexer.COLON && isLabel(current)) {
      return Separator.NONE;
    }
    if (p == MiniCppLexer.LPAREN || c == MiniCppLexer.RPAREN || c == MiniCppLexer.COMMA || c == MiniCppLexer.SEMI) {
      return Separator.NONE;
    }
    if (p == MiniCppLexer.DOT || p == MiniCppLexer.ARROW || c == MiniCppLexer.DOT || c == MiniCppLexer.ARROW) {
      return Separator.NONE;
    }
    if (previous.getParent() instanceof UnaryExprContext) {
      return Separator.NONE; // a unary operator is the only terminal child of unaryExpr
    }
    if (isTypeModifier(current)) {
      return Separator.NONE;
    }
    if (c == MiniCppLexer.LPAREN) {
      ParseTree parent = current.getParent();
      boolean attached = parent instanceof PostfixPartContext || parent instanceof FunctionDefContext
          || parent instanceof MethodDefContext || parent instanceof ConstructorDefContext
          || parent instanceof NewExprContext || parent instanceof VarDeclContext;
      return attached ? Separator.NONE : Separator.SPACE;
    }
    return Separator.SPACE;
  }

  private static boolean isTypeModifier(TerminalNode node) {
    return type(node) == MiniCppLexer.STAR && node.getParent() instanceof TypeContext
        || type(node) == MiniCppLexer.AMP && node.getParent() instanceof TypeRefContext;
  }

  /** The {@code public} or {@code :} of the {@code public:} label in a class body. */
  private static boolean isLabel(TerminalNode node) {
    if (!(node.getParent() instanceof ClassDefContext)) {
      return false;
    }
    TerminalNode previous = previousSibling(node);
    if (type(node) == MiniCppLexer.PUBLIC) {
      return previous != null && type(previous) == MiniCppLexer.LBRACE;
    }
    return type(node) == MiniCppLexer.COLON && previous != null && isLabel(previous);
  }

  private static TerminalNode previousSibling(TerminalNode node) {
    ParseTree parent = node.getParent();
    for (int i = 1; i < parent.getChildCount(); i++) {
      if (parent.getChild(i) == node) {
        return parent.getChild(i - 1) instanceof TerminalNode t ? t : null;
      }
    }
    return null;
  }

  private static int type(TerminalNode node) {
    return node.getSymbol().getType();
  }

  // Gaps

  private static List<Comment> parseComments(String gap) {
    List<Comment> comments = new ArrayList<>();
    int newlines = 0;
    int i = 0;
    while (i < gap.length()) {
      char ch = gap.charAt(i);
      if (ch == '\n') {
        newlines++;
        i++;
      } else if (gap.startsWith("//", i) || ch == '#') {
        int end = gap.indexOf('\n', i);
        end = end < 0 ? gap.length() : end;
        comments.add(new Comment(gap.substring(i, end).stripTrailing(), true, newlines));
        newlines = 0;
        i = end;
      } else if (gap.startsWith("/*", i)) {
        int end = gap.indexOf("*/", i + 2);
        end = end < 0 ? gap.length() : end + 2;
        comments.add(new Comment(gap.substring(i, end), false, newlines));
        newlines = 0;
        i = end;
      } else {
        i++;
      }
    }
    return comments;
  }

  // the whitespace at the end of a gap, after its last comment
  private static String trailingSpace(String gap) {
    int i = gap.length();
    while (i > 0 && Character.isWhitespace(gap.charAt(i - 1))) {
      i--;
    }
    return gap.substring(i);
  }

  private static int countNewlines(String s) {
    return (int) s.chars().filter(ch -> ch == '\n').count();
  }
}
