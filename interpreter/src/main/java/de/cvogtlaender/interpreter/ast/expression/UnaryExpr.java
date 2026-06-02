package de.cvogtlaender.interpreter.ast.expression;

public class UnaryExpr extends Expr {
  public enum Operator {
    POSITIVE, NEGATE, NOT
  }

  private Operator operator;
  private Expr expr;

  public UnaryExpr(Operator operation, Expr expr) {
    this.operator = operation;
    this.expr = expr;
  }

  public Operator getOperator() {
    return operator;
  }

  public Expr getExpr() {
    return expr;
  }

  @Override
  public String toStringTree() {
    StringBuilder builder = new StringBuilder();
    builder.append("\"UnarayExpr\": { \"Op\": \"" + this.operator.toString() + "\", ");
    builder.append("\"Expr\": ");
    builder.append(this.expr == null ? "\"null\"" : this.expr.toStringTree());
    builder.append("}");
    return builder.toString();
  }

}
