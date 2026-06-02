package de.cvogtlaender.interpreter.ast.expression;

import de.cvogtlaender.interpreter.ast.AstNode;
import de.cvogtlaender.interpreter.ast.type.Type;

public abstract class Expr extends AstNode {
  private Type inferredType;
  private boolean isLValue;

  public Type getInferredType() {
    return this.inferredType;
  }

  public void setInferredType(Type inferredType) {
    this.inferredType = inferredType;
  }

  public boolean isLValue() {
    return this.isLValue;
  }

  public void setIsLValue(boolean isLValue) {
    this.isLValue = isLValue;
  }
}
