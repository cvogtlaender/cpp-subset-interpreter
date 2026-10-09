package de.cvogtlaender.interpreter.ast.expression;

import de.cvogtlaender.interpreter.visitor.AstVisitor;

public class NullptrLiteral extends LiteralExpr {

  @Override
  public String toStringTree() {
    return "\"NullptrLit\": null";
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitNullptrLiteral(this);
  }
}
