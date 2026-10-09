package de.cvogtlaender.interpreter.ast.expression;

import de.cvogtlaender.interpreter.visitor.AstVisitor;

public class UnaryExpr extends Expr {
  public enum Operator {
    POSITIVE, NEGATE, NOT, DEREF, ADDRESS_OF
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

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitUnaryExpr(this);
  }
}
