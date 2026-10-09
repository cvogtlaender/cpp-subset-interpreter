package de.cvogtlaender.interpreter.ast.type;

import de.cvogtlaender.interpreter.visitor.AstVisitor;

public class PointerType extends Type {
  private Type pointeeType;

  public PointerType(Type pointeeType) {
    this.pointeeType = pointeeType;
  }

  public Type getPointeeType() {
    return this.pointeeType;
  }

  @Override
  public String getName() {
    return this.pointeeType.getName() + "*";
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitPointerType(this);
  }
}
