package de.cvogtlaender.interpreter.ast.expression;

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
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'toStringTree'");
  }
}
