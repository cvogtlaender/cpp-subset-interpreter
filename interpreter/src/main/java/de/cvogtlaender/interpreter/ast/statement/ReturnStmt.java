package de.cvogtlaender.interpreter.ast.statement;

import de.cvogtlaender.interpreter.ast.expression.Expr;

public class ReturnStmt extends Stmt {
  private Expr returnValue;

  public ReturnStmt(Expr returnValue) {
    this.returnValue = returnValue;
  }

  public Expr getReturnValue() {
    return returnValue;
  }

  @Override
  public String toStringTree() {
    return "\"ReturnStmt\": { \"ReturnVal\": " + (returnValue == null ? "null" : this.returnValue.toStringTree()) + "}";
  }
}
