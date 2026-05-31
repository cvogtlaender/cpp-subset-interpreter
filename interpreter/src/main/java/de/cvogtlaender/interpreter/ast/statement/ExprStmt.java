package de.cvogtlaender.interpreter.ast.statement;

import de.cvogtlaender.interpreter.ast.expression.Expr;

public class ExprStmt extends Stmt {
  private Expr expression;

  public ExprStmt(Expr expression) {
    this.expression = expression;
  }

  public Expr getExpression() {
    return this.expression;
  }

  @Override
  public String toStringTree() {
    return "\"ExprStmt\": { \"Expr\": " + (expression == null ? "\"null\"" : this.expression.toStringTree()) + "}";
  }

}
