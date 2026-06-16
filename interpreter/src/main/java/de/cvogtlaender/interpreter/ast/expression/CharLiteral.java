package de.cvogtlaender.interpreter.ast.expression;

import de.cvogtlaender.interpreter.visitor.AstVisitor;

public class CharLiteral extends LiteralExpr {

  private Character value;

  public CharLiteral(Character value) {
    this.value = value;
  }

  public Character getValue() {
    return value;
  }

  @Override
  public String toStringTree() {
    return "\"CharLit\": \"" + value + "\"";
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitCharLiteral(this);
  }
}
