package de.cvogtlaender.interpreter.ast.statement;

import de.cvogtlaender.interpreter.ast.expression.Expr;
import de.cvogtlaender.interpreter.visitor.AstVisitor;

public class DeleteStmt extends Stmt {
  private Expr pointer;

  public DeleteStmt(Expr pointer) {
    this.pointer = pointer;
  }

  public Expr getPointer() {
    return pointer;
  }

  @Override
  public String toStringTree() {
    return "\"DeleteStmt\": { \"Pointer\": " + this.pointer.toStringTree() + "}";
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitDeleteStmt(this);
  }
}
