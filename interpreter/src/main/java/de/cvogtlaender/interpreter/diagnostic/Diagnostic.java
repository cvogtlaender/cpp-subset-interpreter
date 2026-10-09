package de.cvogtlaender.interpreter.diagnostic;

import de.cvogtlaender.interpreter.ast.AstNode;

public record Diagnostic(Phase phase, int line, int column, int endLine, int endColumn, String message) {

  public enum Phase {
    SYNTAX, RESOLVE, TYPE, RUNTIME
  }

  public static Diagnostic at(Phase phase, AstNode node, String message) {
    if (node == null) {
      return new Diagnostic(phase, 0, 0, 0, 0, message);
    }
    return new Diagnostic(phase, node.getLine(), node.getColumn(), node.getEndLine(), node.getEndColumn(), message);
  }

  public String format() {
    String kind = switch (phase) {
      case SYNTAX -> "syntax error";
      case RESOLVE, TYPE -> "error";
      case RUNTIME -> "runtime error";
    };
    if (line <= 0) {
      return kind + ": " + message;
    }
    return line + ":" + (column + 1) + ": " + kind + ": " + message;
  }

  @Override
  public String toString() {
    return format();
  }
}
