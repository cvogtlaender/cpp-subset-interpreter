package de.cvogtlaender.lsp;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import de.cvogtlaender.interpreter.ast.AstNode;
import de.cvogtlaender.interpreter.diagnostic.Diagnostic;

public final class Positions {

  private Positions() {
  }

  public static Range range(AstNode node) {
    return range(node.getLine(), node.getColumn(), node.getEndLine(), node.getEndColumn());
  }

  public static Range range(int line, int column, int endLine, int endColumn) {
    if (line <= 0) {
      return new Range(new Position(0, 0), new Position(0, 0));
    }
    return new Range(new Position(line - 1, column), new Position(endLine - 1, endColumn));
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
