package de.cvogtlaender.interpreter.ast.type;

import de.cvogtlaender.interpreter.visitor.AstVisitor;

public class ReferenceType extends Type {
  private Type referencedType;

  public ReferenceType(Type referencedType) {
    this.referencedType = referencedType;
  }

  public Type getReferencedType() {
    return referencedType;
  }

  @Override
  public String getName() {
    return this.referencedType.getName() + "&";
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitReferenceType(this);
  }
}
