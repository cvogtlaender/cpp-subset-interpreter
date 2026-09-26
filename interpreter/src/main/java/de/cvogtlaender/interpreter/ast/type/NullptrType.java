package de.cvogtlaender.interpreter.ast.type;

import de.cvogtlaender.interpreter.visitor.AstVisitor;

/** Type of the literal 'nullptr'; converts to every pointer type. */
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
