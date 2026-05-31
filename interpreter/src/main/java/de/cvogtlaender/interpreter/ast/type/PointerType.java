package de.cvogtlaender.interpreter.ast.type;

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

}
