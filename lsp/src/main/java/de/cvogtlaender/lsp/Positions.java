package de.cvogtlaender.lsp;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import de.cvogtlaender.interpreter.ast.AstNode;
import de.cvogtlaender.interpreter.diagnostic.Diagnostic;

/**
 * Conversion between interpreter and LSP coordinates. The interpreter uses
 * 1-based lines and 0-based columns (ANTLR), LSP uses 0-based lines and
 * characters; both use exclusive ends.
 *
 * Note: ANTLR columns count code points, LSP characters count UTF-16 units by
 * default. They only differ for characters outside the BMP (e.g. emoji).
 */
public final class Positions {

  private Positions() {
  }

  public static Range range(AstNode node) {
    return range(node.getLine(), node.getColumn(), node.getEndLine(), node.getEndColumn());
  }

  public static Range range(int line, int column, int endLine, int endColumn) {
    if (line <= 0) {
      // diagnostics without a position (e.g. "no 'main' function") go to the file start
      return new Range(new Position(0, 0), new Position(0, 0));
    }
    return new Range(new Position(line - 1, column), new Position(endLine - 1, endColumn));
  }

  /** True if the LSP position lies within the node's source range. */
  public static boolean contains(AstNode node, Position position) {
    int line = position.getLine() + 1;
    int column = position.getCharacter();
    boolean afterStart = line > node.getLine() || line == node.getLine() && column >= node.getColumn();
    boolean beforeEnd = line < node.getEndLine() || line == node.getEndLine() && column < node.getEndColumn();
    return afterStart && beforeEnd;
  }

  public static org.eclipse.lsp4j.Diagnostic toLsp(Diagnostic d) {
    org.eclipse.lsp4j.Diagnostic result = new org.eclipse.lsp4j.Diagnostic(
        range(d.line(), d.column(), d.endLine(), d.endColumn()), d.message());
    result.setSeverity(DiagnosticSeverity.Error);
    result.setSource("minicpp");
    result.setCode(d.phase().name().toLowerCase());
    return result;
  }
}
