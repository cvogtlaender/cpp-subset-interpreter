package de.cvogtlaender.interpreter.ast.type;

public class ReferenceType extends Type {
  private Type referencedType;

  public ReferenceType(Type referencedType) {
    this.referencedType = referencedType;
  }

  @Override
  public String getName() {
    return this.referencedType.getName() + "&";
  }

}
