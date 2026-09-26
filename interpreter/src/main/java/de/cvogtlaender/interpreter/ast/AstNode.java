package de.cvogtlaender.interpreter.ast;

import de.cvogtlaender.interpreter.visitor.AstVisitor;

public abstract class AstNode {

  // 1-based lines, 0-based columns (ANTLR convention); end is exclusive
  private int line;
  private int column;
  private int endLine;
  private int endColumn;

  public int getLine() {
    return line;
  }

  public int getColumn() {
    return column;
  }

  public int getEndLine() {
    return endLine;
  }

  public int getEndColumn() {
    return endColumn;
  }

  public void setRange(int line, int column, int endLine, int endColumn) {
    this.line = line;
    this.column = column;
    this.endLine = endLine;
    this.endColumn = endColumn;
  }

  public void copyRange(AstNode other) {
    setRange(other.line, other.column, other.endLine, other.endColumn);
  }

  public abstract String toStringTree();

  public abstract <T> T accept(AstVisitor<T> visitor);
}
