package de.cvogtlaender.interpreter.ast.expression;

public class UnaryExpr extends Expr {
  public enum Operation {
    POSITIVE, NEGATE, NOT
  }

  private Operation operation;
  private Expr expr;

  public UnaryExpr(Operation operation, Expr expr) {
    this.operation = operation;
    this.expr = expr;
  }

  public Operation getOperation() {
    return operation;
  }

  public Expr getExpr() {
    return expr;
  }

  @Override
  public String toStringTree() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'toStringTree'");
  }

}
