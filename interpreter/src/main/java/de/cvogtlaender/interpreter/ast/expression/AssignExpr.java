package de.cvogtlaender.interpreter.ast.expression;

import de.cvogtlaender.interpreter.visitor.AstVisitor;

public class AssignExpr extends Expr {
  private Expr target;
  private Expr value;

  public AssignExpr(Expr target, Expr value) {
    this.target = target;
    this.value = value;
  }

  public Expr getTarget() {
    return this.target;
  }

  public Expr getValue() {
    return this.value;
  }

  @Override
  public String toStringTree() {
    StringBuilder builder = new StringBuilder();
    builder.append("\"AssignExpr\": { \"Target\": ");
    builder.append(this.target == null ? "\"null\"" : this.target.toStringTree());
    builder.append(", \"Value\": ");
    builder.append(this.value == null ? "\"null\"" : this.value.toStringTree());
    builder.append("}");
    return builder.toString();
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitAssignExpr(this);
  }
}
