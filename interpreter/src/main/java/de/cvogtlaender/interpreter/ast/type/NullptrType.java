package de.cvogtlaender.interpreter.ast.type;

import de.cvogtlaender.interpreter.visitor.AstVisitor;

public class NullptrType extends Type {

  @Override
  public String getName() {
    return "nullptr_t";
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitNullptrType(this);
  }
}
