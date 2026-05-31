package de.cvogtlaender.interpreter.ast.expression;

public class BinaryExpr extends Expr {
  public enum Operation {
    ADD, SUB, MUL, DIV, MOD,
    EQ, NEQ,
    LT, LE, GT, GE,
    AND, OR
  }

  private Operation operation;
  private Expr leftHandSide;
  private Expr rightHandSide;

  public BinaryExpr(Operation operation, Expr leftHandSide, Expr rightHandSide) {
    this.operation = operation;
    this.leftHandSide = leftHandSide;
    this.rightHandSide = rightHandSide;
  }

  public Operation getOperation() {
    return operation;
  }

  public Expr getLeftHandSide() {
    return leftHandSide;
  }

  public Expr getRightHandSide() {
    return rightHandSide;
  }

  @Override
  public String toStringTree() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'toStringTree'");
  }
}
