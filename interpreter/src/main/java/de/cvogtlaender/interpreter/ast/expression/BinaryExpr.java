package de.cvogtlaender.interpreter.ast.expression;

public class BinaryExpr extends Expr {
  public enum Operator {
    ADD, SUB, MUL, DIV, MOD,
    EQ, NEQ,
    LT, LE, GT, GE,
    AND, OR
  }

  private Operator operator;
  private Expr leftHandSide;
  private Expr rightHandSide;

  public BinaryExpr(Operator operation, Expr leftHandSide, Expr rightHandSide) {
    this.operator = operation;
    this.leftHandSide = leftHandSide;
    this.rightHandSide = rightHandSide;
  }

  public Operator getOperator() {
    return operator;
  }

  public Expr getLeftHandSide() {
    return leftHandSide;
  }

  public Expr getRightHandSide() {
    return rightHandSide;
  }

  @Override
  public String toStringTree() {
    StringBuilder builder = new StringBuilder();
    builder.append("\"BinaryExpr\": { \"Op\": \"" + this.operator.toString() + "\", ");
    builder.append("\"LHS\": ");
    builder.append(this.leftHandSide == null ? "\"null\"" : this.leftHandSide.toStringTree());
    builder.append(", \"RHS\": ");
    builder.append(this.rightHandSide == null ? "\"null\"" : this.rightHandSide.toStringTree());
    builder.append("}");
    return builder.toString();
  }
}
